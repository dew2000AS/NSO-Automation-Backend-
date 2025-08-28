package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccSecInfoDTO {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("passwd")
    private String passwd;
    
    @JsonProperty("user_cat")
    private String userCat;
    
    @JsonProperty("region_code")
    private String regionCode;
    
    @JsonProperty("province_code")
    private String provinceCode;
    
    @JsonProperty("area_code")
    private String areaCode;
    
    // Alternative constructor without password (for security)
    public UserAccSecInfoDTO(String userId, String userName, String userCat, 
                            String regionCode, String provinceCode, String areaCode) {
        this.userId = userId;
        this.userName = userName;
        this.userCat = userCat;
        this.regionCode = regionCode;
        this.provinceCode = provinceCode;
        this.areaCode = areaCode;
    }
}