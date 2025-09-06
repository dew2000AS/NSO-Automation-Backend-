package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.common.Encryption;
import com.example.SPSProjectBackend.service.BillCycleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class SecInfoAuthService {

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private SecInfoSessionRepository secInfoSessionRepository;

    @Autowired
    private Encryption encryption;

    @Autowired
    private BillCycleService billCycleService;

    // Session timeout in hours (24 hours)
    private static final int DEFAULT_SESSION_TIMEOUT_HOURS = 24;

    /**
     * Authenticate user and create session with location codes based on user category
     */
    @Transactional
    public SecInfoLoginDTO.LoginResponse authenticateUser(SecInfoLoginDTO.LoginRequest loginRequest, 
                                                        String ipAddress, String userAgent) {
        try {
            // Validate input
            if (loginRequest.getUserId() == null || loginRequest.getUserId().trim().isEmpty()) {
                return createLoginResponse(false, "User ID is required", null, null, null, null);
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return createLoginResponse(false, "Password is required", null, null, null, null);
            }

            // Find user
            Optional<UserAccSecInfo> userOptional = userAccSecInfoRepository.findById(loginRequest.getUserId().trim());
            if (!userOptional.isPresent()) {
                return createLoginResponse(false, "Invalid user ID or password", null, null, null, null);
            }

            UserAccSecInfo user = userOptional.get();

            // CRITICAL: Check if user is active BEFORE password validation
            // This prevents inactive users from logging in even with correct credentials
            if (user.getStatus() == null || user.getStatus() != 1) {
                // Use generic message for security (don't reveal account exists but is inactive)
                return createLoginResponse(false, "Invalid user ID or password", null, null, null, null);
            }

            // Validate password using encryption service
            boolean passwordValid = encryption.validateLogin(
                loginRequest.getUserId().trim(), 
                loginRequest.getPassword(), 
                user.getPasswd()
            );

            if (!passwordValid) {
                return createLoginResponse(false, "Invalid user ID or password", null, null, null, null);
            }

            // Double-check user status again (additional safety)
            if (user.getStatus() != 1) {
                return createLoginResponse(false, "Invalid user ID or password", null, null, null, null);
            }

            // Check if user already has an active session (single session per user)
            Optional<SecInfoSessionData> existingSession = secInfoSessionRepository.findByUserId(user.getUserId());
            if (existingSession.isPresent()) {
                // Invalidate existing session
                secInfoSessionRepository.delete(existingSession.get());
            }

            // Create new session
            String sessionId = generateSessionId();
            LocalDateTime loginTime = LocalDateTime.now();
            
            // Set session timeout (24 hours)
            Long timeToLive = DEFAULT_SESSION_TIMEOUT_HOURS * 3600L; // 24 hours in seconds

            // Create session with location codes
            SecInfoSessionData sessionData = new SecInfoSessionData(
                sessionId,
                user.getUserId(),
                user.getUserName(),
                user.getUserCat(),
                user.getRegionCode(),    // Store region code in session
                user.getProvinceCode(),  // Store province code in session
                user.getAreaCode(),      // Store area code in session
                loginTime,
                loginTime, // lastAccessTime = loginTime initially
                ipAddress,
                userAgent
            );
            sessionData.setTimeToLive(timeToLive);

            // Save session to Redis
            secInfoSessionRepository.save(sessionData);

            // Create user info with location codes based on user category
            SecInfoLoginDTO.UserInfo userInfo = createUserInfoWithLocationCodes(user);

            LocalDateTime expiresAt = loginTime.plusSeconds(timeToLive);

            return createLoginResponse(true, "Login successful", sessionId, userInfo, loginTime, expiresAt);

        } catch (Exception e) {
            return createLoginResponse(false, "Authentication failed: " + e.getMessage(), null, null, null, null);
        }
    }

    /**
     * Logout user and invalidate session
     */
    @Transactional
    public SecInfoLoginDTO.LogoutResponse logoutUser(SecInfoLoginDTO.LogoutRequest logoutRequest) {
        try {
            // Validate input
            if (logoutRequest.getSessionId() == null || logoutRequest.getSessionId().trim().isEmpty()) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Session ID is required", LocalDateTime.now());
            }

            // Find and delete session
            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(logoutRequest.getSessionId());
            if (!sessionOptional.isPresent()) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Invalid session", LocalDateTime.now());
            }

            SecInfoSessionData session = sessionOptional.get();
            
            // Verify user ID if provided
            if (logoutRequest.getUserId() != null && !logoutRequest.getUserId().equals(session.getUserId())) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Session does not belong to this user", LocalDateTime.now());
            }

            // Delete session
            secInfoSessionRepository.delete(session);

            return new SecInfoLoginDTO.LogoutResponse(true, "Logout successful", LocalDateTime.now());

        } catch (Exception e) {
            return new SecInfoLoginDTO.LogoutResponse(false, "Logout failed: " + e.getMessage(), LocalDateTime.now());
        }
    }

    /**
     * Validate session and update last access time - includes location codes
     * Added check, to ensure user is still active even after login
     */
    @Transactional
    public SecInfoLoginDTO.SessionValidationResponse validateSession(SecInfoLoginDTO.SessionValidationRequest request) {
        try {
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Session ID is required", null, null, null);
            }

            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(request.getSessionId());
            if (!sessionOptional.isPresent()) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Invalid or expired session", null, null, null);
            }

            SecInfoSessionData session = sessionOptional.get();

            // Verify user ID if provided
            if (request.getUserId() != null && !request.getUserId().equals(session.getUserId())) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Session does not belong to this user", null, null, null);
            }

            // CRITICAL: Check if user is still active in the database
            // This handles cases where user was deactivated after login
            Optional<UserAccSecInfo> userOptional = userAccSecInfoRepository.findById(session.getUserId());
            if (!userOptional.isPresent()) {
                // User no longer exists - invalidate session
                secInfoSessionRepository.delete(session);
                return new SecInfoLoginDTO.SessionValidationResponse(false, "User account no longer exists", null, null, null);
            }

            UserAccSecInfo user = userOptional.get();
            if (user.getStatus() == null || user.getStatus() != 1) {
                // User is no longer active - invalidate session immediately
                secInfoSessionRepository.delete(session);
                return new SecInfoLoginDTO.SessionValidationResponse(false, "User account is no longer active", null, null, null);
            }

            // Update last access time
            session.setLastAccessTime(LocalDateTime.now());
            secInfoSessionRepository.save(session);

            // Create user info with location codes from session
            SecInfoLoginDTO.UserInfo userInfo = createUserInfoFromSession(session);

            LocalDateTime expiresAt = session.getLoginTime().plusSeconds(session.getTimeToLive());

            return new SecInfoLoginDTO.SessionValidationResponse(
                true, 
                "Session is valid", 
                userInfo, 
                session.getLastAccessTime(), 
                expiresAt
            );

        } catch (Exception e) {
            return new SecInfoLoginDTO.SessionValidationResponse(false, "Session validation failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Helper method to create UserInfo with location codes based on user category
     * - Admin: no location codes
     * - Region User: region_code only
     * - Province User: region_code and province_code
     * - Area User: region_code, province_code, and area_code
     */
    private SecInfoLoginDTO.UserInfo createUserInfoWithLocationCodes(UserAccSecInfo user) {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUserName());
        userInfo.setUserCategory(user.getUserCat());

        // Set location codes based on user category
        String userCat = user.getUserCat();
        
        if ("Admin".equals(userCat)) {
            // Admin users get no location codes
            userInfo.setRegionCode(null);
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        } else if ("Region User".equals(userCat)) {
            // Region users get region_code only
            userInfo.setRegionCode(user.getRegionCode());
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        } else if ("Province User".equals(userCat)) {
            // Province users get region_code and province_code
            userInfo.setRegionCode(user.getRegionCode());
            userInfo.setProvinceCode(user.getProvinceCode());
            userInfo.setAreaCode(null);
        } else if ("Area User".equals(userCat)) {
            // Area users get all location codes
            userInfo.setRegionCode(user.getRegionCode());
            userInfo.setProvinceCode(user.getProvinceCode());
            userInfo.setAreaCode(user.getAreaCode());
        } else {
            // Default case - no location codes
            userInfo.setRegionCode(null);
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        }

        return userInfo;
    }

    /**
     * Helper method to create UserInfo from session data
     */
    private SecInfoLoginDTO.UserInfo createUserInfoFromSession(SecInfoSessionData session) {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setUserId(session.getUserId());
        userInfo.setUserName(session.getUserName());
        userInfo.setUserCategory(session.getUserCategory());

        // Set location codes based on user category from session
        String userCat = session.getUserCategory();
        
        if ("Admin".equals(userCat)) {
            // Admin users get no location codes
            userInfo.setRegionCode(null);
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        } else if ("Region User".equals(userCat)) {
            // Region users get region_code only
            userInfo.setRegionCode(session.getRegionCode());
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        } else if ("Province User".equals(userCat)) {
            // Province users get region_code and province_code
            userInfo.setRegionCode(session.getRegionCode());
            userInfo.setProvinceCode(session.getProvinceCode());
            userInfo.setAreaCode(null);
        } else if ("Area User".equals(userCat)) {
            // Area users get all location codes
            userInfo.setRegionCode(session.getRegionCode());
            userInfo.setProvinceCode(session.getProvinceCode());
            userInfo.setAreaCode(session.getAreaCode());
        } else {
            // Default case - no location codes
            userInfo.setRegionCode(null);
            userInfo.setProvinceCode(null);
            userInfo.setAreaCode(null);
        }

        return userInfo;
    }

    /**
     * Get session data by session ID (for accessing location codes globally)
     */
    @Transactional(readOnly = true)
    public Optional<SecInfoSessionData> getSessionData(String sessionId) {
        try {
            return secInfoSessionRepository.findById(sessionId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get user's location codes from active session
     */
    @Transactional(readOnly = true)
    public Optional<SecInfoLoginDTO.UserInfo> getUserLocationFromSession(String sessionId, String userId) {
        try {
            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(sessionId);
            if (sessionOptional.isPresent()) {
                SecInfoSessionData session = sessionOptional.get();
                if (userId == null || userId.equals(session.getUserId())) {
                    return Optional.of(createUserInfoFromSession(session));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
    * Get bill cycles for user during login
    */
    private List<BillCycleDTO.AreaBillCycleDTO> getUserBillCyclesForLogin(UserAccSecInfo user) {
    try {
        String userCategory = user.getUserCat();
        List<BillCycleDTO.AreaBillCycleDTO> billCycles = new ArrayList<>();
        
        switch (userCategory) {
            case "Admin":
                // Admin sees all areas
                billCycles = billCycleService.getAllAreasBillCycles();
                break;
            case "Region User":
                // Region user sees all areas in their region
                if (user.getRegionCode() != null) {
                    billCycles = billCycleService.getRegionBillCycles(user.getRegionCode());
                }
                break;
            case "Province User":
                // Province user sees all areas in their province
                if (user.getProvinceCode() != null) {
                    billCycles = billCycleService.getProvinceBillCycles(user.getProvinceCode());
                }
                break;
            case "Area User":
                // Area user sees only their area
                if (user.getAreaCode() != null) {
                    billCycles = billCycleService.getAreaBillCycles(user.getAreaCode());
                }
                break;
        }
        
            return billCycles;
        } catch (Exception e) {
            // Return empty list on error
            return new ArrayList<>();
        }
    }

    // Helper methods
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SecInfoLoginDTO.LoginResponse createLoginResponse(Boolean success, String message, 
                                                            String sessionId, SecInfoLoginDTO.UserInfo userInfo, 
                                                            LocalDateTime loginTime, LocalDateTime expiresAt) {
        return new SecInfoLoginDTO.LoginResponse(success, message, sessionId, userInfo, loginTime, expiresAt);
    }
}