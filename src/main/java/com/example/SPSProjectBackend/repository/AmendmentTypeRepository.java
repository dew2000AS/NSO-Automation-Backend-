package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.AmendmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmendmentTypeRepository extends JpaRepository<AmendmentType, AmendmentType.AmendmentTypeId> {
    // Inherited methods from JpaRepository:
    // - List<AmendmentType> findAll()
    // - AmendmentType save(AmendmentType entity)
    // - boolean existsById(AmendmentType.AmendmentTypeId id)
    // - Optional<AmendmentType> findById(AmendmentType.AmendmentTypeId id)

    // Custom finder method to get all records for a specific amendment type
    List<AmendmentType> findByAmendmentType(String amendmentType);
}
