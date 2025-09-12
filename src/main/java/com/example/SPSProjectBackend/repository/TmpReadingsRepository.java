package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpReadingsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TmpReadingsRepository extends JpaRepository<TmpReadings, TmpReadingsId> {
    
    // Find all readings by account number
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbr(@Param("accNbr") String accNbr);
    
    // Find readings by account number and reading date
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate = :rdngDate ORDER BY t.mtrType")
    List<TmpReadings> findByAccNbrAndRdngDate(@Param("accNbr") String accNbr, @Param("rdngDate") Date rdngDate);
    
    // Find readings by area code
    @Query("SELECT t FROM TmpReadings t WHERE t.areaCd = :areaCd ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByAreaCd(@Param("areaCd") String areaCd);
    
    // Find readings by meter number
    @Query("SELECT t FROM TmpReadings t WHERE t.mtrNbr = :mtrNbr ORDER BY t.rdngDate DESC")
    List<TmpReadings> findByMtrNbr(@Param("mtrNbr") String mtrNbr);
    
    // Find readings by date range
    @Query("SELECT t FROM TmpReadings t WHERE t.rdngDate BETWEEN :startDate AND :endDate ORDER BY t.accNbr, t.rdngDate")
    List<TmpReadings> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find readings by account and date range
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate BETWEEN :startDate AND :endDate ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrAndDateRange(@Param("accNbr") String accNbr, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find latest readings for an account
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate = (SELECT MAX(t2.rdngDate) FROM TmpReadings t2 WHERE t2.accNbr = :accNbr)")
    List<TmpReadings> findLatestReadingsByAccNbr(@Param("accNbr") String accNbr);
    
    // Find readings by user ID
    @Query("SELECT t FROM TmpReadings t WHERE t.userId = :userId ORDER BY t.enteredDtime DESC")
    List<TmpReadings> findByUserId(@Param("userId") String userId);
    
    // Find readings by bill status
    @Query("SELECT t FROM TmpReadings t WHERE t.billStat = :billStat ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByBillStat(@Param("billStat") String billStat);
    
    // Find readings with errors
    @Query("SELECT t FROM TmpReadings t WHERE t.errStat > 0 ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findReadingsWithErrors();
    
    // Check if reading exists
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.areaCd = :areaCd AND t.addedBlcy = :addedBlcy AND t.mtrSeq = :mtrSeq AND t.mtrType = :mtrType AND t.rdngDate = :rdngDate")
    boolean existsReading(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                         @Param("addedBlcy") String addedBlcy, @Param("mtrSeq") Integer mtrSeq,
                         @Param("mtrType") String mtrType, @Param("rdngDate") Date rdngDate);
    
    // Find specific reading
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.areaCd = :areaCd AND t.addedBlcy = :addedBlcy AND t.mtrSeq = :mtrSeq AND t.mtrType = :mtrType AND t.rdngDate = :rdngDate")
    Optional<TmpReadings> findSpecificReading(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                                             @Param("addedBlcy") String addedBlcy, @Param("mtrSeq") Integer mtrSeq,
                                             @Param("mtrType") String mtrType, @Param("rdngDate") Date rdngDate);
    
    // Get distinct account numbers
    @Query("SELECT DISTINCT t.accNbr FROM TmpReadings t ORDER BY t.accNbr")
    List<String> findDistinctAccNbrs();
    
    // Get distinct meter types
    @Query("SELECT DISTINCT t.mtrType FROM TmpReadings t ORDER BY t.mtrType")
    List<String> findDistinctMtrTypes();
}