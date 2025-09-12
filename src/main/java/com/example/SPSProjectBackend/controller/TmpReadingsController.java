// Modified: com/example/SPSProjectBackend/controller/TmpReadingsController.java
package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.service.TmpReadingsService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.HsbLocationService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tmp-readings")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TmpReadingsController {

    @Autowired
    private TmpReadingsService tmpReadingsService;

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private SessionUtils sessionUtils;

    // Get all readings - DEPRECATED, use filtered endpoints
    @GetMapping
    public ResponseEntity<?> getAllReadings() {
        try {
            List<TmpReadingsDTO> readings = tmpReadingsService.getAllReadings();
                        return ResponseEntity.ok(readings);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // FIXED: Get readings by account number - Add session validation
    @GetMapping("/account/{accNbr}")
    public ResponseEntity<?> getReadingsByAccNbr(@PathVariable String accNbr,
                                                 @RequestParam(required = false) String session_id,
                                                 @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null); // No area-specific check for account
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsByAccNbr(accNbr);
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings for account");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by account number and date
    @GetMapping("/account/{accNbr}/date/{rdngDate}")
    public ResponseEntity<?> getReadingsByAccNbrAndDate(
            @PathVariable String accNbr,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date rdngDate,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsByAccNbrAndDate(accNbr, rdngDate);
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("reading_date", rdngDate);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get latest readings for account
    @GetMapping("/account/{accNbr}/latest")
    public ResponseEntity<?> getLatestReadingsByAccNbr(@PathVariable String accNbr,
                                                       @RequestParam(required = false) String session_id,
                                                       @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<TmpReadingsDTO> readings = tmpReadingsService.getLatestReadingsByAccNbr(accNbr);
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve latest readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // FIXED: Get readings by area code - Add session validation and access check
    @GetMapping("/area/{areaCd}")
    public ResponseEntity<?> getReadingsByAreaCd(@PathVariable String areaCd,
                                                 @RequestParam(required = false) String session_id,
                                                 @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsByAreaCd(areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCd);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by area code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by meter number
    @GetMapping("/meter/{mtrNbr}")
    public ResponseEntity<?> getReadingsByMtrNbr(@PathVariable String mtrNbr) {
        try {
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsByMtrNbr(mtrNbr);
            Map<String, Object> response = new HashMap<>();
            response.put("meter_number", mtrNbr);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by meter number");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by date range
    @GetMapping("/date-range")
    public ResponseEntity<?> getReadingsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsByDateRange(startDate, endDate);
            Map<String, Object> response = new HashMap<>();
            response.put("start_date", startDate);
            response.put("end_date", endDate);
            response.put("total_readings", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by date range");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get specific reading
    @GetMapping("/specific")
    public ResponseEntity<?> getSpecificReading(
            @RequestParam String accNbr,
            @RequestParam String areaCd,
            @RequestParam String addedBlcy,
            @RequestParam Integer mtrSeq,
            @RequestParam String mtrType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date rdngDate,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            Optional<TmpReadingsDTO> reading = tmpReadingsService.getSpecificReading(
                accNbr, areaCd, addedBlcy, mtrSeq, mtrType, rdngDate);
            
            if (reading.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Reading found");
                response.put("reading", reading.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reading not found");
                error.put("message", "No reading found for the specified criteria");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve specific reading");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Create new reading
    @PostMapping
    public ResponseEntity<?> createReading(@RequestBody TmpReadingsDTO readingDTO) {
        try {
            TmpReadingsDTO createdReading = tmpReadingsService.createReading(readingDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reading created successfully");
            response.put("reading", createdReading);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create reading");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create reading: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update existing reading
    @PutMapping
    public ResponseEntity<?> updateReading(@RequestBody TmpReadingsDTO readingDTO) {
        try {
            TmpReadingsDTO updatedReading = tmpReadingsService.updateReading(readingDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reading updated successfully");
            response.put("reading", updatedReading);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reading not found");
                error.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update reading");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update reading: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings with errors
    @GetMapping("/errors")
    public ResponseEntity<?> getReadingsWithErrors() {
        try {
            List<TmpReadingsDTO> readings = tmpReadingsService.getReadingsWithErrors();
            Map<String, Object> response = new HashMap<>();
            response.put("total_errors", readings.size());
            response.put("readings", readings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings with errors");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct account numbers
    @GetMapping("/accounts/distinct")
    public ResponseEntity<?> getDistinctAccNbrs() {
        try {
            List<String> accounts = tmpReadingsService.getDistinctAccNbrs();
            Map<String, Object> response = new HashMap<>();
            response.put("total_accounts", accounts.size());
            response.put("accounts", accounts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct account numbers");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct meter types
    @GetMapping("/meter-types/distinct")
    public ResponseEntity<?> getDistinctMtrTypes() {
        try {
            List<String> meterTypes = tmpReadingsService.getDistinctMtrTypes();
            Map<String, Object> response = new HashMap<>();
            response.put("total_types", meterTypes.size());
            response.put("meter_types", meterTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct meter types");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // FIXED: Helper method to validate session and access
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null) {
            // Validate session
            SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
            validationRequest.setSessionId(sessionId);
            validationRequest.setUserId(userId);
            SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);
            if (!validationResponse.getValid()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
            }

            // If area-specific, validate access
            if (targetAreaCode != null) {
                Optional<com.example.SPSProjectBackend.dto.HsbAreaDTO> areaOpt = locationService.getAreaByCode(targetAreaCode);
                if (areaOpt.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
                }
                com.example.SPSProjectBackend.dto.HsbAreaDTO area = areaOpt.get();
                boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);
                if (!hasAccess) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
                }
            }
        }
    }
}