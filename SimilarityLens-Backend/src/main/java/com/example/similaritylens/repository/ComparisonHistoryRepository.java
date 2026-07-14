package com.example.similaritylens.repository;

import com.example.similaritylens.model.ComparisonHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComparisonHistoryRepository extends JpaRepository<ComparisonHistory, Long> {
    
    // Find by room ID
    List<ComparisonHistory> findByRoomIdOrderByComparisonDateDesc(String roomId);
    
    // Find all ordered by date descending (for recent comparisons)
    @Query("SELECT c FROM ComparisonHistory c ORDER BY c.comparisonDate DESC")
    List<ComparisonHistory> findAllByOrderByComparisonDateDesc();
    
    // Get average similarity
    @Query("SELECT AVG(c.similarityPercent) FROM ComparisonHistory c")
    Double getAverageSimilarity();
}