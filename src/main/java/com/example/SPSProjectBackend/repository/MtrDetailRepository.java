package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MtrDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MtrDetailRepository extends JpaRepository<MtrDetail, String> {
}
