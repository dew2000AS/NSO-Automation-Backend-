package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpReadingsId;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TmpReadingsService {

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    // Get all readings
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getAllReadings() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findAll();
            return readings.stream()
                    .map(this::convertToDTO)
                                        .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all readings: " + e.getMessage(), e);
        }
    }

    // Get readings by account number
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbr(accNbr);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings for account: " + accNbr + " - " + e.getMessage(), e);
        }
    }

    // Get readings by account number and date
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAccNbrAndDate(String accNbr, Date rdngDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAccNbrAndRdngDate(accNbr, rdngDate);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings: " + e.getMessage(), e);
        }
    }

    // Get readings by area code
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByAreaCd(String areaCd) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByAreaCd(areaCd);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by area code: " + e.getMessage(), e);
        }
    }

    // Get readings by meter number
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByMtrNbr(String mtrNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByMtrNbr(mtrNbr);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by meter number: " + e.getMessage(), e);
        }
    }

    // Get readings by date range
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsByDateRange(Date startDate, Date endDate) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findByDateRange(startDate, endDate);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings by date range: " + e.getMessage(), e);
        }
    }

    // Get latest readings for account
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getLatestReadingsByAccNbr(String accNbr) {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findLatestReadingsByAccNbr(accNbr);
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest readings: " + e.getMessage(), e);
        }
    }

    // Get specific reading
    @Transactional(readOnly = true)
    public Optional<TmpReadingsDTO> getSpecificReading(String accNbr, String areaCd, String addedBlcy, 
                                                      Integer mtrSeq, String mtrType, Date rdngDate) {
        try {
            Optional<TmpReadings> reading = tmpReadingsRepository.findSpecificReading(
                accNbr, areaCd, addedBlcy, mtrSeq, mtrType, rdngDate);
            return reading.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve specific reading: " + e.getMessage(), e);
        }
    }

    // Create new reading
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

    // Update existing reading
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

    // Get readings with errors
    @Transactional(readOnly = true)
    public List<TmpReadingsDTO> getReadingsWithErrors() {
        try {
            List<TmpReadings> readings = tmpReadingsRepository.findReadingsWithErrors();
            return readings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve readings with errors: " + e.getMessage(), e);
        }
    }

    // Get distinct account numbers
    @Transactional(readOnly = true)
    public List<String> getDistinctAccNbrs() {
        try {
            return tmpReadingsRepository.findDistinctAccNbrs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct account numbers: " + e.getMessage(), e);
        }
    }

    // Get distinct meter types
    @Transactional(readOnly = true)
    public List<String> getDistinctMtrTypes() {
        try {
            return tmpReadingsRepository.findDistinctMtrTypes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct meter types: " + e.getMessage(), e);
        }
    }

    // Helper method to validate reading
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

    // Helper method to update reading fields
    private void updateReadingFields(TmpReadings reading, TmpReadingsDTO dto) {
        if (dto.getInstId() != null) reading.setInstId(dto.getInstId());
        if (dto.getPrvDate() != null) reading.setPrvDate(dto.getPrvDate());
        if (dto.getPrsntRdn() != null) reading.setPrsntRdn(dto.getPrsntRdn());
        if (dto.getPrvRdn() != null) reading.setPrvRdn(dto.getPrvRdn());
        if (dto.getMtrNbr() != null) reading.setMtrNbr(dto.getMtrNbr());
        if (dto.getUnits() != null) reading.setUnits(dto.getUnits());
        if (dto.getRate() != null) reading.setRate(dto.getRate());
        if (dto.getComputedChg() != null) reading.setComputedChg(dto.getComputedChg());
        if (dto.getMntChg() != null) reading.setMntChg(dto.getMntChg());
        if (dto.getAcode() != null) reading.setAcode(dto.getAcode());
        if (dto.getMFactor() != null) reading.setMFactor(dto.getMFactor());
        if (dto.getBillStat() != null) reading.setBillStat(dto.getBillStat());
        if (dto.getErrStat() != null) reading.setErrStat(dto.getErrStat());
        if (dto.getMtrStat() != null) reading.setMtrStat(dto.getMtrStat());
        if (dto.getRdnStat() != null) reading.setRdnStat(dto.getRdnStat());
        if (dto.getUserId() != null) reading.setUserId(dto.getUserId());
    }

    // Convert Entity to DTO
    private TmpReadingsDTO convertToDTO(TmpReadings reading) {
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

    // Convert DTO to Entity
    private TmpReadings convertToEntity(TmpReadingsDTO dto) {
        TmpReadings reading = new TmpReadings();
        reading.setAccNbr(dto.getAccNbr());
        reading.setInstId(dto.getInstId());
        reading.setAreaCd(dto.getAreaCd());
        reading.setAddedBlcy(dto.getAddedBlcy());
        reading.setMtrSeq(dto.getMtrSeq());
        reading.setMtrType(dto.getMtrType());
        reading.setPrvDate(dto.getPrvDate());
        reading.setRdngDate(dto.getRdngDate());
        reading.setPrsntRdn(dto.getPrsntRdn());
        reading.setPrvRdn(dto.getPrvRdn());
        reading.setMtrNbr(dto.getMtrNbr());
        reading.setUnits(dto.getUnits());
        reading.setRate(dto.getRate());
        reading.setComputedChg(dto.getComputedChg());
        reading.setMntChg(dto.getMntChg());
        reading.setAcode(dto.getAcode());
        reading.setMFactor(dto.getMFactor());
        reading.setBillStat(dto.getBillStat());
        reading.setErrStat(dto.getErrStat());
        reading.setMtrStat(dto.getMtrStat());
        reading.setRdnStat(dto.getRdnStat());
        reading.setUserId(dto.getUserId());
        reading.setEnteredDtime(dto.getEnteredDtime());
        reading.setEditedUserId(dto.getEditedUserId());
        reading.setEditedDtime(dto.getEditedDtime());
        return reading;
    }
}