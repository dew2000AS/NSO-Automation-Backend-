package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ErrorStatisticsDTO {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorCount {
        @JsonProperty("error_type")
        private String errorType;
        
        @JsonProperty("error_code")
        private Integer errorCode;
        
        @JsonProperty("account_count")
        private Long accountCount;
        
        @JsonProperty("total_errors")
        private Long totalErrors;
        
        @JsonProperty("description")
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatisticsResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;
        
        @JsonProperty("total_accounts_with_errors")
        private Long totalAccountsWithErrors;
        
        @JsonProperty("total_error_instances")
        private Long totalErrorInstances;
        
        @JsonProperty("error_counts")
        private List<ErrorCount> errorCounts;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("error_code")
        private Integer errorCode;
        
        @JsonProperty("error_type")
        private String errorType;
        
        @JsonProperty("meter_types")
        private List<String> meterTypes;
        
        @JsonProperty("total_meters_with_error")
        private Integer totalMetersWithError;
        
        @JsonProperty("reading_date")
        private String readingDate;
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
        
        @JsonProperty("error_type")
        private String errorType;
        
        @JsonProperty("total_accounts")
        private Long totalAccounts;
        
        @JsonProperty("account_details")
        private List<ErrorDetails> accountDetails;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}