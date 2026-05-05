package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NcreDeveloperRepository extends JpaRepository<NcreDeveloper, String> {
    Optional<NcreDeveloper> findByAccNbr(String accNbr);
}
