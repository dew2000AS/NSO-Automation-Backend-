package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.BulkCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BulkCustomerRepository extends JpaRepository<BulkCustomer, String> {
    
    // Find customer by account number (primary identifier)
    @Query("SELECT c FROM BulkCustomer c WHERE c.accNbr = :accNbr")
    Optional<BulkCustomer> findByAccNbr(@Param("accNbr") String accNbr);
    
    // Check if customer exists by account number
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM BulkCustomer c WHERE c.accNbr = :accNbr")
    boolean existsByAccNbr(@Param("accNbr") String accNbr);
    
    // Find customers by area code
    @Query("SELECT c FROM BulkCustomer c WHERE c.areaCd = :areaCd ORDER BY c.accNbr")
    List<BulkCustomer> findByAreaCd(@Param("areaCd") String areaCd);
    
    // Find customers by zone
    @Query("SELECT c FROM BulkCustomer c WHERE c.zone = :zone ORDER BY c.accNbr")
    List<BulkCustomer> findByZone(@Param("zone") String zone);
    
    // Find customers by zone and area
    @Query("SELECT c FROM BulkCustomer c WHERE c.zone = :zone AND c.areaCd = :areaCd ORDER BY c.accNbr")
    List<BulkCustomer> findByZoneAndAreaCd(@Param("zone") String zone, @Param("areaCd") String areaCd);
    
    // Find customers by name (partial search)
    @Query("SELECT c FROM BulkCustomer c WHERE UPPER(c.name) LIKE UPPER(:name) ORDER BY c.name")
    List<BulkCustomer> findByNameContaining(@Param("name") String name);
    
    // Find customers by tariff
    @Query("SELECT c FROM BulkCustomer c WHERE c.tariff = :tariff ORDER BY c.accNbr")
    List<BulkCustomer> findByTariff(@Param("tariff") String tariff);
    
    // Find customers by customer category
    @Query("SELECT c FROM BulkCustomer c WHERE c.cusCat = :cusCat ORDER BY c.accNbr")
    List<BulkCustomer> findByCusCat(@Param("cusCat") String cusCat);
    
    // Find customers by operational status
    @Query("SELECT c FROM BulkCustomer c WHERE c.opStat = :opStat ORDER BY c.accNbr")
    List<BulkCustomer> findByOpStat(@Param("opStat") String opStat);
    
    // Find customers by mobile number
    @Query("SELECT c FROM BulkCustomer c WHERE c.mobileNo = :mobileNo")
    Optional<BulkCustomer> findByMobileNo(@Param("mobileNo") String mobileNo);
    
    // Find customers by telephone number
    @Query("SELECT c FROM BulkCustomer c WHERE c.telNbr = :telNbr")
    List<BulkCustomer> findByTelNbr(@Param("telNbr") String telNbr);
    
    // Find customers by city
    @Query("SELECT c FROM BulkCustomer c WHERE UPPER(c.city) = UPPER(:city) ORDER BY c.name")
    List<BulkCustomer> findByCity(@Param("city") String city);
    
    // Find customers by job number
    @Query("SELECT c FROM BulkCustomer c WHERE c.jobNbr = :jobNbr")
    List<BulkCustomer> findByJobNbr(@Param("jobNbr") String jobNbr);
    
    // Find customers by connection date range
    @Query("SELECT c FROM BulkCustomer c WHERE c.cnectDate BETWEEN :startDate AND :endDate ORDER BY c.cnectDate DESC")
    List<BulkCustomer> findByCnectDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find customers by entered date range
    @Query("SELECT c FROM BulkCustomer c WHERE DATE(c.enteredDtime) BETWEEN :startDate AND :endDate ORDER BY c.enteredDtime DESC")
    List<BulkCustomer> findByEnteredDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find customers by user ID
    @Query("SELECT c FROM BulkCustomer c WHERE c.userId = :userId ORDER BY c.enteredDtime DESC")
    List<BulkCustomer> findByUserId(@Param("userId") String userId);
    
    // Find customers by edited user ID
    @Query("SELECT c FROM BulkCustomer c WHERE c.editedUserId = :editedUserId ORDER BY c.editedDtime DESC")
    List<BulkCustomer> findByEditedUserId(@Param("editedUserId") String editedUserId);
    
    // Find customers by bill cycle
    @Query("SELECT c FROM BulkCustomer c WHERE c.billCycle = :billCycle ORDER BY c.accNbr")
    List<BulkCustomer> findByBillCycle(@Param("billCycle") Integer billCycle);
    
    // Find customers with deposits greater than amount
    @Query("SELECT c FROM BulkCustomer c WHERE c.depositAmt > :amount ORDER BY c.depositAmt DESC")
    List<BulkCustomer> findByDepositAmtGreaterThan(@Param("amount") java.math.BigDecimal amount);
    
    // Find customers by customer type
    @Query("SELECT c FROM BulkCustomer c WHERE c.custType = :custType ORDER BY c.accNbr")
    List<BulkCustomer> findByCustType(@Param("custType") String custType);
    
    // Search customers by multiple criteria
    @Query("SELECT c FROM BulkCustomer c WHERE " +
           "(:areaCd IS NULL OR c.areaCd = :areaCd) AND " +
           "(:zone IS NULL OR c.zone = :zone) AND " +
           "(:tariff IS NULL OR c.tariff = :tariff) AND " +
           "(:opStat IS NULL OR c.opStat = :opStat) AND " +
           "(:cusCat IS NULL OR c.cusCat = :cusCat) " +
           "ORDER BY c.accNbr")
    List<BulkCustomer> findByMultipleCriteria(@Param("areaCd") String areaCd,
                                            @Param("zone") String zone,
                                            @Param("tariff") String tariff,
                                            @Param("opStat") String opStat,
                                            @Param("cusCat") String cusCat);
    
    // Get distinct area codes
    @Query("SELECT DISTINCT c.areaCd FROM BulkCustomer c WHERE c.areaCd IS NOT NULL ORDER BY c.areaCd")
    List<String> findDistinctAreaCodes();
    
    // Get distinct zones
    @Query("SELECT DISTINCT c.zone FROM BulkCustomer c WHERE c.zone IS NOT NULL ORDER BY c.zone")
    List<String> findDistinctZones();
    
    // Get distinct tariffs
    @Query("SELECT DISTINCT c.tariff FROM BulkCustomer c ORDER BY c.tariff")
    List<String> findDistinctTariffs();
    
    // Get distinct customer categories
    @Query("SELECT DISTINCT c.cusCat FROM BulkCustomer c WHERE c.cusCat IS NOT NULL ORDER BY c.cusCat")
    List<String> findDistinctCusCategories();
    
    // Get distinct cities
    @Query("SELECT DISTINCT c.city FROM BulkCustomer c WHERE c.city IS NOT NULL ORDER BY c.city")
    List<String> findDistinctCities();
    
    // Count customers by area
    @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.areaCd = :areaCd")
    Long countByAreaCd(@Param("areaCd") String areaCd);
    
    // Count customers by zone
    @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.zone = :zone")
    Long countByZone(@Param("zone") String zone);
    
    // Count customers by operational status
    @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.opStat = :opStat")
    Long countByOpStat(@Param("opStat") String opStat);
    
    // Advanced search with pagination support
    @Query("SELECT c FROM BulkCustomer c WHERE " +
           "(:searchTerm IS NULL OR " +
           "UPPER(c.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.accNbr) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.mobileNo) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
           "UPPER(c.city) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY c.accNbr")
    List<BulkCustomer> searchCustomers(@Param("searchTerm") String searchTerm);

    // method to the existing repository
    @Query("SELECT c FROM BulkCustomer c WHERE c.accNbr IN :accNbrs AND c.areaCd = :areaCd ORDER BY c.accNbr")
    List<BulkCustomer> findByAccNbrInAndAreaCd(@Param("accNbrs") List<String> accNbrs, @Param("areaCd") String areaCd);
}