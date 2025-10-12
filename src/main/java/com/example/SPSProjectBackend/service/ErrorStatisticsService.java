package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ErrorStatisticsService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    // Error type mappings
    private static final Map<Integer, String> ERROR_TYPE_MAP = new HashMap<>();
    static {
        ERROR_TYPE_MAP.put(1, "High Consumption");
        ERROR_TYPE_MAP.put(2, "Low Consumption");
        ERROR_TYPE_MAP.put(3, "Reading Error");
        ERROR_TYPE_MAP.put(4, "Charge Error");
        ERROR_TYPE_MAP.put(5, "Negative Error");
        ERROR_TYPE_MAP.put(6, "Total Charge Error");
        ERROR_TYPE_MAP.put(7, "Zero Consumption");
    }

    /**
     * Get error statistics for a specific area
     */
    public ErrorStatisticsDTO.ErrorStatisticsResponse getErrorStatisticsByArea(String areaCd) {
        try {
            // Get active bill cycle for the area
            Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCd);
            String activeBillCycle = activeBillCycleOpt.map(String::valueOf).orElse("Not Available");

            // Get error statistics
            List<Object[]> errorStats = tmpReadingsRepository.findErrorStatisticsByArea(areaCd);
            
            // Get total counts
            Long totalAccountsWithErrors = tmpReadingsRepository.countDistinctAccountsWithErrors(areaCd);
            Long totalErrorInstances = tmpReadingsRepository.countTotalErrorInstances(areaCd);

            // Convert to DTO
            List<ErrorStatisticsDTO.ErrorCount> errorCounts = convertToErrorCounts(errorStats);

            return new ErrorStatisticsDTO.ErrorStatisticsResponse(
                true,
                "Error statistics retrieved successfully for area " + areaCd,
                areaCd,
                activeBillCycle,
                totalAccountsWithErrors,
                totalErrorInstances,
                errorCounts,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            return new ErrorStatisticsDTO.ErrorStatisticsResponse(
                false,
                "Failed to retrieve error statistics: " + e.getMessage(),
                areaCd,
                "Error",
                0L,
                0L,
                new ArrayList<>(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }

    /**
     * Get error statistics for all areas (Admin view)
     */
    public ErrorStatisticsDTO.ErrorStatisticsResponse getErrorStatisticsAllAreas() {
        try {
            // Get error statistics for all areas
            List<Object[]> errorStats = tmpReadingsRepository.findErrorStatisticsAllAreas();

            // Convert to DTO
            List<ErrorStatisticsDTO.ErrorCount> errorCounts = convertToErrorCounts(errorStats);

            // Calculate totals
            Long totalAccountsWithErrors = errorCounts.stream()
                .mapToLong(ErrorStatisticsDTO.ErrorCount::getAccountCount)
                .sum();
            Long totalErrorInstances = errorCounts.stream()
                .mapToLong(ErrorStatisticsDTO.ErrorCount::getTotalErrors)
                .sum();

            return new ErrorStatisticsDTO.ErrorStatisticsResponse(
                true,
                "Error statistics retrieved successfully for all areas",
                "ALL",
                "Multiple",
                totalAccountsWithErrors,
                totalErrorInstances,
                errorCounts,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            return new ErrorStatisticsDTO.ErrorStatisticsResponse(
                false,
                "Failed to retrieve error statistics: " + e.getMessage(),
                "ALL",
                "Error",
                0L,
                0L,
                new ArrayList<>(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }

    /**
     * Get detailed account information for a specific error type in an area
     */
    public ErrorStatisticsDTO.ErrorDetailsResponse getErrorDetailsByAreaAndType(String areaCd, Integer errorCode) {
        try {
            String errorType = ERROR_TYPE_MAP.getOrDefault(errorCode, "Unknown Error");

            // Get account details for the error type
            List<Object[]> accountData = tmpReadingsRepository.findAccountsByErrorType(areaCd, errorCode);

            // Group by account number to consolidate meter types
            Map<String, ErrorStatisticsDTO.ErrorDetails> accountMap = new LinkedHashMap<>();
            
            for (Object[] data : accountData) {
                String accNbr = (String) data[0];
                String mtrType = (String) data[1];
                Date rdngDate = (Date) data[2];

                if (!accountMap.containsKey(accNbr)) {
                    ErrorStatisticsDTO.ErrorDetails details = new ErrorStatisticsDTO.ErrorDetails();
                    details.setAccountNumber(accNbr);
                    details.setErrorCode(errorCode);
                    details.setErrorType(errorType);
                    details.setMeterTypes(new ArrayList<>());
                    details.setReadingDate(rdngDate.toString());
                    accountMap.put(accNbr, details);
                }

                // Add meter type to the existing account
                ErrorStatisticsDTO.ErrorDetails details = accountMap.get(accNbr);
                details.getMeterTypes().add(mtrType);
                details.setTotalMetersWithError(details.getMeterTypes().size());
            }

            List<ErrorStatisticsDTO.ErrorDetails> accountDetails = new ArrayList<>(accountMap.values());

            return new ErrorStatisticsDTO.ErrorDetailsResponse(
                true,
                "Error details retrieved successfully for " + errorType,
                errorCode,
                errorType,
                (long) accountDetails.size(),
                accountDetails,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            return new ErrorStatisticsDTO.ErrorDetailsResponse(
                false,
                "Failed to retrieve error details: " + e.getMessage(),
                errorCode,
                ERROR_TYPE_MAP.getOrDefault(errorCode, "Unknown Error"),
                0L,
                new ArrayList<>(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }

    /**
     * Get detailed account information for a specific error type across all areas (Admin view)
     */
    public ErrorStatisticsDTO.ErrorDetailsResponse getErrorDetailsAllAreas(Integer errorCode) {
        try {
            String errorType = ERROR_TYPE_MAP.getOrDefault(errorCode, "Unknown Error");

            // Get account details for the error type across all areas
            List<Object[]> accountData = tmpReadingsRepository.findAccountsByErrorTypeAllAreas(errorCode);

            // Group by account number and area to consolidate meter types
            Map<String, ErrorStatisticsDTO.ErrorDetails> accountMap = new LinkedHashMap<>();
            
            for (Object[] data : accountData) {
                String accNbr = (String) data[0];
                String areaCd = (String) data[1];
                String mtrType = (String) data[2];
                Date rdngDate = (Date) data[3];

                String accountKey = accNbr + "|" + areaCd;

                if (!accountMap.containsKey(accountKey)) {
                    ErrorStatisticsDTO.ErrorDetails details = new ErrorStatisticsDTO.ErrorDetails();
                    details.setAccountNumber(accNbr + " (" + areaCd + ")");
                    details.setErrorCode(errorCode);
                    details.setErrorType(errorType);
                    details.setMeterTypes(new ArrayList<>());
                    details.setReadingDate(rdngDate.toString());
                    accountMap.put(accountKey, details);
                }

                // Add meter type to the existing account
                ErrorStatisticsDTO.ErrorDetails details = accountMap.get(accountKey);
                details.getMeterTypes().add(mtrType);
                details.setTotalMetersWithError(details.getMeterTypes().size());
            }

            List<ErrorStatisticsDTO.ErrorDetails> accountDetails = new ArrayList<>(accountMap.values());

            return new ErrorStatisticsDTO.ErrorDetailsResponse(
                true,
                "Error details retrieved successfully for " + errorType + " across all areas",
                errorCode,
                errorType,
                (long) accountDetails.size(),
                accountDetails,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            return new ErrorStatisticsDTO.ErrorDetailsResponse(
                false,
                "Failed to retrieve error details: " + e.getMessage(),
                errorCode,
                ERROR_TYPE_MAP.getOrDefault(errorCode, "Unknown Error"),
                0L,
                new ArrayList<>(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
        }
    }

    /**
     * Helper method to convert query results to ErrorCount DTOs
     */
    private List<ErrorStatisticsDTO.ErrorCount> convertToErrorCounts(List<Object[]> errorStats) {
        List<ErrorStatisticsDTO.ErrorCount> errorCounts = new ArrayList<>();

        // Initialize with all error types (1-7)
        for (int i = 1; i <= 7; i++) {
            errorCounts.add(new ErrorStatisticsDTO.ErrorCount(
                ERROR_TYPE_MAP.get(i),
                i,
                0L,
                0L,
                getErrorDescription(i)
            ));
        }

        // Update with actual data from database
        for (Object[] stat : errorStats) {
            Integer errStat = (Integer) stat[0];
            Long accountCount = (Long) stat[1];
            Long totalErrors = (Long) stat[2];

            // Find and update the corresponding error count
            errorCounts.stream()
                .filter(ec -> ec.getErrorCode().equals(errStat))
                .findFirst()
                .ifPresent(ec -> {
                    ec.setAccountCount(accountCount);
                    ec.setTotalErrors(totalErrors);
                });
        }

        return errorCounts;
    }

    /**
     * Get description for each error type
     */
    private String getErrorDescription(Integer errorCode) {
        switch (errorCode) {
            case 1: return "Monthly consumption exceeds 15% of the account's usage Average.";
            case 2: return "Monthly consumption falls below 20% of the account's usage Average.";
            case 3: return "Consumed units do not match the difference between Current and Previous meter readings.";
            case 4: return "Computed charge (Units × Multiplier Factor × Rate) per meter, not equal to Amount.";
            case 5: return "Current meter reading is lower than Previous reading, gives negative Unit consumption.";
            case 6: return "Sum of Individual Meter Charges, Fixed Charge, and VAT does not match the Total Amount.";
            case 7: return "Zero unit consumption gives for difference between Current and Previous meter readings.";
            default: return "Unknown error type";
        }
    }

    /**
     * Get available error types with codes and descriptions
     */
    public Map<String, Object> getErrorTypes() {
        Map<String, Object> errorTypes = new LinkedHashMap<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("code", i);
            errorInfo.put("name", ERROR_TYPE_MAP.get(i));
            errorInfo.put("description", getErrorDescription(i));
            errorTypes.put(ERROR_TYPE_MAP.get(i), errorInfo);
        }
        return errorTypes;
    }
}