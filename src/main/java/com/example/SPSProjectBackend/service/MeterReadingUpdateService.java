// FILE: src/main/java/com/example/SPSProjectBackend/service/MeterReadingUpdateService.java
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MeterReadingUpdateDTO;
import com.example.SPSProjectBackend.dto.MeterReadingDetailUpdateDTO;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpMonTot;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.TmpMonTotRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MeterReadingUpdateService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private TmpMonTotRepository tmpMonTotRepository;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Update meter readings and charges based on user edits
     * IMPORTANT: If reading_date is provided, it will update for ALL meters of that customer
     * for the specified bill cycle, not just the meters in the request
     */
    public UpdateResponse updateMeterReadings(MeterReadingUpdateDTO updateDTO) {
        try {
            // Validate session and access
            validateSessionAndAccess(updateDTO.getSessionId(), updateDTO.getUserId(), updateDTO.getAreaCode());

            // Validate required fields
            validateUpdateRequest(updateDTO);

            // Get ALL existing readings for this account, area, and bill cycle
            List<TmpReadings> allCustomerReadings = tmpReadingsRepository
                .findAllByAccNbrAndAreaCdAndAddedBlcy(
                    updateDTO.getAccountNumber(),
                    updateDTO.getAreaCode(),
                    updateDTO.getBillCycle()
                );

            if (allCustomerReadings.isEmpty()) {
                return new UpdateResponse(false, "No meter readings found for this account and bill cycle");
            }

            Date currentTime = new Date();
            boolean hasUpdates = false;
            
            // 1. FIRST: Update reading date for ALL meters if reading_date is provided
            boolean readingDateUpdated = false;
            if (updateDTO.getReadingDate() != null) {
                readingDateUpdated = updateReadingDateForAllMeters(
                    updateDTO, allCustomerReadings, updateDTO.getUserId(), currentTime
                );
                if (readingDateUpdated) {
                    hasUpdates = true;
                    System.out.println("Reading date updated for all " + allCustomerReadings.size() + " meters");
                }
            }

            // 2. Update specific meter readings if provided
            if (updateDTO.getMeterReadings() != null && !updateDTO.getMeterReadings().isEmpty()) {
                for (MeterReadingDetailUpdateDTO meterUpdate : updateDTO.getMeterReadings()) {
                    if (updateSpecificMeterReading(updateDTO, meterUpdate, updateDTO.getUserId(), currentTime)) {
                        hasUpdates = true;
                    }
                }
            }

            // 3. Update tmp_mon_tot table if charge-related fields are provided
            boolean monTotUpdated = false;
            if (updateDTO.getFixedCharge() != null || updateDTO.getMonthlyCharge() != null || 
                updateDTO.getVatAmount() != null || updateDTO.getTotalAmount() != null) {
                
                Optional<TmpMonTot> tmpMonTotOpt = tmpMonTotRepository.findByAccNbrAndBillCycle(
                    updateDTO.getAccountNumber(), updateDTO.getBillCycle());

                if (tmpMonTotOpt.isPresent()) {
                    monTotUpdated = updateTmpMonTot(tmpMonTotOpt.get(), updateDTO, updateDTO.getUserId(), currentTime);
                    if (monTotUpdated) {
                        hasUpdates = true;
                        // Save the updated TmpMonTot
                        tmpMonTotRepository.save(tmpMonTotOpt.get());
                    }
                }
            }

            // Save all reading updates
            if (hasUpdates) {
                tmpReadingsRepository.saveAll(allCustomerReadings);
                tmpReadingsRepository.flush();
            }

            if (!hasUpdates) {
                return new UpdateResponse(false, "No valid updates provided");
            }

            return new UpdateResponse(true, "Meter readings updated successfully. "
                + (readingDateUpdated ? "Reading date updated for all meters." : "")
                + (monTotUpdated ? " Charges updated." : ""));

        } catch (Exception e) {
            e.printStackTrace();
            return new UpdateResponse(false, "Failed to update meter readings: " + e.getMessage());
        }
    }

    /**
     * Update reading date for ALL meters of the customer
     * This ensures reading_date is consistent across all meter types for a bill cycle
     */
    private boolean updateReadingDateForAllMeters(MeterReadingUpdateDTO updateDTO,
                                                List<TmpReadings> allReadings,
                                                String userId,
                                                Date currentTime) {
        boolean hasUpdated = false;
        Date newReadingDate = updateDTO.getReadingDate();

        for (TmpReadings reading : allReadings) {
            // Check if reading date needs to be updated
            if (!reading.getRdngDate().equals(newReadingDate)) {
                // Store old date for logging
                Date oldDate = reading.getRdngDate();
                
                // Update reading date
                reading.setRdngDate(newReadingDate);
                
                // Set edit timestamps
                reading.setEditedDtime(currentTime);
                reading.setEditedUserId(userId);
                
                hasUpdated = true;
                
                System.out.println("Updated reading date for meter type " + reading.getMtrType() + 
                                 " from " + oldDate + " to " + newReadingDate);
            }
        }

        return hasUpdated;
    }

    /**
     * Update specific meter reading details (present reading, units, rate, etc.)
     * This handles individual meter updates while maintaining reading_date consistency
     */
    private boolean updateSpecificMeterReading(MeterReadingUpdateDTO updateDTO,
                                             MeterReadingDetailUpdateDTO meterUpdate,
                                             String userId,
                                             Date currentTime) {
        try {
            // Find existing reading(s) for this meter type
            List<TmpReadings> existingReadings = tmpReadingsRepository
                .findByAccNbrAndAreaCdAndAddedBlcyAndMtrType(
                    updateDTO.getAccountNumber(),
                    updateDTO.getAreaCode(),
                    updateDTO.getBillCycle(),
                    meterUpdate.getMeterType()
                );

            if (existingReadings.isEmpty()) {
                System.err.println("No reading found for meter type: " + meterUpdate.getMeterType());
                return false;
            }

            boolean hasUpdated = false;

            for (TmpReadings reading : existingReadings) {
                boolean meterUpdated = false;

                // Update present reading if provided
                if (meterUpdate.getPresentReading() != null && 
                    (reading.getPrsntRdn() == null || 
                     !reading.getPrsntRdn().equals(meterUpdate.getPresentReading()))) {
                    reading.setPrsntRdn(meterUpdate.getPresentReading());
                    meterUpdated = true;
                }

                // Update units if provided
                if (meterUpdate.getUnits() != null && 
                    (reading.getUnits() == null || 
                     !reading.getUnits().equals(meterUpdate.getUnits()))) {
                    reading.setUnits(meterUpdate.getUnits());
                    meterUpdated = true;
                }

                // Update rate if provided
                if (meterUpdate.getRate() != null && 
                    (reading.getRate() == null || 
                     reading.getRate().compareTo(meterUpdate.getRate()) != 0)) {
                    reading.setRate(meterUpdate.getRate());
                    meterUpdated = true;
                }

                // Update computed charge if provided
                if (meterUpdate.getComputedCharge() != null && 
                    (reading.getComputedChg() == null || 
                     reading.getComputedChg().compareTo(meterUpdate.getComputedCharge()) != 0)) {
                    reading.setComputedChg(meterUpdate.getComputedCharge());
                    meterUpdated = true;
                }

                // If any field was updated, set edit timestamps
                if (meterUpdated) {
                    reading.setEditedDtime(currentTime);
                    reading.setEditedUserId(userId);
                    hasUpdated = true;
                    
                    System.out.println("Updated meter details for type: " + meterUpdate.getMeterType());
                }
            }

            return hasUpdated;

        } catch (Exception e) {
            System.err.println("Error updating meter reading for type " + meterUpdate.getMeterType() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Update tmp_mon_tot table with new charges
     */
    private boolean updateTmpMonTot(TmpMonTot tmpMonTot, MeterReadingUpdateDTO updateDTO, 
                                   String userId, Date currentTime) {
        boolean hasUpdated = false;

        // Update fixed charge if provided
        if (updateDTO.getFixedCharge() != null && 
            (tmpMonTot.getFixedChg() == null || 
             tmpMonTot.getFixedChg().compareTo(updateDTO.getFixedCharge()) != 0)) {
            tmpMonTot.setFixedChg(updateDTO.getFixedCharge());
            hasUpdated = true;
        }

        // Update monthly charge if provided
        if (updateDTO.getMonthlyCharge() != null && 
            (tmpMonTot.getTotCharge() == null || 
             tmpMonTot.getTotCharge().compareTo(updateDTO.getMonthlyCharge()) != 0)) {
            tmpMonTot.setTotCharge(updateDTO.getMonthlyCharge());
            hasUpdated = true;
        }

        // Update VAT amount if provided
        if (updateDTO.getVatAmount() != null && 
            (tmpMonTot.getTotGst() == null || 
             tmpMonTot.getTotGst().compareTo(updateDTO.getVatAmount()) != 0)) {
            tmpMonTot.setTotGst(updateDTO.getVatAmount());
            hasUpdated = true;
        }

        // Update total amount if provided
        if (updateDTO.getTotalAmount() != null && 
            (tmpMonTot.getTotAmt() == null || 
             tmpMonTot.getTotAmt().compareTo(updateDTO.getTotalAmount()) != 0)) {
            tmpMonTot.setTotAmt(updateDTO.getTotalAmount());
            hasUpdated = true;
        }

        // If any field was updated, set edit timestamps
        if (hasUpdated) {
            tmpMonTot.setEditedDtime(currentTime);
            tmpMonTot.setEditedUserId(userId);
        }

        return hasUpdated;
    }

    /**
     * Validate the update request
     */
    private void validateUpdateRequest(MeterReadingUpdateDTO updateDTO) {
        if (updateDTO.getAccountNumber() == null || updateDTO.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (updateDTO.getAreaCode() == null || updateDTO.getAreaCode().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (updateDTO.getBillCycle() == null || updateDTO.getBillCycle().trim().isEmpty()) {
            throw new RuntimeException("Bill cycle is required");
        }
        if (updateDTO.getSessionId() == null || updateDTO.getSessionId().trim().isEmpty()) {
            throw new RuntimeException("Session ID is required");
        }
        if (updateDTO.getUserId() == null || updateDTO.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        // Reading date is now optional since user might want to update only charges
        // But if provided, it will update for all meters
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

    /**
     * Response DTO for update operations
     */
    @Data
    public static class UpdateResponse {
        private Boolean success;
        private String message;
        private String timestamp;

        // Constructor with boolean and String parameters
        public UpdateResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
            this.timestamp = new Date().toString();
        }

        // Default constructor
        public UpdateResponse() {
            this.timestamp = new Date().toString();
        }
    }
}