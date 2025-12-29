package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BillCycleConfigId.class)
public class BillCycleConfig {
    
    @Id
    @Column(name = "bill_cycle", nullable = false)
    private Integer billCycle;
    
    @Id
    @Column(name = "area_code", length = 2, nullable = false)
    private String areaCode;
    
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;
    
    @Column(name = "entered_date")
    private LocalDateTime enteredDate;
    
    @Column(name = "cycle_stat", columnDefinition = "SMALLINT DEFAULT 1")
    private Integer cycleStat;
    
    // Many-to-one relationship with Area
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    private HsbArea area;
}