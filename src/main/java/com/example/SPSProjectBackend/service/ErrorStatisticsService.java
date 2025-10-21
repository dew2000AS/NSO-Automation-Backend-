package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO;
import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO.*;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.repository.ErrorStatisticsRepository;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ErrorStatisticsService {

    @Autowired
    private ErrorStatisticsRepository errorStatisticsRepository;

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    // Error code to name mapping
    private static final Map<Integer, String> ERROR_CODE_MAP = new HashMap<>();
    static {
        ERROR_CODE_MAP.put(1, "High Consumption");
        ERROR_CODE_MAP.put(2, "Low Consumption");
        ERROR_CODE_MAP.put(3, "Reading Error");
        ERROR_CODE_MAP.put(4, "Charge Error");
        ERROR_CODE_MAP.put(5, "Negative Error");
        ERROR_CODE_MAP.put(6, "Total Charge Error");
        ERROR_CODE_MAP.put(7, "Zero Consumption");
    }

    /**
     * Get error statistics for an area
     */
    public ErrorStatsResponse getErrorStatistics(String sessionId, String userId, String areaCode, String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
                if (!activeBillCycleOpt.isPresent()) {
                    return createErrorStatsErrorResponse("No active bill cycle found for area: " + areaCode);
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get area name
            String areaName = locationService.getAreaByCode(areaCode)
                    .map(area -> area.getAreaName())
                    .orElse("");

            // Get error statistics
            ErrorStatisticsData errorStats = calculateErrorStatistics(areaCode, targetBillCycle, areaName);

            return createErrorStatsSuccessResponse("Error statistics retrieved successfully", errorStats);

        } catch (Exception e) {
            return createErrorStatsErrorResponse("Failed to retrieve error statistics: " + e.getMessage());
        }
    }

    /**
     * Get detailed accounts for a specific error code
     */
    public ErrorDetailsResponse getErrorDetails(String sessionId, String userId, String areaCode, 
                                               String billCycle, Integer errorCode) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Validate error code
            if (!ERROR_CODE_MAP.containsKey(errorCode)) {
                return createErrorDetailsErrorResponse("Invalid error code: " + errorCode);
            }

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
                if (!activeBillCycleOpt.isPresent()) {
                    return createErrorDetailsErrorResponse("No active bill cycle found for area: " + areaCode);
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get error instances for the specific error code
            List<TmpReadings> errorInstances = errorStatisticsRepository.findErrorInstancesByErrorCode(
                    areaCode, targetBillCycle, errorCode);

            // Group by account number
            Map<String, List<TmpReadings>> instancesByAccount = errorInstances.stream()
                    .collect(Collectors.groupingBy(TmpReadings::getAccNbr));

            // Create account error details
            List<AccountErrorDetailsDTO> accountDetails = new ArrayList<>();
            for (Map.Entry<String, List<TmpReadings>> entry : instancesByAccount.entrySet()) {
                String accountNumber = entry.getKey();
                List<TmpReadings> accountInstances = entry.getValue();

                // Get customer name
                String customerName = bulkCustomerRepository.findByAccNbr(accountNumber)
                        .map(BulkCustomer::getName)
                        .orElse("");

                // Convert to error instance DTOs
                List<ErrorInstanceDTO> errorInstanceDTOs = accountInstances.stream()
                        .map(this::convertToErrorInstanceDTO)
                        .collect(Collectors.toList());

                AccountErrorDetailsDTO accountDetail = new AccountErrorDetailsDTO();
                accountDetail.setAccountNumber(accountNumber);
                accountDetail.setCustomerName(customerName);
                accountDetail.setAreaCode(areaCode);
                accountDetail.setErrorInstances(errorInstanceDTOs);
                accountDetail.setTotalErrorInstances(errorInstanceDTOs.size());

                accountDetails.add(accountDetail);
            }

            // Sort by account number
            accountDetails.sort(Comparator.comparing(AccountErrorDetailsDTO::getAccountNumber));

            return createErrorDetailsSuccessResponse(
                    errorCode,
                    ERROR_CODE_MAP.get(errorCode), 
                    accountDetails, 
                    "Error details retrieved successfully"
            );

        } catch (Exception e) {
            return createErrorDetailsErrorResponse("Failed to retrieve error details: " + e.getMessage());
        }
    }

    /**
     * Calculate error statistics for an area and bill cycle
     */
    private ErrorStatisticsData calculateErrorStatistics(String areaCode, String billCycle, String areaName) {
        ErrorStatisticsData errorStats = new ErrorStatisticsData();
        errorStats.setAreaCode(areaCode);
        errorStats.setAreaName(areaName);
        errorStats.setActiveBillCycle(billCycle);

        // Get error statistics grouped by error code
        List<Object[]> errorStatsData = errorStatisticsRepository.findErrorStatisticsByAreaAndBillCycle(areaCode, billCycle);
        
        // Get total error instances count
        Long totalErrorInstances = errorStatisticsRepository.countTotalErrorInstances(areaCode, billCycle);
        errorStats.setTotalErrorInstances(totalErrorInstances.intValue());

        // Get unread accounts count
        Long unreadAccounts = errorStatisticsRepository.countUnreadAccounts(areaCode, billCycle);
        errorStats.setUnreadAccountsCount(unreadAccounts.intValue());

        // Get total accounts in area
        Long totalAccounts = errorStatisticsRepository.countTotalAccountsInArea(areaCode);
        errorStats.setTotalAccountsInArea(totalAccounts.intValue());

        // Calculate error counts
        Map<String, ErrorCountDTO> errorCounts = new LinkedHashMap<>();
        
        int totalAccountsWithErrors = 0;

        // Add counts for each error code
        for (Object[] stat : errorStatsData) {
            Integer errorCode = (Integer) stat[0];
            Long accountCount = (Long) stat[1];
            
            totalAccountsWithErrors += accountCount.intValue();

            ErrorCountDTO errorCount = new ErrorCountDTO();
            errorCount.setErrorCode(errorCode);
            errorCount.setErrorName(ERROR_CODE_MAP.getOrDefault(errorCode, "Unknown Error"));
            errorCount.setAccountCount(accountCount.intValue());
            
            // Get instance count for this error code
            List<TmpReadings> instances = errorStatisticsRepository.findErrorInstancesByErrorCode(areaCode, billCycle, errorCode);
            errorCount.setInstanceCount(instances.size());
            
            // Calculate percentage
            double percentage = totalAccounts > 0 ? (accountCount.doubleValue() / totalAccounts) * 100.0 : 0.0;
            errorCount.setPercentage(Math.round(percentage * 100.0) / 100.0);

            errorCounts.put(errorCode.toString(), errorCount);
        }

        errorStats.setTotalAccountsWithErrors(totalAccountsWithErrors);
        errorStats.setErrorCounts(errorCounts);

        // Calculate overall error percentage
        double errorPercentage = totalAccounts > 0 ? 
                (double) totalAccountsWithErrors / totalAccounts * 100.0 : 0.0;
        errorStats.setErrorPercentage(Math.round(errorPercentage * 100.0) / 100.0);

        return errorStats;
    }

    /**
     * Convert TmpReadings to ErrorInstanceDTO
     */
    private ErrorInstanceDTO convertToErrorInstanceDTO(TmpReadings reading) {
        ErrorInstanceDTO dto = new ErrorInstanceDTO();
        dto.setMeterType(reading.getMtrType());
        dto.setErrorCode(reading.getErrStat());
        dto.setErrorName(ERROR_CODE_MAP.getOrDefault(reading.getErrStat(), "Unknown Error"));
        dto.setReadingDate(reading.getRdngDate() != null ? reading.getRdngDate().toString() : "");
        dto.setPresentReading(reading.getPrsntRdn());
        dto.setPreviousReading(reading.getPrvRdn());
        dto.setUnits(reading.getUnits());
        return dto;
    }

    /**
     * Validate session and access rights
     */
    private void validateSessionAndAccess(String sessionId, String userId, String areaCode) {
        if (sessionId == null || userId == null) {
            throw new RuntimeException("Session ID and User ID are required");
        }

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (!userInfoOpt.isPresent()) {
            throw new RuntimeException("Invalid session or user not found");
        }

        // Check access to area
        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
        boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, 
                userInfo.getRegionCode(), userInfo.getProvinceCode(), areaCode);
        if (!hasAccess) {
            throw new RuntimeException("Access denied to area: " + areaCode);
        }
    }

    // Response creation methods
    private ErrorStatsResponse createErrorStatsSuccessResponse(String message, ErrorStatisticsData errorStats) {
        ErrorStatsResponse response = new ErrorStatsResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setErrorStatistics(errorStats);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private ErrorStatsResponse createErrorStatsErrorResponse(String message) {
        ErrorStatsResponse response = new ErrorStatsResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private ErrorDetailsResponse createErrorDetailsSuccessResponse(Integer errorCode, String errorName, 
                                                                 List<AccountErrorDetailsDTO> accounts, 
                                                                 String message) {
        ErrorDetailsResponse response = new ErrorDetailsResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setErrorName(errorName);
        response.setAccountsWithError(accounts);
        response.setTotalAccounts(accounts.size());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private ErrorDetailsResponse createErrorDetailsErrorResponse(String message) {
        ErrorDetailsResponse response = new ErrorDetailsResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}