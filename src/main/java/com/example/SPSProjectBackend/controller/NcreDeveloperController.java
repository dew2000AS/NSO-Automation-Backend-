package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.NcreDeveloperDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.service.NcreDeveloperService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/developers")
public class NcreDeveloperController {

    @Autowired
    private NcreDeveloperService ncreDeveloperService;

    @PostMapping("/register-developer")
    public ResponseEntity<?> createDeveloper(@Valid @RequestBody NcreDeveloperDTO dto, BindingResult br) {
        if (br.hasErrors()) {
            Map<String, String> errors = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage()
                    ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            NcreDeveloper saved = ncreDeveloperService.saveDeveloper(dto);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("acc_nbr", saved.getAccNbr());
            resp.put("message", "Developer saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to save developer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
