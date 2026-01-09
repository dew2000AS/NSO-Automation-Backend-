package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO;
import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO.*;
import com.example.SPSProjectBackend.model.*;
import com.example.SPSProjectBackend.repository.*;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MeterReadingInfoService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private MonTotRepository monTotRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    /**
     * Get meter reading information for a single customer
     */
    public MeterReadingInfoResponse getMeterReadingInfo(String sessionId, String userId, 
                                                       String accountNumber, String areaCode, 
                                                       String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
            if (!customerOpt.isPresent()) {
                return createErrorResponse("Customer not found with account number: " + accountNumber);
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code matches customer's area
            if (areaCode != null && !areaCode.equals(customer.getAreaCd())) {
                return createErrorResponse("Customer does not belong to area: " + areaCode);
            }

            // Use provided area code or customer's area code
            String targetAreaCode = areaCode != null ? areaCode : customer.getAreaCd();

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(targetAreaCode);
                if (!activeBillCycleOpt.isPresent()) {
                    return createErrorResponse("No active bill cycle found for area: " + targetAreaCode);
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get meter reading information
            MeterReadingInfoDetailsDTO readingInfo = getMeterReadingDetails(customer, targetAreaCode, targetBillCycle);

            return createSuccessResponse("Meter reading information retrieved successfully", readingInfo);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve meter reading information: " + e.getMessage());
        }
    }

    /**
     * Get meter reading information for multiple customers in an area
     */
    public BulkMeterReadingResponse getBulkMeterReadingInfo(String sessionId, String userId, 
                                                           String areaCode, List<String> accountNumbers, 
                                                           String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get active bill cycle if not provided
            String targetBillCycle = billCycle;
            if (targetBillCycle == null) {
                Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
                if (!activeBillCycleOpt.isPresent()) {
                    return createBulkErrorResponse("No active bill cycle found for area: " + areaCode);
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            // Get area name
            String areaName = areaRepository.findByAreaCode(areaCode)
                    .map(HsbArea::getAreaName)
                    .orElse("");

            List<MeterReadingInfoDetailsDTO> meterReadings = new ArrayList<>();
            int customersWithReadings = 0;
            int customersWithoutReadings = 0;

            // If account numbers are provided, get specific customers, otherwise get all customers in area
            List<BulkCustomer> customers;
            if (accountNumbers != null && !accountNumbers.isEmpty()) {
                customers = bulkCustomerRepository.findByAccNbrInAndAreaCd(accountNumbers, areaCode);
            } else {
                customers = bulkCustomerRepository.findByAreaCd(areaCode);
            }

            for (BulkCustomer customer : customers) {
                MeterReadingInfoDetailsDTO readingInfo = getMeterReadingDetails(customer, areaCode, targetBillCycle);
                meterReadings.add(readingInfo);

                if (readingInfo.getHasReading()) {
                    customersWithReadings++;
                } else {
                    customersWithoutReadings++;
                }
            }

            return createBulkSuccessResponse(areaCode, targetBillCycle, areaName, 
                    customers.size(), customersWithReadings, customersWithoutReadings, meterReadings);

        } catch (Exception e) {
            return createBulkErrorResponse("Failed to retrieve bulk meter reading information: " + e.getMessage());
        }
    }

    /**
     * Get meter reading details for a customer - UPDATED VERSION
     */
    private MeterReadingInfoDetailsDTO getMeterReadingDetails(BulkCustomer customer, String areaCode, String billCycle) {
        MeterReadingInfoDetailsDTO dto = new MeterReadingInfoDetailsDTO();

        // Basic customer information
        dto.setAccountNumber(customer.getAccNbr());
        dto.setTariff(customer.getTariff());
        dto.setReaderCode(customer.getRedCode());
        dto.setDailyPack(customer.getDlyPack());
        dto.setWalkOrder(customer.getWlkOrd());
        dto.setAreaCode(areaCode);
        dto.setInstallationId(customer.getInstId());
        dto.setCustomerCategory(customer.getCusCat());
        dto.setCurrentBillCycle(billCycle);

        // Get area name
        areaRepository.findByAreaCode(areaCode)
                .ifPresent(area -> dto.setAreaName(area.getAreaName()));

        // Get bill cycle date from config table
        try {
            Integer billCycleInt = Integer.parseInt(billCycle);
            billCycleConfigRepository.findByAreaCodeAndBillCycle(areaCode, billCycleInt)
                    .ifPresent(config -> {
                        if (config.getEnteredDate() != null) {
                            dto.setBillCycleDate(Date.from(config.getEnteredDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
                        }
                    });
        } catch (NumberFormatException e) {
            // Log error but continue
            System.err.println("Invalid bill cycle format: " + billCycle);
        }

        // Get temp readings for this customer and bill cycle
        List<TmpReadings> tempReadings = tmpReadingsRepository.findByAccNbrAndAreaCdAndAddedBlcy(
                customer.getAccNbr(), areaCode, billCycle);

        boolean hasReading = !tempReadings.isEmpty();
        dto.setHasReading(hasReading);
        dto.setReadingStatus(hasReading ? "RECEIVED" : "PENDING");

        if (hasReading) {
            // Process meter types
            List<MeterTypeDetailsDTO> meterTypes = processMeterTypes(tempReadings);
            dto.setMeterTypes(meterTypes);

            // Get reading dates and calculate number of days
            processReadingDates(tempReadings, dto);

            // Get meter sequence (from first reading)
            if (!tempReadings.isEmpty()) {
                dto.setMeterSequence(tempReadings.get(0).getMtrSeq());
            }

            // ==== FIXED SECTION: Get charges from tmp_mon_tot ====
            Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
            if (tmpMonTotOpt.isPresent()) {
                TmpMonTot tmpMonTot = tmpMonTotOpt.get();
                dto.setFixedCharge(tmpMonTot.getFixedChg() != null ? tmpMonTot.getFixedChg() : BigDecimal.ZERO);
                dto.setMonthlyCharge(tmpMonTot.getTotCharge() != null ? tmpMonTot.getTotCharge() : BigDecimal.ZERO);
                dto.setVatAmount(tmpMonTot.getTotGst() != null ? tmpMonTot.getTotGst() : BigDecimal.ZERO);
                dto.setTotalAmount(tmpMonTot.getTotAmt() != null ? tmpMonTot.getTotAmt() : BigDecimal.ZERO);
            } else {
                // Set default values if no tmp_mon_tot record found
                dto.setFixedCharge(BigDecimal.ZERO);
                dto.setMonthlyCharge(BigDecimal.ZERO);
                dto.setVatAmount(BigDecimal.ZERO);
                dto.setTotalAmount(BigDecimal.ZERO);
            }

            // ==== FIXED SECTION: Get BF Balance from mon_tot of previous bill cycle ====
            try {
                Integer currentBillCycleInt = Integer.parseInt(billCycle);
                Integer previousBillCycleInt = currentBillCycleInt - 1;
                String previousBillCycle = String.valueOf(previousBillCycleInt);
                
                // Get crnt_bal from previous bill cycle (not bf_bal from current cycle)
                Optional<MonTot> previousMonTotOpt = monTotRepository.findByAccNbrAndBillCycle(
                    customer.getAccNbr(), previousBillCycle);
                
                if (previousMonTotOpt.isPresent()) {
                    // Use crnt_bal from previous cycle as B/F Balance
                    MonTot previousMonTot = previousMonTotOpt.get();
                    BigDecimal bfBalance = previousMonTot.getCrntBal() != null ? 
                        previousMonTot.getCrntBal() : BigDecimal.ZERO;
                    dto.setBfBalance(bfBalance);
                } else {
                    // No previous bill cycle record found
                    dto.setBfBalance(BigDecimal.ZERO);
                }
            } catch (NumberFormatException e) {
                // Invalid bill cycle format
                dto.setBfBalance(BigDecimal.ZERO);
            }
            
        } else {
            // No readings - set empty values
            dto.setMeterTypes(new ArrayList<>());
            dto.setNumberOfDays(0);
            dto.setMeterSequence(0);
            dto.setFixedCharge(BigDecimal.ZERO);
            dto.setMonthlyCharge(BigDecimal.ZERO);
            dto.setVatAmount(BigDecimal.ZERO);
            dto.setTotalAmount(BigDecimal.ZERO);
            dto.setBfBalance(BigDecimal.ZERO);
        }

        return dto;
    }

    /**
     * Process meter types from temp readings
     */
    private List<MeterTypeDetailsDTO> processMeterTypes(List<TmpReadings> tempReadings) {
        // Group readings by meter type
        Map<String, List<TmpReadings>> readingsByType = tempReadings.stream()
                .collect(Collectors.groupingBy(TmpReadings::getMtrType));

        List<MeterTypeDetailsDTO> meterTypes = new ArrayList<>();

        // Process each meter type
        for (Map.Entry<String, List<TmpReadings>> entry : readingsByType.entrySet()) {
            String meterType = entry.getKey();
            List<TmpReadings> typeReadings = entry.getValue();

            // Use the first reading for this meter type (should be only one per type per reading date)
            if (!typeReadings.isEmpty()) {
                TmpReadings reading = typeReadings.get(0);

                MeterTypeDetailsDTO meterTypeDTO = new MeterTypeDetailsDTO();
                meterTypeDTO.setMeterType(meterType);
                meterTypeDTO.setMeterNumber(reading.getMtrNbr());
                meterTypeDTO.setPresentReading(reading.getPrsntRdn());
                meterTypeDTO.setPreviousReading(reading.getPrvRdn());
                meterTypeDTO.setUnits(reading.getUnits());
                meterTypeDTO.setAssessedCode(reading.getAcode());
                meterTypeDTO.setMultipliedBy(reading.getMFactor() != null ? reading.getMFactor() : BigDecimal.ZERO);
                meterTypeDTO.setRate(reading.getRate() != null ? reading.getRate() : BigDecimal.ZERO);
                meterTypeDTO.setAmount(reading.getComputedChg() != null ? reading.getComputedChg() : BigDecimal.ZERO);

                meterTypes.add(meterTypeDTO);
            }
        }

        return meterTypes;
    }

    /**
     * Process reading dates and calculate number of days
     */
    private void processReadingDates(List<TmpReadings> tempReadings, MeterReadingInfoDetailsDTO dto) {
        if (!tempReadings.isEmpty()) {
            TmpReadings firstReading = tempReadings.get(0);
            dto.setReadingDate(firstReading.getRdngDate());
            dto.setPreviousReadingDate(firstReading.getPrvDate());

            // Calculate number of days between reading dates
            if (firstReading.getRdngDate() != null && firstReading.getPrvDate() != null) {
                long diffInMillis = firstReading.getRdngDate().getTime() - firstReading.getPrvDate().getTime();
                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
                dto.setNumberOfDays((int) diffInDays);
            } else {
                dto.setNumberOfDays(0);
            }
        }
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

        SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
        
        // Check access based on user category
        if (areaCode != null) {
            boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, 
                    userInfo.getRegionCode(), userInfo.getProvinceCode(), areaCode);
            if (!hasAccess) {
                throw new RuntimeException("Access denied to area: " + areaCode);
            }
        }
    }

    // Response creation methods
    private MeterReadingInfoResponse createSuccessResponse(String message, MeterReadingInfoDetailsDTO readingInfo) {
        MeterReadingInfoResponse response = new MeterReadingInfoResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setMeterReadingInfo(readingInfo);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MeterReadingInfoResponse createErrorResponse(String message) {
        MeterReadingInfoResponse response = new MeterReadingInfoResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkMeterReadingResponse createBulkSuccessResponse(String areaCode, String activeBillCycle, 
                                                             String areaName, int totalCustomers, 
                                                             int withReadings, int withoutReadings,
                                                             List<MeterReadingInfoDetailsDTO> readings) {
        BulkMeterReadingResponse response = new BulkMeterReadingResponse();
        response.setSuccess(true);
        response.setMessage("Bulk meter reading information retrieved successfully");
        response.setAreaCode(areaCode);
        response.setActiveBillCycle(activeBillCycle);
        response.setTotalCustomers(totalCustomers);
        response.setCustomersWithReadings(withReadings);
        response.setCustomersWithoutReadings(withoutReadings);
        response.setMeterReadings(readings);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private BulkMeterReadingResponse createBulkErrorResponse(String message) {
        BulkMeterReadingResponse response = new BulkMeterReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    /**
 * Edit meter readings for a customer - FIXED VERSION
 */
@Transactional
public MeterReadingEditResponse editMeterReadings(String sessionId, String userId, 
                                                String accountNumber, String areaCode, 
                                                String billCycle, Date readingDate,
                                                List<MeterReadingEditDTO> meterReadings) {
    try {
        // Validate session and access
        validateSessionAndAccess(sessionId, userId, areaCode);

        // Get customer information
        Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
        if (!customerOpt.isPresent()) {
            return createEditErrorResponse("Customer not found with account number: " + accountNumber);
        }

        BulkCustomer customer = customerOpt.get();

        // Validate area code matches customer's area
        if (!areaCode.equals(customer.getAreaCd())) {
            return createEditErrorResponse("Customer does not belong to area: " + areaCode);
        }

        // Validate reading date is not in the future
        if (isFutureDate(readingDate)) {
            return createEditErrorResponse("Reading date cannot be a future date");
        }

        // Get existing temp readings for this customer and bill cycle
        List<TmpReadings> existingReadings = tmpReadingsRepository.findByAccNbrAndAreaCdAndAddedBlcy(
                customer.getAccNbr(), areaCode, billCycle);

        if (existingReadings.isEmpty()) {
            return createEditErrorResponse("No existing readings found for editing");
        }

        // Update readings based on the edit request
        boolean hasUpdates = updateReadings(existingReadings, readingDate, meterReadings, userId);

        if (!hasUpdates) {
            return createEditErrorResponse("No valid updates provided");
        }

        // Note: Reading date updates are handled within updateReadings method
        // Present reading updates are saved here
        if (!meterReadings.isEmpty()) {
            tmpReadingsRepository.saveAll(existingReadings);
            tmpReadingsRepository.flush();
        }

        // Recalculate and update TmpMonTot if needed
        updateTmpMonTot(customer.getAccNbr(), areaCode, billCycle, userId);

        // Get updated meter reading information
        MeterReadingInfoDetailsDTO updatedReadingInfo = getMeterReadingDetails(customer, areaCode, billCycle);

        return createEditSuccessResponse("Meter readings updated successfully", updatedReadingInfo);

    } catch (Exception e) {
        e.printStackTrace(); // Add proper logging
        return createEditErrorResponse("Failed to update meter readings: " + e.getMessage());
    }
}

    /**
 * Update readings based on edit request - FIXED VERSION
 */
private boolean updateReadings(List<TmpReadings> existingReadings, Date newReadingDate, 
                              List<MeterReadingEditDTO> meterReadings, String userId) {
    boolean hasUpdates = false;
    Date currentTime = new Date();

    // IMPORTANT: For Informix, we cannot update primary key fields directly
    // We need to handle date updates differently
    
    // Update present readings for specific meter types FIRST
    for (MeterReadingEditDTO editDTO : meterReadings) {
        if (editDTO.getPresentReading() != null) {
            for (TmpReadings reading : existingReadings) {
                if (reading.getMtrType().trim().equals(editDTO.getMeterType().trim())) {
                    
                    // Special handling for KVAH/KVArh meter type
                    if ("KVAH".equals(editDTO.getMeterType().trim()) || "KVArh".equals(editDTO.getMeterType().trim())) {
                        // For KVAH/KVArh, update units instead of present reading
                        reading.setUnits(editDTO.getPresentReading());
                        // Recalculate computed charge if rate exists
                        if (reading.getRate() != null && reading.getRate().compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal computedChg = reading.getRate().multiply(new BigDecimal(editDTO.getPresentReading()));
                            reading.setComputedChg(computedChg);
                        }
                    } else {
                        // For other meter types, update present reading and recalculate units
                        int previousReading = reading.getPrvRdn() != null ? reading.getPrvRdn() : 0;
                        int newUnits = editDTO.getPresentReading() - previousReading;
                        
                        if (newUnits >= 0) {
                            reading.setPrsntRdn(editDTO.getPresentReading());
                            reading.setUnits(newUnits);
                            
                            // Recompute charge if rate exists
                            if (reading.getRate() != null && reading.getRate().compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal computedChg = reading.getRate().multiply(new BigDecimal(newUnits));
                                reading.setComputedChg(computedChg);
                            }
                        }
                    }
                    
                    reading.setEditedDtime(currentTime);
                    reading.setEditedUserId(userId);
                    hasUpdates = true;
                    break;
                }
            }
        }
    }

    // Handle reading date update SEPARATELY with native query
    if (newReadingDate != null) {
        boolean dateChanged = false;
        
        // Check if any reading date is different
        for (TmpReadings reading : existingReadings) {
            if (!reading.getRdngDate().equals(newReadingDate)) {
                dateChanged = true;
                break;
            }
        }
        
        if (dateChanged) {
            // Use native query to update reading date for all records
            updateReadingDateWithNativeQuery(existingReadings, newReadingDate, userId);
            hasUpdates = true;
        }
    }

    return hasUpdates;
}

/**
 * Update reading date using native query to avoid primary key issues - FIXED
 */
private void updateReadingDateWithNativeQuery(List<TmpReadings> existingReadings, Date newReadingDate, String userId) {
    try {
        // Get the first reading to extract common identifiers
        TmpReadings firstReading = existingReadings.get(0);
        
        String accNbr = firstReading.getAccNbr();
        String areaCd = firstReading.getAreaCd();
        String addedBlcy = firstReading.getAddedBlcy();
        Integer mtrSeq = firstReading.getMtrSeq();
        Date oldReadingDate = firstReading.getRdngDate();
        Date currentTime = new Date();
        
        // Execute native update - REMOVE the query string parameter
        int updatedRows = tmpReadingsRepository.executeNativeUpdate(
            newReadingDate,    // ?1 - newRdngDate
            currentTime,       // ?2 - editedDtime  
            userId,            // ?3 - editedUserId
            accNbr,            // ?4 - accNbr
            areaCd,            // ?5 - areaCd
            addedBlcy,         // ?6 - addedBlcy
            mtrSeq,            // ?7 - mtrSeq
            oldReadingDate     // ?8 - oldRdngDate
        );
        
        System.out.println("Updated " + updatedRows + " reading records with new date");
        
    } catch (Exception e) {
        System.err.println("Error updating reading date with native query: " + e.getMessage());
        throw new RuntimeException("Failed to update reading date: " + e.getMessage());
    }
}

    /**
     * Update TmpMonTot with recalculated totals
     */
    private void updateTmpMonTot(String accNbr, String areaCode, String billCycle, String userId) {
        try {
            Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(accNbr, billCycle);
            
            if (tmpMonTotOpt.isPresent()) {
                TmpMonTot tmpMonTot = tmpMonTotOpt.get();
                
                // Get updated readings to recalculate totals
                List<TmpReadings> updatedReadings = tmpReadingsRepository.findByAccNbrAndAreaCdAndAddedBlcy(
                        accNbr, areaCode, billCycle);
                
                // Recalculate totals based on updated readings
                BigDecimal totalCharge = BigDecimal.ZERO;
                BigDecimal totalUnitsKwo = BigDecimal.ZERO;
                BigDecimal totalUnitsKwd = BigDecimal.ZERO;
                BigDecimal totalUnitsKwp = BigDecimal.ZERO;
                BigDecimal totalKva = BigDecimal.ZERO;
                
                for (TmpReadings reading : updatedReadings) {
                    if (reading.getComputedChg() != null) {
                        totalCharge = totalCharge.add(reading.getComputedChg());
                    }
                    
                    // Accumulate units by meter type
                    if (reading.getUnits() != null) {
                        switch (reading.getMtrType().trim()) {
                            case "KWO":
                                totalUnitsKwo = totalUnitsKwo.add(new BigDecimal(reading.getUnits()));
                                break;
                            case "KWD":
                                totalUnitsKwd = totalUnitsKwd.add(new BigDecimal(reading.getUnits()));
                                break;
                            case "KWP":
                                totalUnitsKwp = totalUnitsKwp.add(new BigDecimal(reading.getUnits()));
                                break;
                            case "KVA":
                                totalKva = totalKva.add(new BigDecimal(reading.getUnits()));
                                break;
                            case "KVAH":
                                // KVAH units are already handled in the units field
                                break;
                        }
                    }
                }
                
                // Update TmpMonTot
                tmpMonTot.setTotUntsKwo(totalUnitsKwo);
                tmpMonTot.setTotUntsKwd(totalUnitsKwd);
                tmpMonTot.setTotUntsKwp(totalUnitsKwp);
                tmpMonTot.setTotKva(totalKva);
                tmpMonTot.setTotCharge(totalCharge);
                
                // Recalculate total amount (total charge + fixed charge - VAT)
                BigDecimal fixedChg = tmpMonTot.getFixedChg() != null ? tmpMonTot.getFixedChg() : BigDecimal.ZERO;
                BigDecimal vat = tmpMonTot.getTotGst() != null ? tmpMonTot.getTotGst() : BigDecimal.ZERO;
                BigDecimal totalAmount = totalCharge.add(fixedChg).subtract(vat);
                tmpMonTot.setTotAmt(totalAmount.max(BigDecimal.ZERO));
                
                tmpMonTot.setEditedDtime(new Date());
                tmpMonTot.setEditedUserId(userId);
                
                tmpMonTotRepository.save(tmpMonTot);
            }
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Failed to update TmpMonTot: " + e.getMessage());
        }
    }

    /**
     * Check if date is in the future
     */
    private boolean isFutureDate(Date date) {
        return date.after(new Date());
    }

    // Response creation methods for edit operations
    private MeterReadingEditResponse createEditSuccessResponse(String message, MeterReadingInfoDetailsDTO readingInfo) {
        MeterReadingEditResponse response = new MeterReadingEditResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setUpdatedMeterReadingInfo(readingInfo);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private MeterReadingEditResponse createEditErrorResponse(String message) {
        MeterReadingEditResponse response = new MeterReadingEditResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}