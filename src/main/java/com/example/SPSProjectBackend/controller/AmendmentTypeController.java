package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.AmendmentTypeDTO;
import com.example.SPSProjectBackend.service.AmendmentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/amendment-types")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AmendmentTypeController {

    @Autowired
    private AmendmentTypeService amendmentTypeService;

    @GetMapping
    public ResponseEntity<List<AmendmentTypeDTO>> getAllAmendmentTypes() {
        try {
            List<AmendmentTypeDTO> amendmentTypes = amendmentTypeService.getAllAmendmentTypes();
            return ResponseEntity.ok(amendmentTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createAmendmentType(@RequestBody AmendmentTypeDTO dto) {
        try {
            AmendmentTypeDTO created = amendmentTypeService.createAmendmentType(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            // Duplicate amendment type
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            // General errors
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to create amendment type: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
