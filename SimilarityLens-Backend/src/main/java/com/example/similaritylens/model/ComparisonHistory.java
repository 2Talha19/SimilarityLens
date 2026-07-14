package com.example.similaritylens.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comparison_history")
public class ComparisonHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id")
    private String roomId;
    
    @Column(name = "initiator_session")
    private String initiatorSession;
    
    @Column(name = "partner_session")
    private String partnerSession;
    
    @Column(name = "algorithm")
    private String algorithm;
    
    @Column(name = "left_filename")
    private String leftFilename;
    
    @Column(name = "right_filename")
    private String rightFilename;
    
    @Column(name = "similarity_score")
    private Double similarityScore;
    
    @Column(name = "similarity_percent")
    private Double similarityPercent;
    
    @Column(name = "boost_factor")
    private String boostFactor;
    
    @Column(name = "comparison_date")
    private LocalDateTime comparisonDate;
    
    // NEW FIELDS
    @Column(name = "left_source")
    private String leftSource; // "local" or "partner"
    
    @Column(name = "right_source")
    private String rightSource; // "local" or "partner"
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getInitiatorSession() { return initiatorSession; }
    public void setInitiatorSession(String initiatorSession) { this.initiatorSession = initiatorSession; }
    
    public String getPartnerSession() { return partnerSession; }
    public void setPartnerSession(String partnerSession) { this.partnerSession = partnerSession; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public String getLeftFilename() { return leftFilename; }
    public void setLeftFilename(String leftFilename) { this.leftFilename = leftFilename; }
    
    public String getRightFilename() { return rightFilename; }
    public void setRightFilename(String rightFilename) { this.rightFilename = rightFilename; }
    
    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    
    public Double getSimilarityPercent() { return similarityPercent; }
    public void setSimilarityPercent(Double similarityPercent) { this.similarityPercent = similarityPercent; }
    
    public String getBoostFactor() { return boostFactor; }
    public void setBoostFactor(String boostFactor) { this.boostFactor = boostFactor; }
    
    public LocalDateTime getComparisonDate() { return comparisonDate; }
    public void setComparisonDate(LocalDateTime comparisonDate) { this.comparisonDate = comparisonDate; }
    
    public String getLeftSource() { return leftSource; }
    public void setLeftSource(String leftSource) { this.leftSource = leftSource; }
    
    public String getRightSource() { return rightSource; }
    public void setRightSource(String rightSource) { this.rightSource = rightSource; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}