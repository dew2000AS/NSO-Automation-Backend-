package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO;
import com.example.SPSProjectBackend.service.ErrorStatisticsService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/error-statistics")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ErrorStatisticsController {

    @Autowired
    private ErrorStatisticsService errorStatisticsService;

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Get error statistics for bar chart for a specific area
     */
    @GetMapping("/area/{areaCd}")
    public ResponseEntity<?> getErrorStatisticsByArea(
            @PathVariable String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        
        try {
            // Validate session and access
            if (session_id != null && user_id != null) {
                validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            }

            ErrorStatisticsDTO.ErrorStatisticsResponse response = 
                errorStatisticsService.getErrorStatisticsByArea(areaCd);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve error statistics: " + e.getMessage());
            errorResponse.put("area_code", areaCd);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get error statistics for bar chart for all areas (Admin view)
     */
    @GetMapping("/all-areas")
    public ResponseEntity<?> getErrorStatisticsAllAreas(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        
        try {
            // Validate session (Admin access required)
            if (session_id != null && user_id != null) {
                validateSessionAndAccess(session_id, user_id, null, null, null);
            }

            ErrorStatisticsDTO.ErrorStatisticsResponse response = 
                errorStatisticsService.getErrorStatisticsAllAreas();
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve error statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get detailed account information for a specific error type in an area
     */
    @GetMapping("/area/{areaCd}/error/{errorCode}")
    public ResponseEntity<?> getErrorDetailsByAreaAndType(
            @PathVariable String areaCd,
            @PathVariable Integer errorCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        
        try {
            // Validate session and access
            if (session_id != null && user_id != null) {
                validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            }

            // Validate error code
            if (errorCode < 1 || errorCode > 7) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid error code. Must be between 1 and 7.");
                errorResponse.put("error_code", errorCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ErrorStatisticsDTO.ErrorDetailsResponse response = 
                errorStatisticsService.getErrorDetailsByAreaAndType(areaCd, errorCode);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve error details: " + e.getMessage());
            errorResponse.put("area_code", areaCd);
            errorResponse.put("error_code", errorCode);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get detailed account information for a specific error type across all areas (Admin view)
     */
    @GetMapping("/all-areas/error/{errorCode}")
    public ResponseEntity<?> getErrorDetailsAllAreas(
            @PathVariable Integer errorCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        
        try {
            // Validate session (Admin access required)
            if (session_id != null && user_id != null) {
                validateSessionAndAccess(session_id, user_id, null, null, null);
            }

            // Validate error code
            if (errorCode < 1 || errorCode > 7) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid error code. Must be between 1 and 7.");
                errorResponse.put("error_code", errorCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ErrorStatisticsDTO.ErrorDetailsResponse response = 
                errorStatisticsService.getErrorDetailsAllAreas(errorCode);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve error details: " + e.getMessage());
            errorResponse.put("error_code", errorCode);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get available error types with descriptions
     */
    @GetMapping("/error-types")
    public ResponseEntity<?> getErrorTypes() {
        try {
            Map<String, Object> errorTypes = errorStatisticsService.getErrorTypes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Error types retrieved successfully");
            response.put("error_types", errorTypes);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve error types: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to validate session and access
     */
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null) {
            // Validate session
            SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
            validationRequest.setSessionId(sessionId);
            validationRequest.setUserId(userId);
            SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);
            if (!validationResponse.getValid()) {
                throw new RuntimeException("Invalid or expired session");
            }

            // If area-specific, validate access
            if (targetAreaCode != null) {
                boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, targetRegionCode, targetProvinceCode, targetAreaCode);
                if (!hasAccess) {
                    throw new RuntimeException("Access denied to area " + targetAreaCode);
                }
            }
        }
    }
}