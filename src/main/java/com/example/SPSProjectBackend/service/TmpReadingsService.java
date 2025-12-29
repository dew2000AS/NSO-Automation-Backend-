package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpReadingsId;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Transactional
public class TmpReadingsService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;
    
    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    // Get all readings with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getAllReadings() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findAllWithActiveBillCycle();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all readings: " + e.getMessage(), e);
        }
    }
    
    // Get all readings without bill cycle filter (for historical access) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getAllReadingsWithoutFilter() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findAll();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all readings without filter: " + e.getMessage(), e);
        }
    }

    // Get readings by account number with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrWithActiveBillCycle(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings for account: " + accNbr + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by account number without bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrWithoutFilter(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbr(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings for account without filter: " + accNbr + " - " + e.getMessage(), e);
        }
    }

    // Get readings by account number and date with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrAndDate(String accNbr, Date rdngDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrAndRdngDateWithActiveBillCycle(accNbr, rdngDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings: " + e.getMessage(), e);
        }
    }
    
    // Get readings by account number and date without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrAndDateWithoutFilter(String accNbr, Date rdngDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrAndRdngDate(accNbr, rdngDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings without filter: " + e.getMessage(), e);
        }
    }

    // Get readings by area code with active bill cycle filter (default behavior) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCd(String areaCd) {
        try {
            // Clean area code input
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            
            // Get active bill cycle for the area
            Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCodeTrimmed(cleanAreaCd);
            
            if (!activeBillCycleOpt.isPresent()) {
                System.out.println("DEBUG: No active bill cycle found for area " + cleanAreaCd);
                return new ArrayList<>();
            }
            
            // Convert integer bill cycle to string for comparison with added_blcy
            Integer activeBillCycle = activeBillCycleOpt.get();
            String activeBillCycleStr = String.valueOf(activeBillCycle);
            
            System.out.println("DEBUG: Area=" + cleanAreaCd + ", Active Bill Cycle=" + activeBillCycleStr);
            
            // Use native query with proper string comparison
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCdAndBillCycle(cleanAreaCd, activeBillCycleStr);
            
            // Debug logging
            System.out.println("DEBUG: Found " + readings.size() + " readings for area " + cleanAreaCd + " and bill cycle " + activeBillCycleStr);
            
            if (readings.isEmpty()) {
                // Try alternative query using the active bill cycle filter
                System.out.println("DEBUG: Trying alternative query for area " + cleanAreaCd);
                readings = tmpReadingsRepository.findByAreaCdWithActiveBillCycle(cleanAreaCd);
                System.out.println("DEBUG: Alternative query found " + readings.size() + " readings");
            }
            
            return readings.stream()
                    .filter(reading -> reading != null)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("ERROR in getReadingsByAreaCd: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve readings by area code: " + areaCd + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by area code without bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCdWithoutFilter(String areaCd) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCd(cleanAreaCd);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by area code without filter: " + areaCd + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by area code and specific bill cycle (for historical data) - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCdAndBillCycle(String areaCd, String billCycle) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            String cleanBillCycle = billCycle != null ? billCycle.trim() : "";
            
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCdAndBillCycle(cleanAreaCd, cleanBillCycle);
            
            System.out.println("DEBUG: Area=" + cleanAreaCd + ", BillCycle=" + cleanBillCycle + ", Found=" + readings.size() + " readings");
            
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by area code and bill cycle: " + areaCd + ", " + billCycle + " - " + e.getMessage(), e);
        }
    }

    // Get readings by meter number with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByMtrNbr(String mtrNbr) {
        try {
            String cleanMtrNbr = mtrNbr != null ? mtrNbr.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByMtrNbrWithActiveBillCycle(cleanMtrNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by meter number: " + mtrNbr + " - " + e.getMessage(), e);
        }
    }
    
    // Get readings by meter number without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByMtrNbrWithoutFilter(String mtrNbr) {
        try {
            String cleanMtrNbr = mtrNbr != null ? mtrNbr.trim() : "";
            List<TmpReadings> readings = tmpReadingsRepository.findByMtrNbr(cleanMtrNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by meter number without filter: " + mtrNbr + " - " + e.getMessage(), e);
        }
    }

    // Get readings by date range with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByDateRange(Date startDate, Date endDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByDateRangeWithActiveBillCycle(startDate, endDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by date range: " + e.getMessage(), e);
        }
    }
    
    // Get readings by date range without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByDateRangeWithoutFilter(Date startDate, Date endDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByDateRange(startDate, endDate);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by date range without filter: " + e.getMessage(), e);
        }
    }

    // Get latest readings for account with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getLatestReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findLatestReadingsByAccNbrWithActiveBillCycle(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest readings: " + e.getMessage(), e);
        }
    }
    
    // Get latest readings for account without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getLatestReadingsByAccNbrWithoutFilter(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findLatestReadingsByAccNbr(accNbr);
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest readings without filter: " + e.getMessage(), e);
        }
    }
    
    // Get active bill cycle for area - FIXED
    @Transactional(readOnly = true)
    public Optional<Integer> getActiveBillCycleForArea(String areaCd) {
        try {
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            return billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCodeTrimmed(cleanAreaCd);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Get specific reading - FIXED
    @Transactional(readOnly = true)
    public Optional<TmpReadingsDTO> getSpecificReading(String accNbr, String areaCd, String addedBlcy, 
                                                      Integer mtrSeq, String mtrType, Date rdngDate) {
        try {
            String cleanAccNbr = accNbr != null ? accNbr.trim() : "";
            String cleanAreaCd = areaCd != null ? areaCd.trim() : "";
            String cleanAddedBlcy = addedBlcy != null ? addedBlcy.trim() : "";
            String cleanMtrType = mtrType != null ? mtrType.trim() : "";
            
            Optional<TmpReadings> reading = tmpReadingsRepository.findSpecificReading(
                cleanAccNbr, cleanAreaCd, cleanAddedBlcy, mtrSeq, cleanMtrType, rdngDate);
            return reading.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve specific reading: " + e.getMessage(), e);
        }
    }

    // Create new reading - FIXED
    @Transactional
    public TmpReadingsDTO createReading(TmpReadingsDTO readingDTO) {
        try {
            // Validate required fields
            validateReading(readingDTO);

            // Check if reading already exists
            if (tmpReadingsRepository.existsReading(
                    readingDTO.getAccNbr(), readingDTO.getAreaCd(), readingDTO.getAddedBlcy(),
                    readingDTO.getMtrSeq(), readingDTO.getMtrType(), readingDTO.getRdngDate())) {
                throw new RuntimeException("Reading already exists for this combination");
            }

            TmpReadings reading = convertToEntity(readingDTO);
            
            // Set timestamps
            reading.setEnteredDtime(new Date());
            
            TmpReadings savedReading = tmpReadingsRepository.save(reading);
            tmpReadingsRepository.flush();
            
            return convertToDTO(savedReading);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create reading: " + e.getMessage(), e);
        }
    }

    // Update existing reading - FIXED
    @Transactional
    public TmpReadingsDTO updateReading(TmpReadingsDTO readingDTO) {
        try {
            // Validate required fields
            validateReading(readingDTO);

            Optional<TmpReadings> existingReading = tmpReadingsRepository.findSpecificReading(
                readingDTO.getAccNbr(), readingDTO.getAreaCd(), readingDTO.getAddedBlcy(),
                readingDTO.getMtrSeq(), readingDTO.getMtrType(), readingDTO.getRdngDate());

            if (!existingReading.isPresent()) {
                throw new RuntimeException("Reading not found");
            }

            TmpReadings reading = existingReading.get();
            
            // Update fields
            updateReadingFields(reading, readingDTO);
            
            // Set edit timestamp
            reading.setEditedDtime(new Date());
            if (readingDTO.getEditedUserId() != null) {
                reading.setEditedUserId(readingDTO.getEditedUserId());
            }

            TmpReadings updatedReading = tmpReadingsRepository.save(reading);
            tmpReadingsRepository.flush();
            
            return convertToDTO(updatedReading);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update reading: " + e.getMessage(), e);
        }
    }

    // Get readings with errors with active bill cycle filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsWithErrors() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findReadingsWithErrorsWithActiveBillCycle();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings with errors: " + e.getMessage(), e);
        }
    }
    
    // Get readings with errors without filter - FIXED
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsWithErrorsWithoutFilter() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findReadingsWithErrors();
            return readings.stream()
                    .filter(reading -> reading != null) // Add null filter
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings with errors without filter: " + e.getMessage(), e);
        }
    }

    // Get distinct account numbers - FIXED
    @Transactional(readOnly = true)
    public List<String> getDistinctAccNbrs() {
        try {
            return tmpReadingsRepository.findDistinctAccNbrs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct account numbers: " + e.getMessage(), e);
        }
    }

    // Get distinct meter types - FIXED
    @Transactional(readOnly = true)
    public List<String> getDistinctMtrTypes() {
        try {
            return tmpReadingsRepository.findDistinctMtrTypes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct meter types: " + e.getMessage(), e);
        }
    }

    // Helper method to validate reading - FIXED
    private void validateReading(TmpReadingsDTO readingDTO) {
        if (readingDTO.getAccNbr() == null || readingDTO.getAccNbr().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (readingDTO.getAreaCd() == null || readingDTO.getAreaCd().trim().isEmpty()) {
            throw new RuntimeException("Area code is required");
        }
        if (readingDTO.getAddedBlcy() == null || readingDTO.getAddedBlcy().trim().isEmpty()) {
            throw new RuntimeException("Added block is required");
        }
        if (readingDTO.getMtrSeq() == null) {
            throw new RuntimeException("Meter sequence is required");
        }
        if (readingDTO.getMtrType() == null || readingDTO.getMtrType().trim().isEmpty()) {
            throw new RuntimeException("Meter type is required");
        }
        if (readingDTO.getRdngDate() == null) {
            throw new RuntimeException("Reading date is required");
        }
    }

    // Helper method to update reading fields - FIXED
    private void updateReadingFields(TmpReadings reading, TmpReadingsDTO dto) {
        if (dto.getInstId() != null) reading.setInstId(dto.getInstId().trim());
        if (dto.getPrvDate() != null) reading.setPrvDate(dto.getPrvDate());
        if (dto.getPrsntRdn() != null) reading.setPrsntRdn(dto.getPrsntRdn());
        if (dto.getPrvRdn() != null) reading.setPrvRdn(dto.getPrvRdn());
        if (dto.getMtrNbr() != null) reading.setMtrNbr(dto.getMtrNbr().trim());
        if (dto.getUnits() != null) reading.setUnits(dto.getUnits());
        if (dto.getRate() != null) reading.setRate(dto.getRate());
        if (dto.getComputedChg() != null) reading.setComputedChg(dto.getComputedChg());
        if (dto.getMntChg() != null) reading.setMntChg(dto.getMntChg());
        if (dto.getAcode() != null) reading.setAcode(dto.getAcode().trim());
        if (dto.getMFactor() != null) reading.setMFactor(dto.getMFactor());
        if (dto.getBillStat() != null) reading.setBillStat(dto.getBillStat().trim());
        if (dto.getErrStat() != null) reading.setErrStat(dto.getErrStat());
        if (dto.getMtrStat() != null) reading.setMtrStat(dto.getMtrStat().trim());
        if (dto.getRdnStat() != null) reading.setRdnStat(dto.getRdnStat().trim());
        if (dto.getUserId() != null) reading.setUserId(dto.getUserId().trim());
        if (dto.getEditedUserId() != null) reading.setEditedUserId(dto.getEditedUserId().trim());
    }

    // Convert Entity to DTO - FIXED
    private TmpReadingsDTO convertToDTO(TmpReadings reading) {
        if (reading == null) {
            return null;
        }
        
        TmpReadingsDTO dto = new TmpReadingsDTO();
        dto.setAccNbr(reading.getAccNbr());
        dto.setInstId(reading.getInstId());
        dto.setAreaCd(reading.getAreaCd());
        dto.setAddedBlcy(reading.getAddedBlcy());
        dto.setMtrSeq(reading.getMtrSeq());
        dto.setMtrType(reading.getMtrType());
        dto.setPrvDate(reading.getPrvDate());
        dto.setRdngDate(reading.getRdngDate());
        dto.setPrsntRdn(reading.getPrsntRdn());
        dto.setPrvRdn(reading.getPrvRdn());
        dto.setMtrNbr(reading.getMtrNbr());
        dto.setUnits(reading.getUnits());
        dto.setRate(reading.getRate());
        dto.setComputedChg(reading.getComputedChg());
        dto.setMntChg(reading.getMntChg());
        dto.setAcode(reading.getAcode());
        dto.setMFactor(reading.getMFactor());
        dto.setBillStat(reading.getBillStat());
        dto.setErrStat(reading.getErrStat());
        dto.setMtrStat(reading.getMtrStat());
        dto.setRdnStat(reading.getRdnStat());
        dto.setUserId(reading.getUserId());
        dto.setEnteredDtime(reading.getEnteredDtime());
        dto.setEditedUserId(reading.getEditedUserId());
        dto.setEditedDtime(reading.getEditedDtime());
        return dto;
    }

    // Convert DTO to Entity - FIXED
    private TmpReadings convertToEntity(TmpReadingsDTO dto) {
        TmpReadings reading = new TmpReadings();
        reading.setAccNbr(dto.getAccNbr() != null ? dto.getAccNbr().trim() : null);
        reading.setInstId(dto.getInstId() != null ? dto.getInstId().trim() : null);
        reading.setAreaCd(dto.getAreaCd() != null ? dto.getAreaCd().trim() : null);
        reading.setAddedBlcy(dto.getAddedBlcy() != null ? dto.getAddedBlcy().trim() : null);
        reading.setMtrSeq(dto.getMtrSeq());
        reading.setMtrType(dto.getMtrType() != null ? dto.getMtrType().trim() : null);
        reading.setPrvDate(dto.getPrvDate());
        reading.setRdngDate(dto.getRdngDate());
        reading.setPrsntRdn(dto.getPrsntRdn());
        reading.setPrvRdn(dto.getPrvRdn());
        reading.setMtrNbr(dto.getMtrNbr() != null ? dto.getMtrNbr().trim() : null);
        reading.setUnits(dto.getUnits());
        reading.setRate(dto.getRate());
        reading.setComputedChg(dto.getComputedChg());
        reading.setMntChg(dto.getMntChg());
        reading.setAcode(dto.getAcode() != null ? dto.getAcode().trim() : null);
        reading.setMFactor(dto.getMFactor());
        reading.setBillStat(dto.getBillStat() != null ? dto.getBillStat().trim() : null);
        reading.setErrStat(dto.getErrStat());
        reading.setMtrStat(dto.getMtrStat() != null ? dto.getMtrStat().trim() : null);
        reading.setRdnStat(dto.getRdnStat() != null ? dto.getRdnStat().trim() : null);
        reading.setUserId(dto.getUserId() != null ? dto.getUserId().trim() : null);
        reading.setEnteredDtime(dto.getEnteredDtime());
        reading.setEditedUserId(dto.getEditedUserId() != null ? dto.getEditedUserId().trim() : null);
        reading.setEditedDtime(dto.getEditedDtime());
        return reading;
    }
}