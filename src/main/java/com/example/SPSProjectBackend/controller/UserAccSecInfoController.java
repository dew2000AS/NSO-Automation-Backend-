package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.UserAccSecInfoDTO;
import com.example.SPSProjectBackend.service.UserAccSecInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserAccSecInfoController {

    @Autowired
    private UserAccSecInfoService userAccSecInfoService;

    // Password validation pattern: at least one letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");

    // Enhanced password validation method
    private String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }

        // Check length (6-8 characters)
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (password.length() > 8) {
            return "Password must not exceed 8 characters";
        }

        // Check for at least one letter and one number
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must contain at least one letter and one number";
        }

        return null; // No validation errors
    }

    // Helper to validate location codes based on user category
    private String validateLocationCodes(UserAccSecInfoDTO userAccSecInfoDTO) {
        String userCat = userAccSecInfoDTO.getUserCat();
        
        if ("Region User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                return "Region code is required for Region User";
            }
            if (userAccSecInfoDTO.getProvinceCode() != null || userAccSecInfoDTO.getAreaCode() != null) {
                return "Region User should only have region code";
            }
        } else if ("Province User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                return "Region code is required for Province User";
            }
            if (userAccSecInfoDTO.getProvinceCode() == null || userAccSecInfoDTO.getProvinceCode().trim().isEmpty()) {
                return "Province code is required for Province User";
            }
            if (userAccSecInfoDTO.getAreaCode() != null) {
                return "Province User should not have area code";
            }
        } else if ("Area User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                return "Region code is required for Area User";
            }
            if (userAccSecInfoDTO.getProvinceCode() == null || userAccSecInfoDTO.getProvinceCode().trim().isEmpty()) {
                return "Province code is required for Area User";
            }
            if (userAccSecInfoDTO.getAreaCode() == null || userAccSecInfoDTO.getAreaCode().trim().isEmpty()) {
                return "Area code is required for Area User";
            }
        }
        
        return null; // No validation errors
    }

    // Get all user accounts
    @GetMapping
    public ResponseEntity<?> getAllUserAccSecInfos() {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getAllUserAccSecInfos();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve user accounts");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get user account by ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserAccSecInfoById(@PathVariable String userId) {
        try {
            Optional<UserAccSecInfoDTO> account = userAccSecInfoService.getUserAccSecInfoById(userId);
            if (account.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User account retrieved successfully");
                response.put("user", account.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", "User with ID " + userId + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            e.printStackTrace(); // For debugging
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve user account");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to retrieve user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Create new user account
    @PostMapping
    public ResponseEntity<?> createUserAccSecInfo(@RequestBody UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            // Validation
            if (userAccSecInfoDTO.getUserId() == null || userAccSecInfoDTO.getUserId().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (userAccSecInfoDTO.getUserName() == null || userAccSecInfoDTO.getUserName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User name is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Enhanced password validation
            String passwordError = validatePassword(userAccSecInfoDTO.getPasswd());
            if (passwordError != null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", passwordError);
                return ResponseEntity.badRequest().body(error);
            }

            if (userAccSecInfoDTO.getUserCat() == null || userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User category is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate location codes based on user category
            String locationError = validateLocationCodes(userAccSecInfoDTO);
            if (locationError != null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", locationError);
                return ResponseEntity.badRequest().body(error);
            }

            UserAccSecInfoDTO createdAccount = userAccSecInfoService.createUserAccSecInfo(userAccSecInfoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account created successfully");
            response.put("user", createdAccount);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create user account");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update user account
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserAccSecInfo(@PathVariable String userId, @RequestBody UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            // If password is provided, validate it
            if (userAccSecInfoDTO.getPasswd() != null && !userAccSecInfoDTO.getPasswd().trim().isEmpty()) {
                String passwordError = validatePassword(userAccSecInfoDTO.getPasswd());
                if (passwordError != null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Validation failed");
                    error.put("message", passwordError);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Validate location codes if user category is provided
            if (userAccSecInfoDTO.getUserCat() != null && !userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                String locationError = validateLocationCodes(userAccSecInfoDTO);
                if (locationError != null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Validation failed");
                    error.put("message", locationError);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            UserAccSecInfoDTO updatedAccount = userAccSecInfoService.updateUserAccSecInfo(userId, userAccSecInfoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account updated successfully");
            response.put("user", updatedAccount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user account");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Delete user account
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserAccSecInfo(@PathVariable String userId) {
        try {
            userAccSecInfoService.deleteUserAccSecInfo(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User account deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete user account");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to delete user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by category
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getUsersByCategory(@PathVariable String category) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByCategory(category);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by category");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by region code
    @GetMapping("/region/{regionCode}")
    public ResponseEntity<?> getUsersByRegionCode(@PathVariable String regionCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByRegionCode(regionCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by region code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by province code
    @GetMapping("/province/{provinceCode}")
    public ResponseEntity<?> getUsersByProvinceCode(@PathVariable String provinceCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByProvinceCode(provinceCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by province code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by area code
    @GetMapping("/area/{areaCode}")
    public ResponseEntity<?> getUsersByAreaCode(@PathVariable String areaCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByAreaCode(areaCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by area code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Validate user login
    @PostMapping("/validate")
    public ResponseEntity<?> validateUserLogin(@RequestBody UserLoginRequest loginRequest) {
        try {
            boolean isValid = userAccSecInfoService.validateUserLogin(loginRequest.getUserId(), loginRequest.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Login successful" : "Invalid credentials");
            
            if (isValid) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Login validation failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Inner class for login request
    public static class UserLoginRequest {
        private String userId;
        private String password;

        // Constructors
        public UserLoginRequest() {}

        public UserLoginRequest(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}