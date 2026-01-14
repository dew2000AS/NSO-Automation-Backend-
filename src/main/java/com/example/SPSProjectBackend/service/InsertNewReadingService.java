// FILE: src/main/java/com/example/SPSProjectBackend/service/InsertNewReadingService.java
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InsertNewReadingDTO;
import com.example.SPSProjectBackend.dto.InsertNewReadingDTO.*;
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
@Transactional
public class InsertNewReadingService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private MtrDetailRepository mtrDetailRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Insert new meter readings for a customer
     * This creates records in both tmp_rdngs and tmp_mon_tot tables
     */
    public InsertNewReadingResponse insertNewMeterReadings(InsertNewReadingRequest request) {
        try {
            // Validate session and access
            validateSessionAndAccess(request.getSessionId(), request.getUserId(), request.getAreaCode());

            // Validate required fields
            validateInsertRequest(request);

            // Get customer information
            Optional<BulkCustomer> customerOpt = bulkCustomerRepository.findByAccNbr(request.getAccountNumber());
            if (!customerOpt.isPresent()) {
                return createErrorResponse("Customer not found with account number: " + request.getAccountNumber());
            }

            BulkCustomer customer = customerOpt.get();

            // Validate area code matches customer's area
            if (!request.getAreaCode().equals(customer.getAreaCd())) {
                return createErrorResponse("Customer does not belong to area: " + request.getAreaCode());
            }

            // Check if customer already has readings for this bill cycle
            boolean hasExistingReadings = tmpReadingsRepository.hasReadingsForBillCycle(
                request.getAccountNumber(), request.getAreaCode(), request.getBillCycle());
            
            if (hasExistingReadings) {
                return createErrorResponse("Customer already has readings for bill cycle: " + request.getBillCycle());
            }

            // Get meter details for validation
            List<MtrDetail> meterDetails = mtrDetailRepository.findByInstId(customer.getInstId());
            
            // Process and insert meter readings
            List<String> insertedMeterTypes = insertMeterReadings(
                customer, request, meterDetails, request.getUserId());
            
            // Insert/Update tmp_mon_tot record
            insertTmpMonTot(customer, request, request.getUserId());
            
            return createSuccessResponse(
                "New meter readings inserted successfully", 
                insertedMeterTypes.size(),
                insertedMeterTypes
            );

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Failed to insert new meter readings: " + e.getMessage());
        }
    }

    /**
     * Insert meter readings into tmp_rdngs table
     */
    private List<String> insertMeterReadings(BulkCustomer customer, 
                                            InsertNewReadingRequest request,
                                            List<MtrDetail> meterDetails,
                                            String userId) {
        List<TmpReadings> readingsToInsert = new ArrayList<>();
        List<String> insertedMeterTypes = new ArrayList<>();
        Date currentTime = new Date();
        
        // FIXED: Create a map of meter details, but handle duplicates by taking the latest one
        Map<String, MtrDetail> meterDetailMap = new HashMap<>();
        for (MtrDetail meterDetail : meterDetails) {
            String meterType = meterDetail.getMtrType() != null ? meterDetail.getMtrType().trim() : "";
            if (!meterType.isEmpty()) {
                // Check if we already have this meter type in the map
                if (!meterDetailMap.containsKey(meterType)) {
                    // First occurrence, add it
                    meterDetailMap.put(meterType, meterDetail);
                } else {
                    // Compare effect dates to keep the latest one
                    MtrDetail existing = meterDetailMap.get(meterType);
                    if (meterDetail.getEffctDate() != null && 
                        (existing.getEffctDate() == null || 
                         meterDetail.getEffctDate().after(existing.getEffctDate()))) {
                        meterDetailMap.put(meterType, meterDetail);
                    }
                }
            }
        }
        
        // Get installation ID from customer
        String installationId = customer.getInstId();
        
        // Get meter sequence (use provided or from meter details)
        Integer meterSequence = request.getMeterSequence();
        if (meterSequence == null && !meterDetails.isEmpty()) {
            meterSequence = meterDetails.get(0).getMtrSeq();
        }
        if (meterSequence == null) {
            meterSequence = 1; // Default value
        }
        
        // Get previous reading date (from last reading)
        Date previousReadingDate = getPreviousReadingDate(customer.getAccNbr());
        
        // Process each meter reading from request
        for (MeterReadingInsertDTO meterReading : request.getMeterReadings()) {
            // Skip if present reading is null (user didn't enter reading for this meter type)
            if (meterReading.getPresentReading() == null) {
                continue;
            }
            
            String meterType = meterReading.getMeterType().trim();
            
            // Get meter detail for this type
            MtrDetail meterDetail = meterDetailMap.get(meterType);
            if (meterDetail == null) {
                // Log warning but continue - user might be inserting a reading for a meter type not in mtr_detail
                System.out.println("Info: No meter detail found for meter type: " + meterType + 
                    ". Using provided values from request.");
            }
            
            // Create new TmpReadings entity
            TmpReadings newReading = new TmpReadings();
            
            // Set primary key fields
            newReading.setAccNbr(request.getAccountNumber());
            newReading.setAreaCd(request.getAreaCode());
            newReading.setAddedBlcy(request.getBillCycle());
            newReading.setMtrSeq(meterSequence);
            newReading.setMtrType(meterType);
            
            // Set installation ID
            newReading.setInstId(installationId);
            
            // Set dates
            newReading.setRdngDate(request.getReadingDate());
            newReading.setPrvDate(previousReadingDate);
            
            // Set readings
            newReading.setPrsntRdn(meterReading.getPresentReading());
            newReading.setPrvRdn(meterReading.getPreviousReading() != null ? 
                meterReading.getPreviousReading() : 0);
            
            // Set calculated values
            if (meterReading.getUnits() != null) {
                newReading.setUnits(meterReading.getUnits());
            } else {
                // Calculate units if not provided
                Integer units = calculateUnits(
                    meterReading.getPresentReading(), 
                    meterReading.getPreviousReading());
                newReading.setUnits(units);
            }
            
            // Set meter number
            if (meterReading.getMeterNumber() != null) {
                newReading.setMtrNbr(meterReading.getMeterNumber());
            } else if (meterDetail != null && meterDetail.getMtrNbr() != null) {
                newReading.setMtrNbr(meterDetail.getMtrNbr());
            }
            
            // Set other fields
            if (meterReading.getRate() != null) {
                newReading.setRate(meterReading.getRate());
            }
            
            if (meterReading.getAmount() != null) {
                newReading.setComputedChg(meterReading.getAmount());
            } else if (meterReading.getRate() != null && newReading.getUnits() != null) {
                // Calculate amount if not provided
                BigDecimal amount = meterReading.getRate()
                    .multiply(new BigDecimal(newReading.getUnits()));
                newReading.setComputedChg(amount);
            }
            
            // Set assessed code (default to null = Not-Assessed)
            newReading.setAcode(meterReading.getAssessedCode());
            
            // Set multiplied by factor
            if (meterReading.getMultipliedBy() != null) {
                newReading.setMFactor(meterReading.getMultipliedBy());
            } else if (meterDetail != null && meterDetail.getMFactor() != null) {
                newReading.setMFactor(meterDetail.getMFactor());
            } else {
                newReading.setMFactor(new BigDecimal("1.000"));
            }
            
            // Set default status values
            newReading.setBillStat(null); // Will be set during billing
            newReading.setErrStat(0); // No errors initially
            newReading.setMtrStat("A"); // Active
            newReading.setRdnStat("R"); // Received
            
            // Set user and timestamps
            newReading.setUserId(userId);
            newReading.setEnteredDtime(currentTime);
            newReading.setEditedUserId(userId);
            newReading.setEditedDtime(currentTime);
            
            readingsToInsert.add(newReading);
            insertedMeterTypes.add(meterType);
        }
        
        // Save all readings to database
        if (!readingsToInsert.isEmpty()) {
            tmpReadingsRepository.saveAll(readingsToInsert);
            tmpReadingsRepository.flush();
            System.out.println("Inserted " + readingsToInsert.size() + " meter readings for account: " + 
                request.getAccountNumber());
        }
        
        return insertedMeterTypes;
    }

    /**
     * Insert or update tmp_mon_tot record
     */
    private void insertTmpMonTot(BulkCustomer customer, 
                                InsertNewReadingRequest request,
                                String userId) {
        Date currentTime = new Date();
        
        // Check if tmp_mon_tot record already exists
        Optional<TmpMonTot> existingMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(
            request.getAccountNumber(), request.getBillCycle());
        
        TmpMonTot tmpMonTot;
        
        if (existingMonTotOpt.isPresent()) {
            // Update existing record
            tmpMonTot = existingMonTotOpt.get();
        } else {
            // Create new record
            tmpMonTot = new TmpMonTot();
            
            // Set primary key fields
            tmpMonTot.setAccNbr(request.getAccountNumber());
            tmpMonTot.setBillCycle(request.getBillCycle());
            
            // Set default values for new record
            tmpMonTot.setNewRederCd(customer.getRedCode());
            tmpMonTot.setNewDlyPack(customer.getDlyPack());
            tmpMonTot.setNewWlkOrd(customer.getWlkOrd());
            tmpMonTot.setErrStat(0);
            tmpMonTot.setCrntBal(BigDecimal.ZERO);
            tmpMonTot.setTotBillStat("N"); // New
            tmpMonTot.setUserId(userId);
            tmpMonTot.setEnteredDtime(currentTime);
        }
        
        // Update charges from request
        ChargesInsertDTO charges = request.getCharges();
        if (charges != null) {
            if (charges.getFixedCharge() != null) {
                tmpMonTot.setFixedChg(charges.getFixedCharge());
            }
            
            if (charges.getMonthlyCharge() != null) {
                tmpMonTot.setTotCharge(charges.getMonthlyCharge());
            }
            
            if (charges.getVatAmount() != null) {
                tmpMonTot.setTotGst(charges.getVatAmount());
            }
            
            if (charges.getTotalAmount() != null) {
                tmpMonTot.setTotAmt(charges.getTotalAmount());
            }
        }
        
        // Calculate total units from inserted readings (optional)
        calculateAndSetTotalUnits(request, tmpMonTot);
        
        // Set edit timestamps
        tmpMonTot.setEditedUserId(userId);
        tmpMonTot.setEditedDtime(currentTime);
        
        // Save to database
        tmpMonTotRepository.save(tmpMonTot);
        tmpMonTotRepository.flush();
        
        System.out.println("Saved tmp_mon_tot record for account: " + request.getAccountNumber());
    }

    /**
     * Calculate and set total units in tmp_mon_tot
     */
    private void calculateAndSetTotalUnits(InsertNewReadingRequest request, TmpMonTot tmpMonTot) {
        BigDecimal totalKwo = BigDecimal.ZERO;
        BigDecimal totalKwd = BigDecimal.ZERO;
        BigDecimal totalKwp = BigDecimal.ZERO;
        BigDecimal totalKva = BigDecimal.ZERO;
        BigDecimal totalCompChg = BigDecimal.ZERO;
        
        for (MeterReadingInsertDTO meterReading : request.getMeterReadings()) {
            if (meterReading.getUnits() == null || meterReading.getUnits() <= 0) {
                continue;
            }
            
            BigDecimal units = new BigDecimal(meterReading.getUnits());
            
            switch (meterReading.getMeterType().trim()) {
                case "KWO":
                    totalKwo = totalKwo.add(units);
                    break;
                case "KWD":
                    totalKwd = totalKwd.add(units);
                    break;
                case "KWP":
                    totalKwp = totalKwp.add(units);
                    break;
                case "KVA":
                    totalKva = totalKva.add(units);
                    break;
                case "KVAH":
                    // Handle KVAH if needed
                    break;
            }
            
            // Add to computed charges if amount is provided
            if (meterReading.getAmount() != null) {
                totalCompChg = totalCompChg.add(meterReading.getAmount());
            }
        }
        
        // Set totals in tmp_mon_tot
        tmpMonTot.setTotUntsKwo(totalKwo);
        tmpMonTot.setTotUntsKwd(totalKwd);
        tmpMonTot.setTotUntsKwp(totalKwp);
        tmpMonTot.setTotKva(totalKva);
        tmpMonTot.setTotCompChg(totalCompChg);
    }

    /**
     * Calculate units from present and previous readings
     */
    private Integer calculateUnits(Integer presentReading, Integer previousReading) {
        if (presentReading == null || previousReading == null) {
            return 0;
        }
        
        // Ensure units are not negative
        return Math.max(0, presentReading - previousReading);
    }

    /**
     * Get previous reading date from last reading
     */
    private Date getPreviousReadingDate(String accountNumber) {
        try {
            List<TmpReadings> latestReadings = tmpReadingsRepository.findLatestReadingsByAccNbr(accountNumber);
            if (!latestReadings.isEmpty()) {
                return latestReadings.get(0).getRdngDate();
            }
        } catch (Exception e) {
            System.err.println("Error getting previous reading date: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validate the insert request
     */
    private void validateInsertRequest(InsertNewReadingRequest request) {
        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (request.getAreaCode() == null || request.getAreaCode().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (request.getBillCycle() == null || request.getBillCycle().trim().isEmpty()) {
            throw new RuntimeException("Bill cycle is required");
        }
        if (request.getReadingDate() == null) {
            throw new RuntimeException("Reading date is required");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new RuntimeException("Session ID is required");
        }
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        if (request.getMeterReadings() == null || request.getMeterReadings().isEmpty()) {
            throw new RuntimeException("At least one meter reading is required");
        }
        
        // Validate reading date is not in the future
        if (isFutureDate(request.getReadingDate())) {
            throw new RuntimeException("Reading date cannot be a future date");
        }
        
        // Validate at least one meter reading has present reading
        boolean hasValidReading = false;
        for (MeterReadingInsertDTO reading : request.getMeterReadings()) {
            if (reading.getPresentReading() != null) {
                hasValidReading = true;
                break;
            }
        }
        
        if (!hasValidReading) {
            throw new RuntimeException("At least one meter reading must have a present reading value");
        }
    }

    /**
     * Check if date is in the future
     */
    private boolean isFutureDate(Date date) {
        return date.after(new Date());
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
    private InsertNewReadingResponse createSuccessResponse(String message, 
                                                          Integer insertedCount,
                                                          List<String> insertedMeterTypes) {
        InsertNewReadingResponse response = new InsertNewReadingResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setInsertedCount(insertedCount);
        response.setMeterReadingsInserted(insertedMeterTypes);
        response.setTimestamp(new Date().toString());
        return response;
    }

    private InsertNewReadingResponse createErrorResponse(String message) {
        InsertNewReadingResponse response = new InsertNewReadingResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(new Date().toString());
        return response;
    }
}