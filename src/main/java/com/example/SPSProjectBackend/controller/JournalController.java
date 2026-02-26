package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.JournalDetailDTO;
import com.example.SPSProjectBackend.dto.JournalSummaryDTO;
import com.example.SPSProjectBackend.dto.JournalTypeDTO;
import com.example.SPSProjectBackend.dto.JournalUpdateDTO;
import com.example.SPSProjectBackend.service.JournalService;
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

    // ===================== GET ALL JOURNALS =====================
    @GetMapping
    public ResponseEntity<?> getAllJournals() {
        try {
            System.out.println("API: Fetching journals with filter: area_cd='27' AND added_blcy > 400");
            
            List<JournalSummaryDTO> journals = journalService.getAllJournalsSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", journals.size());
            response.put("data", journals);
            response.put("timestamp", new Date());
            response.put("message", journals.isEmpty() ?
                    "No journals found with area_cd='27' and added_blcy > 400" :
                    "Journals fetched successfully");
            response.put("filter", Map.of(
                "area_cd", "27",
                "added_blcy", "> 400"
            ));

            System.out.println("API: Returning " + journals.size() + " journals");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to fetch journals: " + e.getMessage());
            errorResponse.put("timestamp", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================== GET JOURNAL DETAIL =====================
   @GetMapping("/detail/{jnlNo}")
    public ResponseEntity<?> getJournalDetail(
            @PathVariable Integer jnlNo,
            @RequestParam String accNbr,
            @RequestParam String jnlType,
            @RequestParam BigDecimal adjustAmt) {
        
        try {
            System.out.println("API: Fetching journal detail for composite key: " + 
                             "jnlNo=" + jnlNo + 
                             ", accNbr='" + accNbr + "'" +
                             ", jnlType='" + jnlType + "'" +
                             ", adjustAmt=" + adjustAmt +
                             " with filter: area_cd='27' AND added_blcy > 400");
            
            List<JournalDetailDTO> journals = journalService.getJournalDetail(
                accNbr, jnlType, jnlNo, adjustAmt);

            if (journals.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Journal not found");
                errorResponse.put("message", "No journal found with the specified composite key");
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
}