package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SPSProjectBackend.service.BillCycleService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/secinfo")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SecInfoAuthController {

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private BillCycleService billCycleService;

    /**
     * User Login Endpoint - Updated to include location codes based on user category
     * Update the login method to include bill cycles
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody SecInfoLoginDTO.LoginRequest loginRequest, 
                                HttpServletRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get client information
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Authenticate user
            SecInfoLoginDTO.LoginResponse response = secInfoAuthService.authenticateUser(
                loginRequest, ipAddress, userAgent);

            // Convert to Map to avoid Jackson serialization issues
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("session_id", response.getSessionId());
                
                if (response.getUserInfo() != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("user_id", response.getUserInfo().getUserId());
                    userInfo.put("user_name", response.getUserInfo().getUserName());
                    userInfo.put("user_category", response.getUserInfo().getUserCategory());
                    
                    // Add location codes based on user category
                    String userCategory = response.getUserInfo().getUserCategory();
                    
                    if ("Admin".equals(userCategory)) {
                        // Admin users: no location codes
                        // Don't add any location fields
                    } else if ("Region User".equals(userCategory)) {
                        // Region users: region_code only
                        if (response.getUserInfo().getRegionCode() != null) {
                            userInfo.put("region_code", response.getUserInfo().getRegionCode());
                        }
                    } else if ("Province User".equals(userCategory) || "Accountant Revenue".equals(userCategory)|| "Acc Assistance".equals(userCategory)||"Accountant Clark".equals(userCategory)) {
                        // Province users: region_code and province_code
                        if (response.getUserInfo().getRegionCode() != null) {
                            userInfo.put("region_code", response.getUserInfo().getRegionCode());
                        }
                        if (response.getUserInfo().getProvinceCode() != null) {
                            userInfo.put("province_code", response.getUserInfo().getProvinceCode());
                        }
                    } else if ("Area User".equals(userCategory)) {
                        // Area users: all location codes
                        if (response.getUserInfo().getRegionCode() != null) {
                            userInfo.put("region_code", response.getUserInfo().getRegionCode());
                        }
                        if (response.getUserInfo().getProvinceCode() != null) {
                            userInfo.put("province_code", response.getUserInfo().getProvinceCode());
                        }
                        if (response.getUserInfo().getAreaCode() != null) {
                            userInfo.put("area_code", response.getUserInfo().getAreaCode());
                        }
                    }
                    
                    responseMap.put("user_info", userInfo);
                    
                    // Get bill cycles for the user
                    try {
                        BillCycleDTO.BillCycleResponse billCycleResponse = billCycleService.getBillCyclesForUser(
                            response.getSessionId(), response.getUserInfo().getUserId());
                        
                        if (billCycleResponse.getSuccess()) {
                            responseMap.put("bill_cycles", billCycleResponse.getBillCycles());
                            
                            // Add summary
                            int totalAreas = billCycleResponse.getBillCycles().size();
                            long areasWithCycles = billCycleResponse.getBillCycles().stream()
                                    .filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle)
                                    .count();
                            
                            Map<String, Object> billCycleSummary = new HashMap<>();
                            billCycleSummary.put("total_areas", totalAreas);
                            billCycleSummary.put("areas_with_cycles", areasWithCycles);
                            billCycleSummary.put("areas_without_cycles", totalAreas - areasWithCycles);
                            
                            responseMap.put("bill_cycle_summary", billCycleSummary);
                        }
                    } catch (Exception e) {
                        // Log error but don't fail login
                        System.err.println("Failed to get bill cycles during login: " + e.getMessage());
                        responseMap.put("bill_cycles", new ArrayList<>());
                    }
                }
                
                responseMap.put("login_time", response.getLoginTime() != null ? response.getLoginTime().toString() : null);
                responseMap.put("expires_at", response.getExpiresAt() != null ? response.getExpiresAt().toString() : null);
                
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Login failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            // Log the error for debugging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * User Logout Endpoint
     */
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout(@RequestBody SecInfoLoginDTO.LogoutRequest logoutRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SecInfoLoginDTO.LogoutResponse response = secInfoAuthService.logoutUser(logoutRequest);
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("logout_time", response.getLogoutTime() != null ? response.getLogoutTime().toString() : null);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Logout failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Session Validation Endpoint - Updated to include location codes
     */
    @PostMapping(value = "/validate-session", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateSession(@RequestBody SecInfoLoginDTO.SessionValidationRequest validationRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SecInfoLoginDTO.SessionValidationResponse response = secInfoAuthService.validateSession(validationRequest);
            
            responseMap.put("valid", response.getValid());
            responseMap.put("message", response.getMessage());
            
            if (response.getValid() && response.getUserInfo() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("user_id", response.getUserInfo().getUserId());
                userInfo.put("user_name", response.getUserInfo().getUserName());
                userInfo.put("user_category", response.getUserInfo().getUserCategory());
                
                // Add location codes based on user category
                String userCategory = response.getUserInfo().getUserCategory();
                
                if ("Admin".equals(userCategory)) {
                    // Admin users: no location codes
                    // Don't add any location fields
                } else if ("Region User".equals(userCategory)) {
                    // Region users: region_code only
                    if (response.getUserInfo().getRegionCode() != null) {
                        userInfo.put("region_code", response.getUserInfo().getRegionCode());
                    }
                } else if ("Province User".equals(userCategory)|| "Accountant Revenue".equals(userCategory) || "Acc Assistance".equals(userCategory)||"Accountant Clark".equals(userCategory)) {
                    // Province users: region_code and province_code
                    if (response.getUserInfo().getRegionCode() != null) {
                        userInfo.put("region_code", response.getUserInfo().getRegionCode());
                    }
                    if (response.getUserInfo().getProvinceCode() != null) {
                        userInfo.put("province_code", response.getUserInfo().getProvinceCode());
                    }
                } else if ("Area User".equals(userCategory)) {
                    // Area users: all location codes
                    if (response.getUserInfo().getRegionCode() != null) {
                        userInfo.put("region_code", response.getUserInfo().getRegionCode());
                    }
                    if (response.getUserInfo().getProvinceCode() != null) {
                        userInfo.put("province_code", response.getUserInfo().getProvinceCode());
                    }
                    if (response.getUserInfo().getAreaCode() != null) {
                        userInfo.put("area_code", response.getUserInfo().getAreaCode());
                    }
                }
                
                responseMap.put("user_info", userInfo);
                responseMap.put("last_access_time", response.getLastAccessTime() != null ? response.getLastAccessTime().toString() : null);
                responseMap.put("expires_at", response.getExpiresAt() != null ? response.getExpiresAt().toString() : null);
            }
            
            if (response.getValid()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("valid", false);
            responseMap.put("message", "Session validation failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get user location data from session - New endpoint for global access
     */
    @PostMapping(value = "/get-user-location", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserLocationFromSession(@RequestBody Map<String, String> request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            String sessionId = request.get("session_id");
            String userId = request.get("user_id");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            var userLocationOptional = secInfoAuthService.getUserLocationFromSession(sessionId, userId);
            
            if (userLocationOptional.isPresent()) {
                SecInfoLoginDTO.UserInfo userInfo = userLocationOptional.get();
                
                Map<String, Object> locationData = new HashMap<>();
                locationData.put("user_id", userInfo.getUserId());
                locationData.put("user_name", userInfo.getUserName());
                locationData.put("user_category", userInfo.getUserCategory());
                
                // Add location codes based on user category
                String userCategory = userInfo.getUserCategory();
                
                if ("Admin".equals(userCategory)) {
                    // Admin users: no location codes
                    // Don't add any location fields
                } else if ("Region User".equals(userCategory)) {
                    // Region users: region_code only
                    if (userInfo.getRegionCode() != null) {
                        locationData.put("region_code", userInfo.getRegionCode());
                    }
                } else if ("Province User".equals(userCategory)|| "Accountant Revenue".equals(userCategory)|| "Acc Assistance".equals(userCategory)||"Accountant Clark".equals(userCategory)) {
                    // Province users: region_code and province_code
                    if (userInfo.getRegionCode() != null) {
                        locationData.put("region_code", userInfo.getRegionCode());
                    }
                    if (userInfo.getProvinceCode() != null) {
                        locationData.put("province_code", userInfo.getProvinceCode());
                    }
                } else if ("Area User".equals(userCategory)) {
                    // Area users: all location codes
                    if (userInfo.getRegionCode() != null) {
                        locationData.put("region_code", userInfo.getRegionCode());
                    }
                    if (userInfo.getProvinceCode() != null) {
                        locationData.put("province_code", userInfo.getProvinceCode());
                    }
                    if (userInfo.getAreaCode() != null) {
                        locationData.put("area_code", userInfo.getAreaCode());
                    }
                }
                
                responseMap.put("success", true);
                responseMap.put("message", "User location data retrieved successfully");
                responseMap.put("user_location", locationData);
                
                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("success", false);
                responseMap.put("message", "Invalid session or session expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve user location: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Check System Health (Session Store connectivity)
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Try to perform a simple operation to check Redis connectivity
            // We'll do a simple check by trying to validate an empty session (which will fail gracefully)
            SecInfoLoginDTO.SessionValidationRequest testRequest = new SecInfoLoginDTO.SessionValidationRequest();
            testRequest.setSessionId("health-check");
            
            // This will return false but won't throw exception if Redis is working
            secInfoAuthService.validateSession(testRequest);
            
            responseMap.put("status", "healthy");
            responseMap.put("message", "SecInfo authentication system is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "SecInfo authentication system has issues: " + e.getMessage());
            responseMap.put("error", "SYSTEM_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IP addresses (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}