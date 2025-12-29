package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MonTot;
import com.example.SPSProjectBackend.model.MonTotId; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonTotRepository extends JpaRepository<MonTot, MonTotId> { // Change String to MonTotId
    
    // Find by account number and bill cycle
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr AND m.billCycle = :billCycle")
    Optional<MonTot> findByAccNbrAndBillCycle(@Param("accNbr") String accNbr, @Param("billCycle") String billCycle);
    
    // Find all by account number
    @Query("SELECT m FROM MonTot m WHERE m.accNbr = :accNbr ORDER BY m.billCycle DESC")
    List<MonTot> findByAccNbr(@Param("accNbr") String accNbr);
    
    // Find by area code and bill cycle
    @Query("SELECT m FROM MonTot m WHERE m.billCycle = :billCycle AND m.accNbr IN " +
           "(SELECT c.accNbr FROM BulkCustomer c WHERE c.areaCd = :areaCode)")
    List<MonTot> findByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") String billCycle);
}