package com.example.similaritylens;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ComparisonWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    
    // Store active sessions and their paired partners
    private final Map<String, String> sessionPairs = new ConcurrentHashMap<>();
    private final Map<String, String> waitingSessions = new ConcurrentHashMap<>();

    public ComparisonWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/register")
    @SendTo("/topic/registered")
    public RegistrationResponse register(@Payload RegistrationRequest request) {
        String sessionId = request.getSessionId();
        System.out.println("Session registered: " + sessionId);
        
        // Check if someone is waiting
        if (!waitingSessions.isEmpty()) {
            String partnerId = waitingSessions.keySet().iterator().next();
            waitingSessions.remove(partnerId);
            
            sessionPairs.put(sessionId, partnerId);
            sessionPairs.put(partnerId, sessionId);
            
            // Notify both that they are paired
            messagingTemplate.convertAndSendToUser(
                sessionId, "/queue/paired", 
                new PairingResponse(partnerId, true)
            );
            messagingTemplate.convertAndSendToUser(
                partnerId, "/queue/paired",
                new PairingResponse(sessionId, true)
            );
            
            return new RegistrationResponse(sessionId, partnerId, "paired");
        } else {
            waitingSessions.put(sessionId, "");
            return new RegistrationResponse(sessionId, null, "waiting");
        }
    }

    @MessageMapping("/image")
    public void handleImage(@Payload ImageMessage message) {
        String partnerId = sessionPairs.get(message.getFromSession());
        
        if (partnerId != null) {
            messagingTemplate.convertAndSendToUser(
                partnerId, 
                "/queue/image",
                message
            );
        }
    }

    @MessageMapping("/compare/request")
    public void requestComparison(@Payload ComparisonRequest request) {
        String partnerId = sessionPairs.get(request.getFromSession());
        
        if (partnerId != null) {
            messagingTemplate.convertAndSendToUser(
                partnerId,
                "/queue/compare/request",
                request
            );
        }
    }

    @MessageMapping("/compare/result")
    public void sendComparisonResult(@Payload ComparisonResult result) {
        String partnerId = sessionPairs.get(result.getForSession());
        
        if (partnerId != null) {
            messagingTemplate.convertAndSendToUser(
                partnerId,
                "/queue/compare/result",
                result
            );
        }
    }

    @MessageMapping("/disconnect")
    public void handleDisconnect(@Payload DisconnectMessage message) {
        String sessionId = message.getSessionId();
        String partnerId = sessionPairs.remove(sessionId);
        waitingSessions.remove(sessionId);
        
        if (partnerId != null) {
            sessionPairs.remove(partnerId);
            messagingTemplate.convertAndSendToUser(
                partnerId,
                "/queue/partner/disconnected",
                new DisconnectMessage(partnerId, "Partner disconnected")
            );
        }
    }
}

// DTO Classes (you can put these in separate files or keep them here)
class RegistrationRequest {
    private String sessionId;
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}

class RegistrationResponse {
    private String sessionId;
    private String partnerId;
    private String status;
    
    public RegistrationResponse(String sessionId, String partnerId, String status) {
        this.sessionId = sessionId;
        this.partnerId = partnerId;
        this.status = status;
    }
    
    public String getSessionId() { return sessionId; }
    public String getPartnerId() { return partnerId; }
    public String getStatus() { return status; }
}

class PairingResponse {
    private String partnerId;
    private boolean paired;
    
    public PairingResponse(String partnerId, boolean paired) {
        this.partnerId = partnerId;
        this.paired = paired;
    }
    
    public String getPartnerId() { return partnerId; }
    public boolean isPaired() { return paired; }
}

class ImageMessage {
    private String fromSession;
    private String imageData;
    private String side;
    private String filename;
    
    public String getFromSession() { return fromSession; }
    public void setFromSession(String fromSession) { this.fromSession = fromSession; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}

class ComparisonRequest {
    private String fromSession;
    private String method;
    
    public String getFromSession() { return fromSession; }
    public void setFromSession(String fromSession) { this.fromSession = fromSession; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}

class ComparisonResult {
    private String forSession;
    private Map<String, Object> results;
    
    public String getForSession() { return forSession; }
    public void setForSession(String forSession) { this.forSession = forSession; }
    public Map<String, Object> getResults() { return results; }
    public void setResults(Map<String, Object> results) { this.results = results; }
}

class DisconnectMessage {
    private String sessionId;
    private String message;
    
    public DisconnectMessage(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }
    
    public String getSessionId() { return sessionId; }
    public String getMessage() { return message; }
}