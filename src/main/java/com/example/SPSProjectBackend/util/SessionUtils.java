package com.example.SPSProjectBackend.util;

import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for accessing user session data globally throughout the application
 */
@Component
public class SessionUtils {

    @Autowired
    @Lazy
    private SecInfoAuthService secInfoAuthService;

    /**
     * Get complete session data by session ID
     */
    public Optional<SecInfoSessionData> getSessionData(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        return secInfoAuthService.getSessionData(sessionId);
    }

    /**
     * Get user's location information from session
     */
    public Optional<SecInfoLoginDTO.UserInfo> getUserLocationFromSession(String sessionId, String userId) {
        return secInfoAuthService.getUserLocationFromSession(sessionId, userId);
    }

    /**
     * Check if user has access to a specific region
     */
    public boolean hasRegionAccess(String sessionId, String userId, String targetRegionCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return false;
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();

            // Admin has access to all regions
            if ("Admin".equals(userCategory)) {
                return true;
            }

            // Region/Province/Area users can only access their assigned region
            return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user has access to a specific province
     */
    public boolean hasProvinceAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return false;
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();

            // Admin has access to all provinces
            if ("Admin".equals(userCategory)) {
                return true;
            }

            // Region users have access to all provinces in their region
            if ("Region User".equals(userCategory)) {
                return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode());
            }

            // Province/Area users can only access their assigned province
            return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode()) &&
                   targetProvinceCode != null && targetProvinceCode.equals(userInfo.getProvinceCode());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user has access to a specific area
     */
    public boolean hasAreaAccess(String sessionId, String userId, String targetRegionCode, 
                                String targetProvinceCode, String targetAreaCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return false;
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();

            // Admin has access to all areas
            if ("Admin".equals(userCategory)) {
                return true;
            }

            // Region users have access to all areas in their region
            if ("Region User".equals(userCategory)) {
                return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode());
            }

            // Province users have access to all areas in their province
            if ("Province User".equals(userCategory)) {
                return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode()) &&
                       targetProvinceCode != null && targetProvinceCode.equals(userInfo.getProvinceCode());
            }

            // Area users can only access their assigned area
            if ("Area User".equals(userCategory)) {
                return targetRegionCode != null && targetRegionCode.equals(userInfo.getRegionCode()) &&
                       targetProvinceCode != null && targetProvinceCode.equals(userInfo.getProvinceCode()) &&
                       targetAreaCode != null && targetAreaCode.equals(userInfo.getAreaCode());
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user's region code from session
     */
    public Optional<String> getUserRegionCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getRegionCode);
    }

    /**
     * Get user's province code from session
     */
    public Optional<String> getUserProvinceCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getProvinceCode);
    }

    /**
     * Get user's area code from session
     */
    public Optional<String> getUserAreaCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getAreaCode);
    }

    /**
     * Get user category from session
     */
    public Optional<String> getUserCategory(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getUserCategory);
    }

    /**
     * Check if session is valid and active
     */
    public boolean isSessionValid(String sessionId, String userId) {
        try {
            Optional<SecInfoSessionData> sessionOpt = getSessionData(sessionId);
            if (!sessionOpt.isPresent()) {
                return false;
            }

            SecInfoSessionData session = sessionOpt.get();
            return userId == null || userId.equals(session.getUserId());

        } catch (Exception e) {
            return false;
        }
    }
}