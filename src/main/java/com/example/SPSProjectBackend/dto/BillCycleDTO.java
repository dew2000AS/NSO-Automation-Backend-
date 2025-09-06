package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class BillCycleDTO {
    
    // DTO for individual bill cycle configuration
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleConfigDTO {
        @JsonProperty("bill_cycle")
        private Integer billCycle;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("entered_date")
        private LocalDateTime enteredDate;
        
        @JsonProperty("cycle_stat")
        private Integer cycleStat;
        
        @JsonProperty("is_active")
        private Boolean isActive;
    }
    
    // DTO for area with its active bill cycle
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaBillCycleDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("province_name")
        private String provinceName;
        
        @JsonProperty("region_code")
        private String regionCode;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("has_bill_cycle")
        private Boolean hasBillCycle;
    }
    
    // DTO for login response with bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBillCycleInfo {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("user_name")
        private String userName;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        @JsonProperty("bill_cycles")
        private List<AreaBillCycleDTO> billCycles;
        
        @JsonProperty("total_areas")
        private Integer totalAreas;
        
        @JsonProperty("areas_with_cycles")
        private Integer areasWithCycles;
    }
    
    // DTO for bill cycle summary
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleSummaryDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("total_cycles")
        private Integer totalCycles;
        
        @JsonProperty("active_cycles")
        private Integer activeCycles;
        
        @JsonProperty("inactive_cycles")
        private Integer inactiveCycles;
    }
    
    // Request DTO for getting bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("region_code")
        private String regionCode;
    }
    
    // Response DTO for bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        @JsonProperty("bill_cycles")
        private List<AreaBillCycleDTO> billCycles;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
}