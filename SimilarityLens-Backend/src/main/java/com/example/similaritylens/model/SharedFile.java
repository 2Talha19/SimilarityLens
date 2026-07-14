package com.example.similaritylens.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_files")
public class SharedFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id")
    private String roomId;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "side")
    private String side;
    
    @Column(name = "filename")
    private String filename;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_data", columnDefinition = "LONGTEXT")
    private String fileData;
    
    @Column(name = "file_size")
    private Integer fileSize;
    
    @Column(name = "text_content", columnDefinition = "LONGTEXT")
    private String textContent;
    
    @Column(name = "shared_at")
    private LocalDateTime sharedAt;
    
    // NEW FIELDS
    @Column(name = "source")
    private String source; // "local" or "partner"
    
    @Column(name = "shared_by")
    private String sharedBy;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public String getFileData() { return fileData; }
    public void setFileData(String fileData) { this.fileData = fileData; }
    
    public Integer getFileSize() { return fileSize; }
    public void setFileSize(Integer fileSize) { this.fileSize = fileSize; }
    
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    
    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getSharedBy() { return sharedBy; }
    public void setSharedBy(String sharedBy) { this.sharedBy = sharedBy; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}