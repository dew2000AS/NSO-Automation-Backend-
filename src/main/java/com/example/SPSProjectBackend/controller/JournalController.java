package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.JournalDetailDTO;
import com.example.SPSProjectBackend.dto.JournalSummaryDTO;
import com.example.SPSProjectBackend.dto.JournalTypeDTO;
import com.example.SPSProjectBackend.dto.JournalUpdateDTO;
import com.example.SPSProjectBackend.dto.JournalCreateDTO;
import com.example.SPSProjectBackend.dto.JurnlAuthDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.service.JournalService;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; 
import java.util.*;

@RestController
@RequestMapping("/api/journals")
@CrossOrigin(origins = "http://localhost:3000")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private SessionUtils sessionUtils;

    // ===================== HEALTH CHECK =====================
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "UP");
        response.put("service", "Journal Service");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }

    // ===================== GET ALL JOURNAL TYPES =====================
    @GetMapping("/types")
    public ResponseEntity<?> getAllJournalTypes() {
        try {
            System.out.println("API: Fetching all journal types");
            
            List<JournalTypeDTO> journalTypes = journalService.getAllJournalTypes();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", journalTypes.size());
            response.put("data", journalTypes);
            response.put("timestamp", new Date());
            response.put("message", journalTypes.isEmpty() ?
                    "No journal types found" :
                    "Journal types fetched successfully");

            System.out.println("API: Returning " + journalTypes.size() + " journal types");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to fetch journal types: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================== GET ALL JURNL AUTH =====================
    @GetMapping("/jurnl-auth")
    public ResponseEntity<?> getAllJurnlAuth() {
        try {
            System.out.println("API: Fetching all jurnl_auth records");
            
            List<JurnlAuthDTO> jurnlAuths = journalService.getAllJurnlAuth();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", jurnlAuths.size());
            response.put("data", jurnlAuths);
            response.put("timestamp", new Date());
            response.put("message", jurnlAuths.isEmpty() ?
                    "No jurnl_auth records found" :
                    "Jurnl_auth records fetched successfully");

            System.out.println("API: Returning " + jurnlAuths.size() + " jurnl_auth records");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to fetch jurnl_auth: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================== GET ALL JOURNALS BY SELECTED AREA AND BILL CYCLE =====================
    @GetMapping
    public ResponseEntity<?> getAllJournals(
            @RequestParam(required = false) String area_code,
            @RequestParam(required = false) Integer bill_cycle) {
        try {
            // Validate area_code and bill_cycle parameters
            if (area_code == null || area_code.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "area_code is required");
                errorResponse.put("timestamp", new Date());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (bill_cycle == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "bill_cycle is required");
                errorResponse.put("timestamp", new Date());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            System.out.println("API: Fetching journals for area: " + area_code + ", bill cycle: " + bill_cycle);
            
            List<JournalSummaryDTO> journals = journalService.getAllJournalsSummary(area_code, bill_cycle);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", journals.size());
            response.put("data", journals);
            response.put("timestamp", new Date());
            response.put("message", journals.isEmpty() ?
                    "No journals found for area " + area_code + " and bill cycle " + bill_cycle :
                    "Journals fetched successfully");
            response.put("filter", Map.of(
                "area_code", area_code,
                "bill_cycle", bill_cycle
            ));

            System.out.println("API: Returning " + journals.size() + " journals");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to fetch journals: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================== GET JOURNAL DETAIL BY SELECTED AREA AND BILL CYCLE =====================
    @GetMapping("/detail/{jnlNo}")
    public ResponseEntity<?> getJournalDetail(
            @PathVariable Integer jnlNo,
            @RequestParam String accNbr,
            @RequestParam String jnlType,
            @RequestParam BigDecimal adjustAmt,
            @RequestParam(required = false) String area_code,
            @RequestParam(required = false) Integer bill_cycle) {
        
        try {
            // Validate area_code and bill_cycle parameters
            if (area_code == null || area_code.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "area_code is required");
                errorResponse.put("timestamp", new Date());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (bill_cycle == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "bill_cycle is required");
                errorResponse.put("timestamp", new Date());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            System.out.println("API: Fetching journal detail for area: " + area_code + ", bill cycle: " + bill_cycle);
            System.out.println("Composite key: jnlNo=" + jnlNo + ", accNbr='" + accNbr + "', jnlType='" + jnlType + "', adjustAmt=" + adjustAmt);
            
            List<JournalDetailDTO> journals = journalService.getJournalDetail(
                accNbr, jnlType, jnlNo, adjustAmt, area_code, bill_cycle);

            if (journals.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Journal not found");
                errorResponse.put("message", "No journal found with the specified criteria for area " + area_code + " and bill cycle " + bill_cycle);
                errorResponse.put("compositeKey", Map.of(
                    "jnlNo", jnlNo,
                    "accNbr", accNbr,
                    "jnlType", jnlType,
                    "adjustAmt", adjustAmt
                ));
                errorResponse.put("timestamp", new Date());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", journals.get(0));
                response.put("timestamp", new Date());
                response.put("message", "Journal details fetched successfully");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("API Error for journal " + jnlNo + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to fetch journal details: " + e.getMessage());
            errorResponse.put("jnlNo", jnlNo);
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Keep the old endpoint for backward compatibility or remove it
    @GetMapping("/{jnlNo}")
    public ResponseEntity<?> getJournalDetailOld(@PathVariable Integer jnlNo) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Deprecated Endpoint");
        response.put("message", "Please use /api/journals/detail/{jnlNo} with query parameters: accNbr, jnlType, adjustAmt");
        response.put("timestamp", new Date());
        return ResponseEntity.status(HttpStatus.GONE).body(response);
    }

// ===================== UPDATE JOURNAL =====================
@PutMapping("/update")
public ResponseEntity<?> updateJournal(@RequestBody JournalUpdateDTO journalUpdateDTO) {
    try {
        System.out.println("========== UPDATE JOURNAL REQUEST RECEIVED ==========");
        System.out.println("Journal No: " + journalUpdateDTO.getJnlNo());
        System.out.println("Account Number: " + journalUpdateDTO.getAccNbr());
        System.out.println("Journal Type: " + journalUpdateDTO.getJnlType());
        System.out.println("New Adjustment Amount: " + journalUpdateDTO.getAdjustAmt());
        System.out.println("Area Code: " + journalUpdateDTO.getAreaCd());
        System.out.println("==================================================");
        
        JournalDetailDTO updatedJournal = journalService.updateJournal(journalUpdateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", updatedJournal);
        response.put("message", "Journal updated successfully");
        response.put("timestamp", new Date());

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.err.println("========== UPDATE JOURNAL ERROR ==========");
        System.err.println("Error message: " + e.getMessage());
        System.err.println("Error type: " + e.getClass().getName());
        e.printStackTrace(); // This will print the full stack trace
        System.err.println("===========================================");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Update Failed");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timestamp", new Date());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

// ===================== CREATE JOURNAL =====================
@PostMapping("/create")
public ResponseEntity<?> createJournal(
        @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestBody JournalCreateDTO journalCreateDTO) {
    try {
        System.out.println("========== CREATE JOURNAL REQUEST RECEIVED ==========");
        System.out.println("Session ID: " + sessionId);
        System.out.println("User ID: " + userId);
        
        // Authorization check: Only Accountant Clark can create new journals
        if (sessionId == null || userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Session ID and User ID are required");
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Get user information from session
        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (!userInfoOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Invalid session or user");
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
        String userCategory = userInfo.getUserCategory();
        
        // Check if user is Accountant Clark
        if (!"Accountant Clark".equals(userCategory)) {
            System.out.println("User category '" + userCategory + "' is not authorized to create journals");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Forbidden");
            errorResponse.put("message", "Only Accountant Clark users can create new journals. Other users can only view journals.");
            errorResponse.put("userCategory", userCategory);
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        System.out.println("User authorized. Category: " + userCategory);
        System.out.println("Account Number: " + journalCreateDTO.getAccNbr());
        System.out.println("Journal Type: " + journalCreateDTO.getJnlType());
        System.out.println("Journal No: " + journalCreateDTO.getJnlNo());
        System.out.println("Field 1: " + journalCreateDTO.getField1() + " (" + journalCreateDTO.getField1Type() + ")");
        System.out.println("Field 2: " + journalCreateDTO.getField2() + " (" + journalCreateDTO.getField2Type() + ")");
        System.out.println("Total Amount: " + journalCreateDTO.getTotalAmt());
        System.out.println("====================================================");
        
        JournalDetailDTO createdJournal = journalService.createJournal(journalCreateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", createdJournal);
        response.put("message", "Journal created successfully");
        response.put("timestamp", new Date());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
        System.err.println("========== CREATE JOURNAL ERROR ==========");
        System.err.println("Error message: " + e.getMessage());
        System.err.println("Error type: " + e.getClass().getName());
        e.printStackTrace();
        System.err.println("===========================================");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Create Failed");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timestamp", new Date());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
}