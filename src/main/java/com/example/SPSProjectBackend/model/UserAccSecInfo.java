package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sec_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccSecInfo {
    
    @Id
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "user_name", length = 20, nullable = false)
    private String userName;

    @Column(name = "passwd", length = 50, nullable = false)
    private String passwd;

    @Column(name = "user_cat", length = 20)
    private String userCat;
    
    @Column(name = "region_code", length = 2)
    private String regionCode;
    
    @Column(name = "province_code", length = 2)
    private String provinceCode;
    
    @Column(name = "area_code", length = 2)
    private String areaCode;
    
    @Column(name = "epf_num", length = 10, nullable = false)
    private String epfNum;
    
    @Column(name = "status", nullable = false, columnDefinition = "SMALLINT DEFAULT 1")
    private Integer status = 1; // Default to active (1)
}