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
     * Get meter reading details for a customer
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

            // Get charges from tmp_mon_tot
            Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
            if (tmpMonTotOpt.isPresent()) {
                TmpMonTot tmpMonTot = tmpMonTotOpt.get();
                dto.setFixedCharge(tmpMonTot.getFixedChg());
                dto.setMonthlyCharge(tmpMonTot.getTotCharge());
                dto.setVatAmount(tmpMonTot.getTotGst());
                dto.setTotalAmount(tmpMonTot.getTotAmt());
            } else {
                // Set default values if no tmp_mon_tot record found
                dto.setFixedCharge(BigDecimal.ZERO);
                dto.setMonthlyCharge(BigDecimal.ZERO);
                dto.setVatAmount(BigDecimal.ZERO);
                dto.setTotalAmount(BigDecimal.ZERO);
            }

            // Get BF Balance from mon_tot
            Optional<MonTot> monTotOpt = monTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
            if (monTotOpt.isPresent()) {
                dto.setBfBalance(monTotOpt.get().getBfBal());
            } else {
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
}