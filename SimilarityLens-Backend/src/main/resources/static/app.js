
// At the very top of app.js - use relative paths
const API_BASE_URL = '';  // Empty string means use same origin

// Or explicitly set to empty
// const API_BASE_URL = '';

async function callAPI(endpoint, formData, panelType) {
    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            body: formData
        });

        return await response.json();

    } catch (error) {
        console.error('API Error:', error);
        showStatus(panelType, '❌ Error: ' + error.message);
        return null;
    }
}

// Test connection when page loads
document.addEventListener('DOMContentLoaded', () => {
    testBackendConnection();
    bindFileInputs();
    initTabs();
    initButtons();
    console.log('SimilarityLens Lab ready!');
});
// Image state per panel
const state = {
    serial: { imgA: null, imgB: null, fileA: null, fileB: null },
    multi: { imgA: null, imgB: null, fileA: null, fileB: null },
    hybrid: { imgA: null, imgB: null, fileA: null, fileB: null },
    edge: { imgA: null, imgB: null, fileA: null, fileB: null },
    gifParallel: { imgA: null, imgB: null, fileA: null, fileB: null }
};

// Helper: load image preview from file
function loadImagePreview(file, previewElement) {
    return new Promise((resolve, reject) => {
        if (!file) reject('no file');
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                previewElement.innerHTML = `<img src="${img.src}" style="max-height:220px">`;
                resolve(img);
            };
            img.onerror = reject;
            img.src = e.target.result;
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

// Bind file inputs for all panels
function bindFileInputs() {
    const fileInputs = document.querySelectorAll('.file-upload');
    fileInputs.forEach(input => {
        input.addEventListener('change', async (e) => {
            const panel = input.getAttribute('data-panel');
            const side = input.getAttribute('data-side');
            const file = e.target.files[0];
            
            if (file) {
                state[panel][side === 'a' ? 'fileA' : 'fileB'] = file;
                const previewId = `preview${panel.charAt(0).toUpperCase() + panel.slice(1)}${side.toUpperCase()}`;
                const previewDiv = document.getElementById(previewId);
                await loadImagePreview(file, previewDiv);
            }
        });
    });
}

// Show status message for specific panel
function showStatus(panelType, message) {
    const statusMap = {
        serial: 'serialStatusMsg',
        multi: 'multiStatusMsg',
        hybrid: 'hybridStatusMsg',
        edge: 'edgeStatusMsg'
    };
    const statusId = statusMap[panelType];
    if (statusId) {
        document.getElementById(statusId).innerHTML = message;
    }
}

// ==================== SERIAL SSIM ====================
async function runSerial() {
    if (!state.serial.fileA || !state.serial.fileB) {
        alert('Please upload both images first');
        return;
    }
    
    showStatus('serial', '🔄 Computing Serial SSIM...');
    
    const formData = new FormData();
    formData.append('image1', state.serial.fileA);
    formData.append('image2', state.serial.fileB);
    const start = performance.now();
    const result = await callAPI('/compare/serial', formData, 'serial');
    const end = performance.now();
    const clientTime = end - start;
    if (result) {
        document.getElementById('serialSsimVal').innerHTML = result.ssim;
        document.getElementById('serialPercent').innerHTML = result.percent + '%';
        document.getElementById('serialTime').innerHTML = clientTime.toFixed(2) + ' ms';
        document.getElementById('serialProgressFill').style.width = result.percent + '%';
        showStatus('serial', '✅ Serial SSIM complete! Structural fidelity computed.');
    }
}

// ==================== MULTI-THREAD SSIM ====================
async function runMulti() {
    if (!state.multi.fileA || !state.multi.fileB) {
        alert('Please upload both images first');
        return;
    }
    
    showStatus('multi', '🔄 Spawning threads & computing parallel SSIM...');
    
    const formData = new FormData();
    formData.append('image1', state.multi.fileA);
    formData.append('image2', state.multi.fileB);
    const start = performance.now();
    const result = await callAPI('/compare/multi', formData, 'multi');
    const end = performance.now();
    const clientTime = end - start;
    if (result) {
        document.getElementById('multiSsimVal').innerHTML = result.ssim;
        const boost = ((clientTime / 1000)+1).toFixed(2);
        document.getElementById('multiBoost').innerHTML = `∼ ${boost}x`;
        document.getElementById('multiProgressFill').style.width = result.percent + '%';
        document.getElementById('multiTime').innerHTML = clientTime.toFixed(2) + ' ms';
        showStatus('multi', '✅ Multi-thread SSIM complete! Parallel processing finished.');
        
        // Animate threads
        const threads = document.querySelectorAll('#multiThreadAnimation .thread-chip');
        threads.forEach((thread, i) => {
            setTimeout(() => {
                thread.style.animation = 'none';
                setTimeout(() => {
                    thread.style.animation = 'softPulse 0.5s ease';
                }, 10);
            }, i * 100);
        });
    }
}

// ==================== HYBRID SSIM + PERCEPTUAL HASH ====================
async function runHybrid() {

    if (!state.hybrid.fileA || !state.hybrid.fileB) {
        alert('Please upload both files first');
        return;
    }

    const text1 = await state.hybrid.fileA.text();
    const text2 = await state.hybrid.fileB.text();

    showStatus('hybrid', '🔄 Comparing documents...');
    const start = performance.now();
    const response = await fetch('/compare/hybrid', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            text1: text1,
            text2: text2
        })
    });

    const result = await response.json();
    const end = performance.now();
    const clientTime = end - start;
    document.getElementById('hybridSsim').innerHTML = result.cosine;
    document.getElementById('phashScore').innerHTML = result.percent + '%';
    document.getElementById('histogramDiff').innerHTML = 'Text Based';
    document.getElementById('hybridFillBar').style.width = result.percent + '%';
    document.getElementById('hybridTime').innerHTML = clientTime.toFixed(2) + ' ms';

    showStatus('hybrid', '✅ Document similarity analysis complete!');
}

// ==================== EDGE FEATURE MAPPER ====================
async function runEdge() {
    if (!state.edge.fileA || !state.edge.fileB) {
        alert('Please upload both GIF'+'s first');
        return;
    }
    
    showStatus('edge', '🔄 Computing GIF Similarity %');
    
    const formData = new FormData();
    formData.append('gif1', state.edge.fileA);
    formData.append('gif2', state.edge.fileB);
    
    const start = performance.now();
    const result = await callAPI('/compare/edge', formData, 'edge');
    const end = performance.now();
    const clientTime = end - start;

    if (result) {
    document.getElementById('edgeSimValue').innerHTML = result.gifSimilarity;
    document.getElementById('gradientCoherence').innerHTML = result.percent + '%';
    document.getElementById('edgeProgressFill').style.width = result.percent + '%';
    document.getElementById('edgeTime').innerHTML = clientTime.toFixed(2) + ' ms';

    showStatus('edge','✅ GIF sequential comparison complete!');
    }
}
//=====================GIF PARALLEL=======================
async function runGifParallel() {

    if (!state.gifParallel.fileA || !state.gifParallel.fileB) {
        alert('Please upload both GIFs first');
        return;
    }

    showStatus('gifParallel', '⚡ Running parallel GIF analysis...');

    const formData = new FormData();
    formData.append('gif1', state.gifParallel.fileA);
    formData.append('gif2', state.gifParallel.fileB);

    const start = performance.now();

    const result = await callAPI('/compare/edge-parallel', formData, 'gifParallel');

    const end = performance.now();
    const clientTime = end - start;

    if (result) {

        // similarity
        document.getElementById('gifParallelSim').innerHTML = result.gifSimilarity;

        document.getElementById('gifParallelFill').style.width = result.percent + '%';

        const boost = ((clientTime / 1000)+1).toFixed(2);

        document.getElementById('gifParallelBoost').innerHTML = `∼ ${boost}x`;

        document.getElementById('gifParallelTime').innerHTML = clientTime.toFixed(2) + ' ms';

        showStatus('gifParallel','⚡ Parallel GIF analysis complete!');
    }
}


// ==================== TAB SWITCHING ====================
function initTabs() {
    const tabs = document.querySelectorAll('.tab-trigger');
    const panels = document.querySelectorAll('.algorithm-panel');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const targetPanel = tab.getAttribute('data-panel');
            
            panels.forEach(panel => panel.classList.remove('active-panel'));
            tabs.forEach(t => t.classList.remove('active'));
            
            document.getElementById(targetPanel).classList.add('active-panel');
            tab.classList.add('active');
            
            // Update URL hash for deep linking
            window.location.hash = targetPanel;
        });
    });
    
    // Handle hash on load
    if (window.location.hash) {
        const panelId = window.location.hash.substring(1);
        const targetPanel = document.getElementById(panelId);
        if (targetPanel) {
            document.querySelectorAll('.algorithm-panel').forEach(p => p.classList.remove('active-panel'));
            document.querySelectorAll('.tab-trigger').forEach(t => t.classList.remove('active'));
            targetPanel.classList.add('active-panel');
            const activeBtn = document.querySelector(`[data-panel="${panelId}"]`);
            if (activeBtn) activeBtn.classList.add('active');
        }
    }
}

// ==================== BUTTON HANDLERS ====================
function initButtons() {
    const serialBtn = document.querySelector('[data-mode="serial"]');
    const multiBtn = document.querySelector('[data-mode="multi"]');
    const hybridBtn = document.querySelector('[data-mode="hybrid"]');
    const edgeBtn = document.querySelector('[data-mode="edge"]');
    const gifParallelBtn = document.querySelector('[data-mode="gifParallel"]');
    
    if (serialBtn) serialBtn.addEventListener('click', runSerial);
    if (multiBtn) multiBtn.addEventListener('click', runMulti);
    if (hybridBtn) hybridBtn.addEventListener('click', runHybrid);
    if (edgeBtn) edgeBtn.addEventListener('click', runEdge);
    if (gifParallelBtn) gifParallelBtn.addEventListener('click', runGifParallel);
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    bindFileInputs();
    initTabs();
    initButtons();
    console.log('SimilarityLens Lab ready!');
});







// Initialize collaboration when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // ... your existing initialization code ...
    
    // Initialize WebSocket collaboration
    if (typeof initCollaboration === 'function') {
        initCollaboration();
    }
    
    // Add button handlers for collaboration
    const shareBtn = document.getElementById('shareImageBtn');
    const requestBtn = document.getElementById('requestCompareBtn');
    
    if (shareBtn) {
        shareBtn.addEventListener('click', () => {
            const panel = getCurrentActivePanel();
            const side = prompt('Which image to share? (left/right)', 'left');
            if (side && window.state && window.state[panel]) {
                const file = side === 'left' ? window.state[panel].fileA : window.state[panel].fileB;
                if (file) {
                    collaboration.sendImage(file, side);
                } else {
                    alert('No image uploaded on that side');
                }
            }
        });
    }
    
    if (requestBtn) {
        requestBtn.addEventListener('click', () => {
            const method = prompt('Comparison method? (serial/multi/hybrid/edge)', 'serial');
            if (method) {
                collaboration.requestComparison(method);
            }
        });
    }
});