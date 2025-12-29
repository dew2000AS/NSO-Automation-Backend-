package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Main DTO class - now only contains the nested static classes
public class ErrorStatisticsDTO {
    
    // No fields in the outer class, only nested static classes
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatsResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("error_statistics")
        private ErrorStatisticsData errorStatistics;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatisticsData {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;
        
        @JsonProperty("total_accounts_with_errors")
        private Integer totalAccountsWithErrors;
        
        @JsonProperty("total_error_instances")
        private Integer totalErrorInstances;
        
        @JsonProperty("error_counts")
        private Map<String, ErrorCountDTO> errorCounts;
        
        @JsonProperty("unread_accounts_count")
        private Integer unreadAccountsCount;
        
        @JsonProperty("total_accounts_in_area")
        private Integer totalAccountsInArea;
        
        @JsonProperty("error_percentage")
        private Double errorPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorCountDTO {
        @JsonProperty("error_code")
        private Integer errorCode;
        
        @JsonProperty("error_name")
        private String errorName;
        
        @JsonProperty("account_count")
        private Integer accountCount;
        
        @JsonProperty("instance_count")
        private Integer instanceCount;
        
        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatsRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetailsResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("error_code")
        private Integer errorCode;
        
        @JsonProperty("error_name")
        private String errorName;
        
        @JsonProperty("accounts_with_error")
        private List<AccountErrorDetailsDTO> accountsWithError;
        
        @JsonProperty("total_accounts")
        private Integer totalAccounts;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountErrorDetailsDTO {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("customer_name")
        private String customerName;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("error_instances")
        private List<ErrorInstanceDTO> errorInstances;
        
        @JsonProperty("total_error_instances")
        private Integer totalErrorInstances;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInstanceDTO {
        @JsonProperty("meter_type")
        private String meterType;
        
        @JsonProperty("error_code")
        private Integer errorCode;
        
        @JsonProperty("error_name")
        private String errorName;
        
        @JsonProperty("reading_date")
        private String readingDate;
        
        @JsonProperty("present_reading")
        private Integer presentReading;
        
        @JsonProperty("previous_reading")
        private Integer previousReading;
        
        @JsonProperty("units")
        private Integer units;
    }
}