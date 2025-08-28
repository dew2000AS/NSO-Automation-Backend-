package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class SecInfoLoginDTO {
    
    // Login Request DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("password")
        private String password;
    }
    
    // Login Response DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_info")
        private UserInfo userInfo;
        
        @JsonProperty("login_time")
        private LocalDateTime loginTime;
        
        @JsonProperty("expires_at")
        private LocalDateTime expiresAt;
    }
    
    // Updated User Info DTO for response with location codes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("user_name")
        private String userName;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        // Add location fields based on user category
        @JsonProperty("region_code")
        private String regionCode;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        // Constructor without location codes (for backward compatibility)
        public UserInfo(String userId, String userName, String userCategory) {
            this.userId = userId;
            this.userName = userName;
            this.userCategory = userCategory;
        }
    }
    
    // Logout Request DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
    }
    
    // Logout Response DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("logout_time")
        private LocalDateTime logoutTime;
    }
    
    // Session Validation Request DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionValidationRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
    }
    
    // Updated Session Validation Response DTO with location codes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionValidationResponse {
        @JsonProperty("valid")
        private Boolean valid;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("user_info")
        private UserInfo userInfo;
        
        @JsonProperty("last_access_time")
        private LocalDateTime lastAccessTime;
        
        @JsonProperty("expires_at")
        private LocalDateTime expiresAt;
    }
}