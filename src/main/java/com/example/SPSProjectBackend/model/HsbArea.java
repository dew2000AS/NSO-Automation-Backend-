package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbArea {
    
    @Id
    @Column(name = "area_code", length = 2, nullable = false)
    private String areaCode;
    
    @Column(name = "prov_code", length = 2, nullable = false)
    private String provCode;
    
    @Column(name = "area_name", length = 20, nullable = false)
    private String areaName;
    
    @Column(name = "address_l1", length = 40)
    private String addressL1;
    
    @Column(name = "address_l2", length = 40)
    private String addressL2;
    
    @Column(name = "address_l3", length = 25)
    private String addressL3;
    
    @Column(name = "ar_tel_1", length = 12)
    private String arTel1;
    
    @Column(name = "ar_tel_2", length = 12)
    private String arTel2;
    
    @Column(name = "ar_tel_3", length = 12)
    private String arTel3;
    
    @Column(name = "ar_fax_1", length = 13)
    private String arFax1;
    
    @Column(name = "ar_fax_2", length = 13)
    private String arFax2;
    
    @Column(name = "ar_email", length = 75)
    private String arEmail;
    
    @Column(name = "region", length = 2)
    private String region;
    
    @Column(name = "ar_email_2", length = 75)
    private String arEmail2;
    
    // Many-to-one relationship with Province
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prov_code", referencedColumnName = "prov_code", insertable = false, updatable = false)
    private HsbProvince province;
}