package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorStatisticsRepository extends JpaRepository<TmpReadings, String> {

    /**
     * Get distinct account numbers with errors for an area and bill cycle
     * Counts accounts, not individual error instances
     */
    @Query("SELECT DISTINCT t.accNbr FROM TmpReadings t " +
           "WHERE t.areaCd = :areaCode " +
           "AND t.addedBlcy = :billCycle " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0 " +
           "ORDER BY t.accNbr")
    List<String> findDistinctAccountsWithErrors(@Param("areaCode") String areaCode, 
                                               @Param("billCycle") String billCycle);

    /**
     * Get error statistics grouped by error code for an area and bill cycle
     * Returns count of distinct accounts for each error code
     */
    @Query("SELECT t.errStat, COUNT(DISTINCT t.accNbr) " +
           "FROM TmpReadings t " +
           "WHERE t.areaCd = :areaCode " +
           "AND t.addedBlcy = :billCycle " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0 " +
           "GROUP BY t.errStat " +
           "ORDER BY t.errStat")
    List<Object[]> findErrorStatisticsByAreaAndBillCycle(@Param("areaCode") String areaCode, 
                                                        @Param("billCycle") String billCycle);

    /**
     * Get all error instances for a specific error code in an area and bill cycle
     * Used for detailed error view
     */
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd = :areaCode " +
           "AND t.addedBlcy = :billCycle " +
           "AND t.errStat = :errorCode " +
           "ORDER BY t.accNbr, t.mtrType")
    List<TmpReadings> findErrorInstancesByErrorCode(@Param("areaCode") String areaCode, 
                                                   @Param("billCycle") String billCycle, 
                                                   @Param("errorCode") Integer errorCode);

    /**
     * Get accounts with specific error code including their error instances
     */
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd = :areaCode " +
           "AND t.addedBlcy = :billCycle " +
           "AND t.errStat = :errorCode " +
           "ORDER BY t.accNbr, t.mtrType")
    List<TmpReadings> findAccountsWithErrorCode(@Param("areaCode") String areaCode, 
                                               @Param("billCycle") String billCycle, 
                                               @Param("errorCode") Integer errorCode);

    /**
     * Get total count of error instances (all meter readings with errors)
     */
    @Query("SELECT COUNT(t) FROM TmpReadings t " +
           "WHERE t.areaCd = :areaCode " +
           "AND t.addedBlcy = :billCycle " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0")
    Long countTotalErrorInstances(@Param("areaCode") String areaCode, 
                                 @Param("billCycle") String billCycle);

    /**
     * Get count of accounts without any readings for an area and bill cycle
     */
    @Query("SELECT COUNT(bc) FROM BulkCustomer bc " +
           "WHERE bc.areaCd = :areaCode " +
           "AND bc.accNbr NOT IN (" +
           "    SELECT DISTINCT t.accNbr FROM TmpReadings t " +
           "    WHERE t.areaCd = :areaCode AND t.addedBlcy = :billCycle" +
           ")")
    Long countUnreadAccounts(@Param("areaCode") String areaCode, 
                            @Param("billCycle") String billCycle);

    /**
     * Get total number of accounts in an area
     */
    @Query("SELECT COUNT(bc) FROM BulkCustomer bc WHERE bc.areaCd = :areaCode")
    Long countTotalAccountsInArea(@Param("areaCode") String areaCode);
}