package com.example.similaritylens.service;

import com.example.similaritylens.model.*;
import com.example.similaritylens.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DatabaseService {
    
    @Autowired
    private SharedFileRepository sharedFileRepository;
    
    @Autowired
    private ComparisonHistoryRepository comparisonHistoryRepository;
    
    // UPDATED: Save shared file with source info
    public void saveSharedFile(String roomId, String sessionId, String side, 
                                String filename, String fileType, String fileData, 
                                String textContent, int fileSize, String source, String sharedBy, String ipAddress) {
        System.out.println("🔵 SAVING TO DATABASE - Room: " + roomId + ", Side: " + side + ", Filename: " + filename + ", Source: " + source);
        
        try {
            SharedFile file = new SharedFile();
            file.setRoomId(roomId);
            file.setSessionId(sessionId);
            file.setSide(side);
            file.setFilename(filename);
            file.setFileType(fileType);
            file.setFileData(fileData);
            file.setFileSize(fileSize);
            file.setTextContent(textContent);
            file.setSharedAt(LocalDateTime.now());
            file.setSource(source); // "local" or "partner"
            file.setSharedBy(sharedBy);
            file.setIpAddress(ipAddress);
            
            sharedFileRepository.save(file);
            System.out.println("✅ SAVED to database: " + filename + " (Source: " + source + ")");
        } catch (Exception e) {
            System.err.println("❌ ERROR saving to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // UPDATED: Save comparison result with source info
    public void saveComparisonResult(String roomId, String initiatorSession, String partnerSession,
                                      String algorithm, String leftFilename, String rightFilename,
                                      Double similarityScore, Double similarityPercent, String boostFactor,
                                      String leftSource, String rightSource, String ipAddress) {
        System.out.println("🔵 SAVING COMPARISON - Room: " + roomId + ", Algorithm: " + algorithm + ", Percent: " + similarityPercent);
        
        try {
            ComparisonHistory history = new ComparisonHistory();
            history.setRoomId(roomId);
            history.setInitiatorSession(initiatorSession);
            history.setPartnerSession(partnerSession);
            history.setAlgorithm(algorithm);
            history.setLeftFilename(leftFilename);
            history.setRightFilename(rightFilename);
            history.setSimilarityScore(similarityScore);
            history.setSimilarityPercent(similarityPercent);
            history.setBoostFactor(boostFactor);
            history.setComparisonDate(LocalDateTime.now());
            history.setLeftSource(leftSource); // "local" or "partner"
            history.setRightSource(rightSource);
            history.setIpAddress(ipAddress);
            
            comparisonHistoryRepository.save(history);
            System.out.println("✅ SAVED comparison: " + algorithm + " - " + similarityPercent + "% (Left: " + leftSource + ", Right: " + rightSource + ")");
        } catch (Exception e) {
            System.err.println("❌ ERROR saving comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Legacy method for backward compatibility (keeps old code working)
    public void saveSharedFile(String roomId, String sessionId, String side, 
                                String filename, String fileType, String fileData, 
                                String textContent, int fileSize) {
        saveSharedFile(roomId, sessionId, side, filename, fileType, fileData, 
                      textContent, fileSize, "unknown", sessionId, "0.0.0.0");
    }
    
    // Legacy method for backward compatibility
    public void saveComparisonResult(String roomId, String initiatorSession, String partnerSession,
                                      String algorithm, String leftFilename, String rightFilename,
                                      Double similarityScore, Double similarityPercent, String boostFactor) {
        saveComparisonResult(roomId, initiatorSession, partnerSession, algorithm, 
                            leftFilename, rightFilename, similarityScore, similarityPercent, 
                            boostFactor, "unknown", "unknown", "0.0.0.0");
    }
    
    public List<ComparisonHistory> getRoomHistory(String roomId) {
        return comparisonHistoryRepository.findByRoomIdOrderByComparisonDateDesc(roomId);
    }
    
    public List<ComparisonHistory> getRecentComparisons() {
        List<ComparisonHistory> all = comparisonHistoryRepository.findAllByOrderByComparisonDateDesc();
        if (all.size() > 50) {
            return all.subList(0, 50);
        }
        return all;
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", sharedFileRepository.count());
        stats.put("totalComparisons", comparisonHistoryRepository.count());
        
        // Get counts by source
        long localFiles = sharedFileRepository.findAll().stream()
            .filter(f -> "local".equals(f.getSource()))
            .count();
        long partnerFiles = sharedFileRepository.findAll().stream()
            .filter(f -> "partner".equals(f.getSource()))
            .count();
        
        stats.put("localFiles", localFiles);
        stats.put("partnerFiles", partnerFiles);
        
        return stats;
    }
    
    public List<SharedFile> getAllFiles() {
        return sharedFileRepository.findAllByOrderBySharedAtDesc();
    }
    
    public Double getAverageSimilarity() {
        List<ComparisonHistory> comparisons = comparisonHistoryRepository.findAll();
        if (comparisons.isEmpty()) {
            return 0.0;
        }
        double total = comparisons.stream().mapToDouble(ComparisonHistory::getSimilarityPercent).sum();
        return total / comparisons.size();
    }
}