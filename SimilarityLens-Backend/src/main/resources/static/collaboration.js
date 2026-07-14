// collaboration.js - WebSocket Collaboration Module for SimilarityLens

class SimilarityCollaboration {
    constructor(options) {
        this.sessionId = this.generateSessionId();
        this.partnerId = null;
        this.isPaired = false;
        this.stompClient = null;
        this.onImageReceived = options.onImageReceived || (() => {});
        this.onPartnerConnected = options.onPartnerConnected || (() => {});
        this.onPartnerDisconnected = options.onPartnerDisconnected || (() => {});
        this.onComparisonRequest = options.onComparisonRequest || (() => {});
        this.onComparisonResult = options.onComparisonResult || (() => {});
        this.ui = options.ui || null;
    }

    generateSessionId() {
        return 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected to WebSocket');
            this.register();
            this.subscribeToQueues();
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus('✓ Connected to server. Waiting for partner...');
            }
        }, (error) => {
            console.error('WebSocket connection error:', error);
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus('❌ Connection failed. Make sure backend is running on port 8080');
            }
        });
    }

    register() {
        this.stompClient.send("/app/register", {}, JSON.stringify({
            sessionId: this.sessionId
        }));
        
        // Subscribe to pairing response
        this.stompClient.subscribe(`/user/queue/paired`, (message) => {
            const response = JSON.parse(message.body);
            this.partnerId = response.partnerId;
            this.isPaired = true;
            this.onPartnerConnected(this.partnerId);
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus(`✓ Connected with partner! Ready to compare.`);
            }
        });
        
        // Subscribe to partner disconnect
        this.stompClient.subscribe(`/user/queue/partner/disconnected`, (message) => {
            const data = JSON.parse(message.body);
            this.isPaired = false;
            this.onPartnerDisconnected();
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus('⚠️ Partner disconnected. Waiting for new partner...');
            }
            this.register(); // Re-register to find new partner
        });
    }

    subscribeToQueues() {
        // Subscribe to incoming images
        this.stompClient.subscribe(`/user/queue/image`, (message) => {
            const imageMsg = JSON.parse(message.body);
            console.log('Received image from partner:', imageMsg.side);
            this.onImageReceived(imageMsg);
        });
        
        // Subscribe to comparison requests
        this.stompClient.subscribe(`/user/queue/compare/request`, (message) => {
            const request = JSON.parse(message.body);
            console.log('Received comparison request:', request.method);
            this.onComparisonRequest(request);
        });
        
        // Subscribe to comparison results
        this.stompClient.subscribe(`/user/queue/compare/result`, (message) => {
            const result = JSON.parse(message.body);
            console.log('Received comparison results');
            this.onComparisonResult(result);
        });
    }

    sendImage(imageFile, side) {
        if (!this.isPaired) {
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus('❌ Not connected to partner. Please wait for pairing.');
            }
            return false;
        }
        
        const reader = new FileReader();
        reader.onload = (e) => {
            const imageData = e.target.result;
            this.stompClient.send("/app/image", {}, JSON.stringify({
                fromSession: this.sessionId,
                imageData: imageData,
                side: side,
                filename: imageFile.name
            }));
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus(`📤 Sent ${side} image to partner`);
            }
        };
        reader.readAsDataURL(imageFile);
        return true;
    }

    requestComparison(method) {
        if (!this.isPaired) {
            if (this.ui && this.ui.showStatus) {
                this.ui.showStatus('❌ Not connected to partner');
            }
            return false;
        }
        
        this.stompClient.send("/app/compare/request", {}, JSON.stringify({
            fromSession: this.sessionId,
            method: method
        }));
        if (this.ui && this.ui.showStatus) {
            this.ui.showStatus(`📡 Requesting ${method} comparison from partner...`);
        }
        return true;
    }

    sendComparisonResult(forSession, results) {
        this.stompClient.send("/app/compare/result", {}, JSON.stringify({
            forSession: forSession,
            results: results
        }));
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.send("/app/disconnect", {}, JSON.stringify({
                sessionId: this.sessionId
            }));
            this.stompClient.disconnect();
        }
    }
}

// Global variables
let collaboration = null;
let remoteImages = {
    serial: { left: null, right: null },
    multi: { left: null, right: null },
    hybrid: { left: null, right: null },
    edge: { left: null, right: null },
    gifParallel: { left: null, right: null }
};

// Initialize collaboration when DOM is ready
function initCollaboration() {
    collaboration = new SimilarityCollaboration({
        onImageReceived: (imageMsg) => {
            console.log(`Received ${imageMsg.side} image from partner`);
            // Display received image in the corresponding preview
            const panel = getCurrentActivePanel();
            const previewId = getPreviewIdForSide(panel, imageMsg.side);
            const previewDiv = document.getElementById(previewId);
            
            if (previewDiv) {
                previewDiv.innerHTML = `<img src="${imageMsg.imageData}" style="max-height:220px; border-radius: 8px;">`;
                previewDiv.style.border = '2px solid #a78bfa';
                previewDiv.style.boxShadow = '0 0 15px rgba(167, 139, 250, 0.3)';
            }
            
            // Store the remote image
            storeRemoteImage(panel, imageMsg.side, imageMsg.imageData, imageMsg.filename);
            
            // Show notification
            showNotification(`📸 Partner shared ${imageMsg.side} image`, 'info');
        },
        
        onPartnerConnected: (partnerId) => {
            console.log('Partner connected:', partnerId);
            updatePartnerStatus(true);
            showNotification('✨ Partner connected! You can now share images.', 'success');
        },
        
        onPartnerDisconnected: () => {
            console.log('Partner disconnected');
            updatePartnerStatus(false);
            showNotification('👋 Partner disconnected', 'warning');
        },
        
        onComparisonRequest: async (request) => {
            console.log('Comparison requested:', request.method);
            showNotification(`🔍 Partner requested ${request.method} comparison`, 'info');
            // Auto-accept and perform comparison
            await performComparisonWithPartner(request.method);
        },
        
        onComparisonResult: (result) => {
            console.log('Received comparison results:', result);
            displayComparisonResults(result.results);
            showNotification(`📊 Similarity: ${formatPercentage(result.results)}`, 'success');
        },
        
        ui: {
            showStatus: (msg) => {
                updateStatusMessage(msg);
            }
        }
    });
    
    collaboration.connect();
}

// Helper functions
function getCurrentActivePanel() {
    const activePanel = document.querySelector('.algorithm-panel.active-panel');
    return activePanel ? activePanel.id : 'serial';
}

function getPreviewIdForSide(panel, side) {
    const panelMap = {
        serial: { left: 'previewSerialA', right: 'previewSerialB' },
        multi: { left: 'previewMultiA', right: 'previewMultiB' },
        hybrid: { left: 'previewHybridA', right: 'previewHybridB' },
        edge: { left: 'previewEdgeA', right: 'previewEdgeB' },
        gifParallel: { left: 'previewGifParallelA', right: 'previewGifParallelB' }
    };
    return panelMap[panel]?.[side === 'left' ? 'left' : 'right'];
}

function storeRemoteImage(panel, side, imageData, filename) {
    // Convert base64 to File object
    const blob = dataURLtoBlob(imageData);
    const file = new File([blob], filename, { type: blob.type });
    
    remoteImages[panel][side] = file;
    
    // Also update global state if available
    if (window.state && window.state[panel]) {
        window.state[panel][side === 'left' ? 'fileA' : 'fileB'] = file;
    }
}

function dataURLtoBlob(dataURL) {
    const arr = dataURL.split(',');
    const mime = arr[0].match(/:(.*?);/)[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], { type: mime });
}

async function performComparisonWithPartner(method) {
    const panel = getCurrentActivePanel();
    
    // Get images from both local and remote
    const localFile = window.state?.[panel]?.['fileA'];
    const remoteFile = remoteImages[panel]?.right || remoteImages[panel]?.left;
    
    if (!localFile || !remoteFile) {
        updateStatusMessage('❌ Missing images for comparison');
        return;
    }
    
    const formData = new FormData();
    const endpointMap = {
        serial: '/compare/serial',
        multi: '/compare/multi',
        hybrid: '/compare/hybrid',
        edge: '/compare/edge'
    };
    
    updateStatusMessage(`🔄 Running ${method} comparison...`);
    
    try {
        let result;
        
        if (method === 'hybrid') {
            // Handle text comparison
            const text1 = await localFile.text();
            const text2 = await remoteFile.text();
            
            const response = await fetch('/compare/hybrid', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text1, text2 })
            });
            result = await response.json();
        } else {
            formData.append('image1', localFile);
            formData.append('image2', remoteFile);
            
            const endpoint = endpointMap[method];
            const response = await fetch(endpoint, { method: 'POST', body: formData });
            result = await response.json();
        }
        
        // Send results back to partner
        collaboration.sendComparisonResult(collaboration.partnerId, result);
        
        // Also display locally
        displayComparisonResults(result);
        updateStatusMessage(`✅ Comparison complete!`);
        
    } catch (error) {
        console.error('Comparison error:', error);
        updateStatusMessage(`❌ Comparison failed: ${error.message}`);
    }
}

function displayComparisonResults(results) {
    const panel = getCurrentActivePanel();
    
    if (panel === 'serial') {
        if (results.ssim) document.getElementById('serialSsimVal').innerHTML = results.ssim;
        if (results.percent) {
            const percent = results.percent.replace('%', '');
            document.getElementById('serialPercent').innerHTML = results.percent;
            const fill = document.getElementById('serialProgressFill');
            if (fill) fill.style.width = percent + '%';
        }
    } 
    else if (panel === 'multi') {
        if (results.ssim) document.getElementById('multiSsimVal').innerHTML = results.ssim;
        if (results.percent) {
            const percent = results.percent.replace('%', '');
            const fill = document.getElementById('multiProgressFill');
            if (fill) fill.style.width = percent + '%';
        }
        if (results.boost) document.getElementById('multiBoost').innerHTML = `∼ ${results.boost}x`;
    }
    else if (panel === 'hybrid') {
        if (results.cosine) document.getElementById('hybridSsim').innerHTML = results.cosine;
        if (results.percent) {
            const fill = document.getElementById('hybridFillBar');
            if (fill) fill.style.width = results.percent;
        }
    }
    else if (panel === 'edge') {
        if (results.gifSimilarity) document.getElementById('edgeSimValue').innerHTML = results.gifSimilarity;
        if (results.percent) {
            const percent = results.percent.replace('%', '');
            const fill = document.getElementById('edgeProgressFill');
            if (fill) fill.style.width = percent + '%';
        }
    }
    else if (panel === 'gifParallel') {
        if (results.gifSimilarity) document.getElementById('gifParallelSim').innerHTML = results.gifSimilarity;
        if (results.percent) {
            const percent = results.percent.replace('%', '');
            const fill = document.getElementById('gifParallelFill');
            if (fill) fill.style.width = percent + '%';
        }
    }
}

function updatePartnerStatus(connected) {
    const statusBadge = document.getElementById('partnerStatus');
    if (statusBadge) {
        if (connected) {
            statusBadge.innerHTML = '🟢 Partner Connected';
            statusBadge.style.backgroundColor = 'rgba(34, 197, 94, 0.2)';
            statusBadge.style.color = '#4ade80';
        } else {
            statusBadge.innerHTML = '🟡 Waiting for Partner...';
            statusBadge.style.backgroundColor = 'rgba(234, 179, 8, 0.2)';
            statusBadge.style.color = '#facc15';
        }
    }
}

function updateStatusMessage(message) {
    const statusDiv = document.getElementById('collabStatusMsg');
    if (statusDiv) {
        statusDiv.innerHTML = message;
        statusDiv.style.opacity = '1';
        setTimeout(() => {
            if (statusDiv.innerHTML === message) {
                statusDiv.style.opacity = '0.5';
            }
        }, 3000);
    }
}

function showNotification(message, type = 'info') {
    // Create toast notification
    const toast = document.createElement('div');
    toast.className = 'collab-toast';
    toast.innerHTML = `
        <div style="
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: ${type === 'success' ? 'rgba(34, 197, 94, 0.9)' : type === 'warning' ? 'rgba(234, 179, 8, 0.9)' : 'rgba(167, 139, 250, 0.9)'};
            color: white;
            padding: 10px 20px;
            border-radius: 8px;
            font-size: 14px;
            z-index: 10000;
            animation: slideIn 0.3s ease;
            backdrop-filter: blur(10px);
        ">
            ${message}
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function formatPercentage(results) {
    if (results.percent) return results.percent;
    if (results.ssim) return (parseFloat(results.ssim) * 100).toFixed(2) + '%';
    if (results.cosine) return (parseFloat(results.cosine) * 100).toFixed(2) + '%';
    return 'N/A';
}

// Add CSS animation for notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
`;
document.head.appendChild(style);

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { SimilarityCollaboration, initCollaboration };
}