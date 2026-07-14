package com.example.similaritylens.repository;

import com.example.similaritylens.model.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {
    
    List<SharedFile> findByRoomIdOrderBySharedAtDesc(String roomId);
    
    List<SharedFile> findByRoomIdAndSide(String roomId, String side);
    
    // Find all ordered by shared date
    @Query("SELECT s FROM SharedFile s ORDER BY s.sharedAt DESC")
    List<SharedFile> findAllByOrderBySharedAtDesc();
    
    // Find latest file for a room and side
    SharedFile findTopByRoomIdAndSideOrderBySharedAtDesc(String roomId, String side);
}