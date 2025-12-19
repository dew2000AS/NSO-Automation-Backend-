package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO;
import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO.*;
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
public class PendingMeterReadingsService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private MonTotRepository monTotRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private MtrDetailRepository mtrDetailRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Get all pending meter readings for an area (with or without specific bill cycle)
     */
    public PendingMeterReadingResponse getPendingMeterReadingsForArea(String sessionId, String userId, 
                                                                     String areaCode, String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get target bill cycle (active if not provided)
            String targetBillCycle = getTargetBillCycle(areaCode, billCycle);

            // Get all customers in the area
            List<BulkCustomer> areaCustomers = bulkCustomerRepository.findByAreaCd(areaCode);
            
            // Filter customers without readings for the target bill cycle
            List<PendingMeterReadingDetailsDTO> pendingReadings = new ArrayList<>();
            
            for (BulkCustomer customer : areaCustomers) {
                boolean hasReading = tmpReadingsRepository.hasReadingsForBillCycle(
                    customer.getAccNbr(), areaCode, targetBillCycle);
                
                if (!hasReading) {
                    PendingMeterReadingDetailsDTO pendingReading = getPendingReadingDetails(
                        customer, areaCode, targetBillCycle);
                    pendingReadings.add(pendingReading);
                }
            }

            return createSuccessResponse("Pending meter readings retrieved successfully", pendingReadings);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve pending meter readings: " + e.getMessage());
        }
    }

    /**
     * Get single pending meter reading for a customer in an area
     */
    public SinglePendingReadingResponse getPendingMeterReadingForCustomer(String sessionId, String userId, 
                                                                         String accountNumber, String areaCode, 
                                                                         String billCycle) {
        try {
            // Validate session and access
            validateSessionAndAccess(sessionId, userId, areaCode);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(accountNumber);
            if (!customerOpt.isPresent()) {
                return createSingleErrorResponse("Customer not found with account number: " + accountNumber);
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code matches customer's area
            if (!areaCode.equals(customer.getAreaCd())) {
                return createSingleErrorResponse("Customer does not belong to area: " + areaCode);
            }

            // Get target bill cycle (active if not provided)
            String targetBillCycle = getTargetBillCycle(areaCode, billCycle);

            // Check if customer already has readings
            boolean hasReading = tmpReadingsRepository.hasReadingsForBillCycle(
                customer.getAccNbr(), areaCode, targetBillCycle);
            
            if (hasReading) {
                return createSingleErrorResponse("Customer already has meter readings for bill cycle: " + targetBillCycle);
            }

            // Get pending reading details
            PendingMeterReadingDetailsDTO pendingReading = getPendingReadingDetails(customer, areaCode, targetBillCycle);

            return createSingleSuccessResponse("Pending meter reading retrieved successfully", pendingReading);

        } catch (Exception e) {
            return createSingleErrorResponse("Failed to retrieve pending meter reading: " + e.getMessage());
        }
    }

    /**
     * Get pending meter reading details for a customer
     */
    private PendingMeterReadingDetailsDTO getPendingReadingDetails(BulkCustomer customer, String areaCode, String billCycle) {
        PendingMeterReadingDetailsDTO dto = new PendingMeterReadingDetailsDTO();

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
        dto.setVatApplicable(customer.getGstApl()); // "Y" or null

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

        // Get previous reading information
        setPreviousReadingInfo(customer, areaCode, dto);

        // Get meter sequence and B/F balance
        setMeterSequenceAndBalance(customer, areaCode, billCycle, dto);

        // Get meter types and details
        setMeterTypesDetails(customer, dto);

        // Set default values for user-input fields
        dto.setReadingDate(null); // User will select
        dto.setNumberOfDays(0); // Initially zero

        return dto;
    }

    /**
     * Set previous reading information
     */
    private void setPreviousReadingInfo(BulkCustomer customer, String areaCode, PendingMeterReadingDetailsDTO dto) {
        try {
            // Find the latest previous reading from tmp_rdngs table
            List<TmpReadings> previousReadings = tmpReadingsRepository.findLatestReadingsByAccNbr(customer.getAccNbr());
            
            if (!previousReadings.isEmpty()) {
                TmpReadings latestReading = previousReadings.get(0);
                dto.setPreviousReadingDate(latestReading.getRdngDate());
                
                // Note: We don't set previous reading here as it's meter-specific
                // Previous reading will be set per meter type in setMeterTypesDetails
            }
        } catch (Exception e) {
            System.err.println("Error getting previous reading info for customer " + customer.getAccNbr() + ": " + e.getMessage());
        }
    }

    /**
     * Set meter sequence and B/F balance with improved error handling
     */
    private void setMeterSequenceAndBalance(BulkCustomer customer, String areaCode, String billCycle, PendingMeterReadingDetailsDTO dto) {
        try {
            // Get meter sequence from mtr_detail (use first meter's sequence)
            List<MtrDetail> meterDetails = mtrDetailRepository.findByInstId(customer.getInstId());
            if (!meterDetails.isEmpty()) {
                Integer meterSeq = meterDetails.get(0).getMtrSeq();
                dto.setMeterSequence(meterSeq != null ? meterSeq : 0);
            } else {
                dto.setMeterSequence(0);
                System.out.println("No meter details found for installation ID: " + customer.getInstId());
            }

            // Get B/F Balance from mon_tot
            Optional<MonTot> monTotOpt = monTotRepository.findByAccNbrAndBillCycle(customer.getAccNbr(), billCycle);
            if (monTotOpt.isPresent()) {
                BigDecimal bfBalance = monTotOpt.get().getCrntBal();
                dto.setBfBalance(bfBalance != null ? bfBalance : BigDecimal.ZERO);
            } else {
                dto.setBfBalance(BigDecimal.ZERO);
                System.out.println("No mon_tot record found for account: " + customer.getAccNbr() + ", bill cycle: " + billCycle);
            }
        } catch (Exception e) {
            System.err.println("Error getting meter sequence and balance for customer " + customer.getAccNbr() + ": " + e.getMessage());
            e.printStackTrace();
            dto.setMeterSequence(0);
            dto.setBfBalance(BigDecimal.ZERO);
        }
    }

    /**
     * Set meter types and details with improved error handling
     */
    private void setMeterTypesDetails(BulkCustomer customer, PendingMeterReadingDetailsDTO dto) {
        List<MeterTypePendingDTO> meterTypes = new ArrayList<>();
        
        try {
            // Get all meter types for this customer from mtr_detail
            List<MtrDetail> meterDetails = mtrDetailRepository.findByInstId(customer.getInstId());
            
            if (meterDetails.isEmpty()) {
                System.out.println("No meter details found for installation ID: " + customer.getInstId());
            }
            
            for (MtrDetail meterDetail : meterDetails) {
                MeterTypePendingDTO meterTypeDTO = new MeterTypePendingDTO();
                meterTypeDTO.setMeterType(meterDetail.getMtrType() != null ? meterDetail.getMtrType().trim() : "");
                meterTypeDTO.setMeterNumber(meterDetail.getMtrNbr() != null ? meterDetail.getMtrNbr().trim() : "");
                meterTypeDTO.setMultipliedBy(meterDetail.getMFactor() != null ? meterDetail.getMFactor() : BigDecimal.ONE);
                meterTypeDTO.setPresentReading(null); // User will enter
                
                // Get previous reading for this specific meter type
                Integer previousReading = getPreviousReadingForMeterType(customer.getAccNbr(), meterDetail.getMtrType());
                meterTypeDTO.setPreviousReading(previousReading != null ? previousReading : 0);
                
                meterTypes.add(meterTypeDTO);
            }
            
            dto.setMeterTypes(meterTypes);
            dto.setTotalMeters(meterTypes.size());
            
        } catch (Exception e) {
            System.err.println("Error getting meter types for customer " + customer.getAccNbr() + ": " + e.getMessage());
            e.printStackTrace();
            dto.setMeterTypes(new ArrayList<>());
            dto.setTotalMeters(0);
        }
    }

    /**
     * Get previous reading for specific meter type with improved error handling
     */
    private Integer getPreviousReadingForMeterType(String accNbr, String meterType) {
        if (meterType == null) {
            return 0;
        }
        
        try {
            // Find the latest reading for this meter type
            List<TmpReadings> allReadings = tmpReadingsRepository.findByAccNbr(accNbr);
            
            List<TmpReadings> meterReadings = allReadings.stream()
                    .filter(reading -> reading.getMtrType() != null && 
                            reading.getMtrType().trim().equals(meterType.trim()))
                    .sorted((r1, r2) -> {
                        if (r1.getRdngDate() == null) return 1;
                        if (r2.getRdngDate() == null) return -1;
                        return r2.getRdngDate().compareTo(r1.getRdngDate());
                    })
                    .collect(Collectors.toList());
            
            if (!meterReadings.isEmpty()) {
                Integer presentReading = meterReadings.get(0).getPrsntRdn();
                return presentReading != null ? presentReading : 0;
            } else {
                System.out.println("No previous readings found for account: " + accNbr + ", meter type: " + meterType);
            }
        } catch (Exception e) {
            System.err.println("Error getting previous reading for meter type " + meterType + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get target bill cycle (active if not provided)
     */
    private String getTargetBillCycle(String areaCode, String billCycle) {
        if (billCycle != null && !billCycle.trim().isEmpty()) {
            return billCycle;
        }
        
        // Get active bill cycle for the area
        Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
        if (!activeBillCycleOpt.isPresent()) {
            throw new RuntimeException("No active bill cycle found for area: " + areaCode);
        }
        
        return activeBillCycleOpt.get().toString();
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
    private PendingMeterReadingResponse createSuccessResponse(String message, List<PendingMeterReadingDetailsDTO> pendingReadings) {
        PendingMeterReadingResponse response = new PendingMeterReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPendingReadings(pendingReadings);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private PendingMeterReadingResponse createErrorResponse(String message) {
        PendingMeterReadingResponse response = new PendingMeterReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private SinglePendingReadingResponse createSingleSuccessResponse(String message, PendingMeterReadingDetailsDTO pendingReading) {
        SinglePendingReadingResponse response = new SinglePendingReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPendingReading(pendingReading);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private SinglePendingReadingResponse createSingleErrorResponse(String message) {
        SinglePendingReadingResponse response = new SinglePendingReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}