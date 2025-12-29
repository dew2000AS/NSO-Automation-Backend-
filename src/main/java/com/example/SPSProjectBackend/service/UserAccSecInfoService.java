package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.UserAccSecInfoDTO;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.common.Encryption;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAccSecInfoService {

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private Encryption encryption;

    // Password validation pattern: at least one letter and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");

    // Enhanced password validation method
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        // Check length (6-8 characters)
        if (password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (password.length() > 8) {
            throw new RuntimeException("Password must not exceed 8 characters");
        }

        // Check for at least one letter and one number
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("Password must contain at least one letter and one number");
        }
    }

    // Method to encrypt password using your custom encryption
    private String encryptPassword(String userId, String plainPassword) {
        try {
            Double M_PS = Double.parseDouble(encryption.checkPass(plainPassword));
            Double M_PU = Double.parseDouble(encryption.checkPass(userId.trim().toUpperCase()));
            Double M_P = ((M_PU + M_PS) / 100005600000.9987);
            
            // Format to 8 decimal places to maintain consistency
            DecimalFormat df = new DecimalFormat("#.########");
            return df.format(M_P);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt password: " + e.getMessage(), e);
        }
    }

    // Validate location codes based on user category
    private void validateLocationCodes(UserAccSecInfoDTO userAccSecInfoDTO) {
        String userCat = userAccSecInfoDTO.getUserCat();
        
        if ("Region User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                throw new RuntimeException("Region code is required for Region User");
            }
            // Region users should not have province or area codes
            if (userAccSecInfoDTO.getProvinceCode() != null || userAccSecInfoDTO.getAreaCode() != null) {
                throw new RuntimeException("Region User should only have region code, not province or area codes");
            }
        } else if ("Province User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                throw new RuntimeException("Region code is required for Province User");
            }
            if (userAccSecInfoDTO.getProvinceCode() == null || userAccSecInfoDTO.getProvinceCode().trim().isEmpty()) {
                throw new RuntimeException("Province code is required for Province User");
            }
            // Province users should not have area code
            if (userAccSecInfoDTO.getAreaCode() != null) {
                throw new RuntimeException("Province User should not have area code");
            }
        } else if ("Area User".equals(userCat)) {
            if (userAccSecInfoDTO.getRegionCode() == null || userAccSecInfoDTO.getRegionCode().trim().isEmpty()) {
                throw new RuntimeException("Region code is required for Area User");
            }
            if (userAccSecInfoDTO.getProvinceCode() == null || userAccSecInfoDTO.getProvinceCode().trim().isEmpty()) {
                throw new RuntimeException("Province code is required for Area User");
            }
            if (userAccSecInfoDTO.getAreaCode() == null || userAccSecInfoDTO.getAreaCode().trim().isEmpty()) {
                throw new RuntimeException("Area code is required for Area User");
            }
        }
    }

    // Get all user accounts (including status)
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllUserAccSecInfos() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAll();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user accounts: " + e.getMessage(), e);
        }
    }

    // Get all active users only
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllActiveUsers() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAllActiveUsers();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve active user accounts: " + e.getMessage(), e);
        }
    }

    // Get all inactive users only
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getAllInactiveUsers() {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findAllInactiveUsers();
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve inactive user accounts: " + e.getMessage(), e);
        }
    }

    // Get user account by ID
    @Transactional(readOnly = true)
    public Optional<UserAccSecInfoDTO> getUserAccSecInfoById(String userId) {
        try {
            Optional<UserAccSecInfo> account = userAccSecInfoRepository.findById(userId);
            return account.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user account with ID: " + userId + " - " + e.getMessage(), e);
        }
    }

    // Create new user account (status defaults to 1 - active)
    @Transactional
    public UserAccSecInfoDTO createUserAccSecInfo(UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            // Validate required fields
            if (userAccSecInfoDTO.getUserId() == null || userAccSecInfoDTO.getUserId().trim().isEmpty()) {
                throw new RuntimeException("User ID is required");
            }
            
            if (userAccSecInfoDTO.getUserName() == null || userAccSecInfoDTO.getUserName().trim().isEmpty()) {
                throw new RuntimeException("User name is required");
            }
            
            if (userAccSecInfoDTO.getUserCat() == null || userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                throw new RuntimeException("User category is required");
            }

            // Validate EPF number (required)
            if (userAccSecInfoDTO.getEpfNum() == null || userAccSecInfoDTO.getEpfNum().trim().isEmpty()) {
                throw new RuntimeException("EPF number is required");
            }

            // Validate password
            validatePassword(userAccSecInfoDTO.getPasswd());

            // Validate location codes based on user category
            validateLocationCodes(userAccSecInfoDTO);

            // Check if user ID already exists
            if (userAccSecInfoRepository.existsByUserId(userAccSecInfoDTO.getUserId())) {
                throw new RuntimeException("User ID already exists: " + userAccSecInfoDTO.getUserId());
            }

            // Check if username already exists
            if (userAccSecInfoRepository.existsByUserName(userAccSecInfoDTO.getUserName())) {
                throw new RuntimeException("Username already exists: " + userAccSecInfoDTO.getUserName());
            }

            // Check if EPF number already exists
            if (userAccSecInfoRepository.existsByEpfNum(userAccSecInfoDTO.getEpfNum())) {
                throw new RuntimeException("EPF number already exists: " + userAccSecInfoDTO.getEpfNum());
            }

            // Convert DTO to Entity
            UserAccSecInfo userAccSecInfo = new UserAccSecInfo();
            userAccSecInfo.setUserId(userAccSecInfoDTO.getUserId());
            userAccSecInfo.setUserName(userAccSecInfoDTO.getUserName());
            userAccSecInfo.setUserCat(userAccSecInfoDTO.getUserCat());
            userAccSecInfo.setEpfNum(userAccSecInfoDTO.getEpfNum());
            
            // Set status to active (1) for new users - user doesn't need to pass status
            userAccSecInfo.setStatus(1);
            
            // Set location codes based on user category
            setLocationCodesOnEntity(userAccSecInfo, userAccSecInfoDTO);
            
            // ENCRYPT THE PASSWORD BEFORE SAVING
            String encryptedPassword = encryptPassword(userAccSecInfoDTO.getUserId(), userAccSecInfoDTO.getPasswd());
            userAccSecInfo.setPasswd(encryptedPassword);

            UserAccSecInfo savedAccount = userAccSecInfoRepository.save(userAccSecInfo);
            
            // Force flush to ensure data is saved
            userAccSecInfoRepository.flush();
            
            return convertToDTO(savedAccount);
        } catch (RuntimeException e) {
            throw e; // Re-throw runtime exceptions as-is
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user account: " + e.getMessage(), e);
        }
    }

    // Helper method to set location codes on entity
    private void setLocationCodesOnEntity(UserAccSecInfo entity, UserAccSecInfoDTO dto) {
        String userCat = dto.getUserCat();
        
        if ("Region User".equals(userCat)) {
            entity.setRegionCode(dto.getRegionCode());
            entity.setProvinceCode(null);
            entity.setAreaCode(null);
        } else if ("Province User".equals(userCat)) {
            entity.setRegionCode(dto.getRegionCode());
            entity.setProvinceCode(dto.getProvinceCode());
            entity.setAreaCode(null);
        } else if ("Area User".equals(userCat)) {
            entity.setRegionCode(dto.getRegionCode());
            entity.setProvinceCode(dto.getProvinceCode());
            entity.setAreaCode(dto.getAreaCode());
        } else {
            // For other user categories, set all location codes to null
            entity.setRegionCode(null);
            entity.setProvinceCode(null);
            entity.setAreaCode(null);
        }
    }

    // Update user account
    public UserAccSecInfoDTO updateUserAccSecInfo(String userId, UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            UserAccSecInfo existingAccount = userAccSecInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            // Update fields
            if (userAccSecInfoDTO.getUserName() != null && !userAccSecInfoDTO.getUserName().isEmpty()) {
                // Check if new username already exists (excluding current user)
                Optional<UserAccSecInfo> userWithSameName = userAccSecInfoRepository.findByUserName(userAccSecInfoDTO.getUserName());
                if (userWithSameName.isPresent() && !userWithSameName.get().getUserId().equals(userId)) {
                    throw new RuntimeException("Username already exists: " + userAccSecInfoDTO.getUserName());
                }
                existingAccount.setUserName(userAccSecInfoDTO.getUserName());
            }

            if (userAccSecInfoDTO.getEpfNum() != null && !userAccSecInfoDTO.getEpfNum().isEmpty()) {
                // Check if new EPF number already exists (excluding current user)
                Optional<UserAccSecInfo> userWithSameEpf = userAccSecInfoRepository.findByEpfNum(userAccSecInfoDTO.getEpfNum());
                if (userWithSameEpf.isPresent() && !userWithSameEpf.get().getUserId().equals(userId)) {
                    throw new RuntimeException("EPF number already exists: " + userAccSecInfoDTO.getEpfNum());
                }
                existingAccount.setEpfNum(userAccSecInfoDTO.getEpfNum());
            }

            if (userAccSecInfoDTO.getPasswd() != null && !userAccSecInfoDTO.getPasswd().isEmpty()) {
                // Validate password before updating
                validatePassword(userAccSecInfoDTO.getPasswd());
                
                // ENCRYPT THE NEW PASSWORD BEFORE SAVING
                String encryptedPassword = encryptPassword(userId, userAccSecInfoDTO.getPasswd());
                existingAccount.setPasswd(encryptedPassword);
            }

            if (userAccSecInfoDTO.getUserCat() != null && !userAccSecInfoDTO.getUserCat().isEmpty()) {
                existingAccount.setUserCat(userAccSecInfoDTO.getUserCat());
                
                // Validate and update location codes based on new user category
                validateLocationCodes(userAccSecInfoDTO);
                setLocationCodesOnEntity(existingAccount, userAccSecInfoDTO);
            }

            // Update status if provided
            if (userAccSecInfoDTO.getStatus() != null) {
                if (userAccSecInfoDTO.getStatus() == 0 || userAccSecInfoDTO.getStatus() == 1) {
                    existingAccount.setStatus(userAccSecInfoDTO.getStatus());
                } else {
                    throw new RuntimeException("Status must be 0 (inactive) or 1 (active)");
                }
            }

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            
            // Force flush to ensure data is saved
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e; // Re-throw runtime exceptions as-is
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user account: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Toggle user status (activate/deactivate)
    @Transactional
    public UserAccSecInfoDTO toggleUserStatus(String userId) {
        try {
            UserAccSecInfo existingAccount = userAccSecInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            // Toggle status: 1 -> 0, 0 -> 1
            Integer newStatus = existingAccount.getStatus() == 1 ? 0 : 1;
            existingAccount.setStatus(newStatus);

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle user status: " + e.getMessage(), e);
        }
    }

    // NEW METHOD: Set user status explicitly
    @Transactional
    public UserAccSecInfoDTO setUserStatus(String userId, Integer status) {
        try {
            if (status != 0 && status != 1) {
                throw new RuntimeException("Status must be 0 (inactive) or 1 (active)");
            }

            UserAccSecInfo existingAccount = userAccSecInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User account not found with ID: " + userId));

            existingAccount.setStatus(status);

            UserAccSecInfo updatedAccount = userAccSecInfoRepository.save(existingAccount);
            userAccSecInfoRepository.flush();
            
            return convertToDTO(updatedAccount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user status: " + e.getMessage(), e);
        }
    }

    // Check if user is active
    @Transactional(readOnly = true)
    public boolean isUserActive(String userId) {
        try {
            return userAccSecInfoRepository.isUserActive(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check user status: " + e.getMessage(), e);
        }
    }

    // REMOVE DELETE METHOD - Users are never deleted
    // public void deleteUserAccSecInfo(String userId) - REMOVED

    // Get users by category
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByCategory(String category) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByUserCat(category);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by category: " + e.getMessage(), e);
        }
    }

    // Get users by region code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByRegionCode(String regionCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByRegionCode(regionCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by region code: " + e.getMessage(), e);
        }
    }

    // Get users by province code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByProvinceCode(String provinceCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByProvinceCode(provinceCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by province code: " + e.getMessage(), e);
        }
    }

    // Get users by area code
    @Transactional(readOnly = true)
    public List<UserAccSecInfoDTO> getUsersByAreaCode(String areaCode) {
        try {
            List<UserAccSecInfo> accounts = userAccSecInfoRepository.findByAreaCode(areaCode);
            return accounts.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve users by area code: " + e.getMessage(), e);
        }
    }

    // Get user by EPF number
    @Transactional(readOnly = true)
    public Optional<UserAccSecInfoDTO> getUserByEpfNum(String epfNum) {
        try {
            Optional<UserAccSecInfo> account = userAccSecInfoRepository.findByEpfNum(epfNum);
            return account.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user by EPF number: " + e.getMessage(), e);
        }
    }

    // Validate user login - Updated to only allow active users with better error handling
    @Transactional(readOnly = true)
    public boolean validateUserLogin(String userId, String password) {
        try {
            if (userId == null || userId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return false;
            }

            // Use the new repository method to find only active users
            Optional<UserAccSecInfo> user = userAccSecInfoRepository.findActiveUserById(userId.trim());
            if (!user.isPresent()) {
                // User doesn't exist OR user exists but is inactive
                // We return false for both cases (security through obscurity)
                return false;
            }

            UserAccSecInfo activeUser = user.get();
            
            // Double-check status (defensive programming)
            if (activeUser.getStatus() == null || activeUser.getStatus() != 1) {
                return false;
            }

            try {
                // Use the encryption service to validate login
                // The stored password in DB is already encrypted, so we use validateLogin method
                return encryption.validateLogin(userId.trim(), password, activeUser.getPasswd());
            } catch (Exception e) {
                System.err.println("Encryption validation failed for user " + userId + ": " + e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Login validation failed: " + e.getMessage());
            return false;
        }
    }

    // NEW METHOD: Check if user can login (separate from password validation)
    @Transactional(readOnly = true)
    public boolean canUserLogin(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return false;
            }
            return userAccSecInfoRepository.isUserActiveById(userId.trim());
        } catch (Exception e) {
            System.err.println("Failed to check if user can login: " + e.getMessage());
            return false;
        }
    }

    // Helper method to convert Entity to DTO (updated to include status)
    private UserAccSecInfoDTO convertToDTO(UserAccSecInfo userAccSecInfo) {
        try {
            if (userAccSecInfo == null) {
                return null;
            }
            
            UserAccSecInfoDTO dto = new UserAccSecInfoDTO();
            dto.setUserId(userAccSecInfo.getUserId());
            dto.setUserName(userAccSecInfo.getUserName());
            // Don't include password in DTO for security
            dto.setUserCat(userAccSecInfo.getUserCat());
            // Include location codes in DTO
            dto.setRegionCode(userAccSecInfo.getRegionCode());
            dto.setProvinceCode(userAccSecInfo.getProvinceCode());
            dto.setAreaCode(userAccSecInfo.getAreaCode());
            // Include EPF number in DTO
            dto.setEpfNum(userAccSecInfo.getEpfNum());
            // Include status in DTO
            dto.setStatus(userAccSecInfo.getStatus());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to DTO: " + e.getMessage(), e);
        }
    }
}