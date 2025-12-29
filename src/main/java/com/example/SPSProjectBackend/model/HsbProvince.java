package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provinces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbProvince {
    
    @Id
    @Column(name = "prov_code", length = 2, nullable = false)
    private String provCode;
    
    @Column(name = "prov_name", length = 20, nullable = false)
    private String provName;
}