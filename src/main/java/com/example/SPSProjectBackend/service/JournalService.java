package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.JournalDetailDTO;
import com.example.SPSProjectBackend.dto.JournalSummaryDTO;
import com.example.SPSProjectBackend.dto.JournalTypeDTO;
import com.example.SPSProjectBackend.dto.JournalUpdateDTO;
import com.example.SPSProjectBackend.dto.JournalCreateDTO;
import com.example.SPSProjectBackend.dto.JurnlAuthDTO;
import com.example.SPSProjectBackend.model.Journal;
import com.example.SPSProjectBackend.model.JournalId;
import com.example.SPSProjectBackend.model.JournalType;
import com.example.SPSProjectBackend.model.JurnlAuth;
import com.example.SPSProjectBackend.repository.JournalRepository;
import com.example.SPSProjectBackend.repository.JournalTypeRepository;
import com.example.SPSProjectBackend.repository.JurnlAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private JournalTypeRepository journalTypeRepository;

    @Autowired
    private JurnlAuthRepository jurnlAuthRepository;

    // ===================== GET ALL JOURNALS WITH SELECTED AREA AND BILL CYCLE =====================
    @Transactional(readOnly = true)
    public List<JournalSummaryDTO> getAllJournalsSummary(String areaCode, Integer billCycle) {
        try {
            System.out.println("Fetching journals for area: " + areaCode + ", bill cycle: " + billCycle);
            
            // Fetch journals filtered by selected area and bill cycle
            List<JournalSummaryDTO> journals = journalRepository.findFilteredJournalsByAreaAndBillCycle(areaCode, billCycle);
            
            System.out.println("Found " + journals.size() + " journals matching criteria");
            
            if (!journals.isEmpty()) {
                System.out.println("Sample of first 5 journals:");
                for (int i = 0; i < Math.min(5, journals.size()); i++) {
                    JournalSummaryDTO j = journals.get(i);
                    System.out.println("  " + (i+1) + ". jnl_no=" + j.getJnlNo() + 
                                     ", acc_nbr='" + j.getAccNbr() + "'" +
                                     ", adjust_amt=" + j.getAdjustAmt());
                }
            }
            
            return journals;
        } catch (Exception e) {
            System.err.println("Error in getAllJournalsSummary: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journals: " + e.getMessage(), e);
        }
    }

    // ===================== GET JOURNAL DETAILS WITH SELECTED AREA AND BILL CYCLE =====================
    @Transactional(readOnly = true)
    public List<JournalDetailDTO> getJournalDetail(String accNbr, String jnlType, Integer jnlNo, BigDecimal adjustAmt, 
                                                    String areaCode, Integer billCycle) {
        try {
            if (accNbr == null || jnlType == null || jnlNo == null || adjustAmt == null) {
                System.out.println("Required parameters are null, returning empty list");
                return Collections.emptyList();
            }
            
            System.out.println("Fetching journal details for area: " + areaCode + ", bill cycle: " + billCycle);
            System.out.println("Composite key: accNbr=" + accNbr + ", jnlType=" + jnlType + 
                             ", jnlNo=" + jnlNo + ", adjustAmt=" + adjustAmt);
            
            // Fetch journal details with area and bill cycle filtering
            List<Journal> journals = journalRepository.findJournalDetailsByCompositeKeyAndArea(
                accNbr, jnlType, jnlNo, adjustAmt, areaCode, billCycle);
            
            System.out.println("Found " + journals.size() + " matching journals");
            
            if (!journals.isEmpty()) {
                Journal j = journals.get(0);
                System.out.println("Journal details found:");
                System.out.println("  jnl_no=" + j.getId().getJnlNo());
                System.out.println("  acc_nbr=" + j.getId().getAccNbr());
                System.out.println("  area_cd=" + j.getId().getAreaCd());
                System.out.println("  added_blcy=" + j.getId().getAddedBlcy());
                System.out.println("  jnl_type=" + j.getId().getJnlType());
                System.out.println("  adjust_amt=" + j.getAdjustAmt());
                System.out.println("  adjust_stat=" + j.getAdjustStat());
                System.out.println("  auth_code=" + j.getAuthCode());
                System.out.println("  doc_attch=" + j.getDocAttch());
                System.out.println("  confirmed=" + j.getConfirmed());
            }
            
            return journals.stream()
                    .map(this::convertToDetailDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getJournalDetail: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journal details: " + e.getMessage(), e);
        }
    }

    // ===================== UPDATE JOURNAL =====================
    @Transactional
    public JournalDetailDTO updateJournal(JournalUpdateDTO updateDTO) {
        try {
            System.out.println("========== JOURNAL SERVICE UPDATE ==========");
            System.out.println("Updating journal with composite key:");
            System.out.println("  accNbr: " + updateDTO.getAccNbr());
            System.out.println("  areaCd: " + updateDTO.getAreaCd());
            System.out.println("  addedBlcy: " + updateDTO.getAddedBlcy());
            System.out.println("  jnlType: " + updateDTO.getJnlType());
            System.out.println("  jnlNo: " + updateDTO.getJnlNo());
            System.out.println("  New adjustAmt: " + updateDTO.getAdjustAmt());
            
            // Create the composite key
            JournalId journalId = new JournalId(
                updateDTO.getAccNbr(),
                updateDTO.getAreaCd(),
                updateDTO.getAddedBlcy(),
                updateDTO.getJnlType(),
                updateDTO.getJnlNo()
            );
            
            // Find the journal by composite key
            System.out.println("Fetching journal by composite key...");
            Optional<Journal> journalOpt = journalRepository.findById(journalId);
            
            if (!journalOpt.isPresent()) {
                System.err.println("Journal not found with composite key");
                throw new RuntimeException("Journal not found with the specified composite key");
            }
            
            Journal journal = journalOpt.get();
            System.out.println("Journal found. Current adjust_amt: " + journal.getAdjustAmt());
            
            // Update the adjustment amount
            BigDecimal oldAmount = journal.getAdjustAmt();
            journal.setAdjustAmt(updateDTO.getAdjustAmt());
            
            // Update edited user ID and timestamp
            journal.setEditedUserId(updateDTO.getEditedUserId() != null ? updateDTO.getEditedUserId() : "SYSTEM");
            journal.setEditedDtime(LocalDateTime.now());
            
            System.out.println("Adjustment amount changed from " + oldAmount + " to " + updateDTO.getAdjustAmt());
            System.out.println("Saving journal using JPA...");
            
            // Save using JPA - no native query needed now
            Journal savedJournal = journalRepository.save(journal);
            
            System.out.println("Journal updated successfully. New adjust_amt: " + savedJournal.getAdjustAmt());
            System.out.println("==============================================");
            
            return convertToDetailDTO(savedJournal);
            
        } catch (Exception e) {
            System.err.println("========== ERROR IN JOURNAL SERVICE UPDATE ==========");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("====================================================");
            throw new RuntimeException("Error updating journal: " + e.getMessage(), e);
        }
    }

    // ===================== CREATE JOURNAL =====================
    @Transactional
    public JournalDetailDTO createJournal(JournalCreateDTO createDTO) {
        try {
            System.out.println("Creating new journal with data: " + createDTO);
            
            // ====== CREATE FIRST JOURNAL ENTRY (Field 1) ======
            JournalId journalId1 = new JournalId();
            journalId1.setAccNbr(createDTO.getAccNbr());
            journalId1.setAreaCd(createDTO.getAreaCd());
            journalId1.setAddedBlcy(createDTO.getCurrentBillCycle());
            journalId1.setJnlType(createDTO.getJnlType());
            journalId1.setJnlNo(createDTO.getJnlNo());
            
            // Check if journal already exists
            if (journalRepository.existsById(journalId1)) {
                throw new RuntimeException("Journal already exists with jnl_no: " + createDTO.getJnlNo());
            }
            
            // Create first journal entity (for Field 1)
            Journal journal1 = new Journal();
            journal1.setId(journalId1);
            
            // Calculate adjust_amt from field1
            BigDecimal field1Amount = createDTO.getField1() != null ? createDTO.getField1() : BigDecimal.ZERO;
            int field1Sign = "CREDIT".equalsIgnoreCase(createDTO.getField1Type()) ? -1 : 1;
            BigDecimal adjustAmt1 = field1Amount.multiply(new BigDecimal(field1Sign));
            
            journal1.setAdjustAmt(adjustAmt1);
            
            // adjust_stat: "1" for DEBIT, "-1" for CREDIT
            journal1.setAdjustStat("CREDIT".equalsIgnoreCase(createDTO.getField1Type()) ? "-1" : "1");
            
            journal1.setAuthCode(createDTO.getApprovedBy());
            
            // Document Attached: "0" for No, actual value for Yes
            journal1.setDocAttch("N".equalsIgnoreCase(createDTO.getDocAttch()) ? "0" : createDTO.getDocAttch());
            
            // Parse and set journal date
            if (createDTO.getJnlDate() != null && !createDTO.getJnlDate().isEmpty()) {
                try {
                    journal1.setJnlDate(LocalDateTime.parse(createDTO.getJnlDate() + "T00:00:00"));
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                    journal1.setJnlDate(LocalDateTime.now());
                }
            } else {
                journal1.setJnlDate(LocalDateTime.now());
            }
            
            // Confirmed: null if not checked, "Y" if checked
            journal1.setConfirmed(createDTO.getIndividuallyConfirmed() != null && createDTO.getIndividuallyConfirmed() ? "Y" : null);
            
            journal1.setUserId("ADMIN"); // Set from current user session
            journal1.setEnteredDtime(LocalDateTime.now());
            
            // Save first journal to database
            Journal savedJournal1 = journalRepository.save(journal1);
            System.out.println("First journal entry created: " + savedJournal1.getId());
            
            // ====== CREATE SECOND JOURNAL ENTRY (Field 2 - if paired type exists) ======
            if (createDTO.getPairedJnlType() != null && !createDTO.getPairedJnlType().isEmpty() 
                && createDTO.getField2() != null && createDTO.getField2().compareTo(BigDecimal.ZERO) > 0) {
                
                System.out.println("Creating paired journal entry with type: " + createDTO.getPairedJnlType());
                
                // Use same jnl_no for second entry (only jnl_type differs)
                JournalId journalId2 = new JournalId();
                journalId2.setAccNbr(createDTO.getAccNbr());
                journalId2.setAreaCd(createDTO.getAreaCd());
                journalId2.setAddedBlcy(createDTO.getCurrentBillCycle());
                journalId2.setJnlType(createDTO.getPairedJnlType()); // Use paired journal type
                journalId2.setJnlNo(createDTO.getJnlNo()); // Same journal number
                
                // Check if second journal already exists
                if (journalRepository.existsById(journalId2)) {
                    throw new RuntimeException("Paired journal already exists with jnl_no: " + createDTO.getJnlNo());
                }
                
                // Create second journal entity (for Field 2)
                Journal journal2 = new Journal();
                journal2.setId(journalId2);
                
                // Calculate adjust_amt from field2
                BigDecimal field2Amount = createDTO.getField2();
                int field2Sign = "CREDIT".equalsIgnoreCase(createDTO.getField2Type()) ? -1 : 1;
                BigDecimal adjustAmt2 = field2Amount.multiply(new BigDecimal(field2Sign));
                
                journal2.setAdjustAmt(adjustAmt2);
                
                // adjust_stat: "1" for DEBIT, "-1" for CREDIT
                journal2.setAdjustStat("CREDIT".equalsIgnoreCase(createDTO.getField2Type()) ? "-1" : "1");
                
                journal2.setAuthCode(createDTO.getApprovedBy());
                journal2.setDocAttch("N".equalsIgnoreCase(createDTO.getDocAttch()) ? "0" : createDTO.getDocAttch());
                journal2.setJnlDate(journal1.getJnlDate()); // Same date as first entry
                journal2.setConfirmed(createDTO.getIndividuallyConfirmed() != null && createDTO.getIndividuallyConfirmed() ? "Y" : null);
                journal2.setUserId("ADMIN");
                journal2.setEnteredDtime(LocalDateTime.now());
                
                // Save second journal to database
                Journal savedJournal2 = journalRepository.save(journal2);
                System.out.println("Second journal entry created: " + savedJournal2.getId());
            }
            
            System.out.println("Journal creation completed successfully");
            
            return convertToDetailDTO(savedJournal1);
            
        } catch (Exception e) {
            System.err.println("Error creating journal: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating journal: " + e.getMessage(), e);
        }
    }

    // ===================== DTO MAPPER =====================
    private JournalDetailDTO convertToDetailDTO(Journal journal) {
        if (journal == null) return null;

        JournalDetailDTO dto = new JournalDetailDTO(
                journal.getId().getJnlNo(),
                trimString(journal.getId().getAccNbr()),
                trimString(journal.getId().getAreaCd()),
                journal.getId().getAddedBlcy(),
                trimString(journal.getId().getJnlType()),
                journal.getAdjustAmt() != null ? journal.getAdjustAmt() : BigDecimal.ZERO,
                trimString(journal.getAdjustStat()),
                trimString(journal.getAuthCode()),
                trimString(journal.getDocAttch()),
                journal.getJnlDate(),
                trimString(journal.getConfirmed()),
                trimString(journal.getUserId()),
                journal.getEnteredDtime(),
                trimString(journal.getEditedUserId()),
                journal.getEditedDtime()
        );
        
        return dto;
    }

    // ===================== GET ALL JOURNAL TYPES =====================
    @Transactional(readOnly = true)
    public List<JournalTypeDTO> getAllJournalTypes() {
        try {
            System.out.println("Fetching all journal types from jnal_typs table where jnlt_status='A'");
            
            List<JournalType> journalTypes = journalTypeRepository.findAllOrderedByType();
            
            System.out.println("Found " + journalTypes.size() + " active journal types");
            
            return journalTypes.stream()
                    .map(jt -> new JournalTypeDTO(jt.getJnlType(), jt.getJnlDesc()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllJournalTypes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journal types: " + e.getMessage(), e);
        }
    }

    // ===================== GET ALL JURNL AUTH =====================
    public List<JurnlAuthDTO> getAllJurnlAuth() {
        try {
            System.out.println("Fetching all jurnl_auth records where ac_status='A'");
            
            List<JurnlAuth> jurnlAuths = jurnlAuthRepository.findAllActive();
            
            System.out.println("Found " + jurnlAuths.size() + " active jurnl_auth records");
            
            return jurnlAuths.stream()
                    .map(ja -> new JurnlAuthDTO(ja.getAuthCode(), ja.getDesig(), ja.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllJurnlAuth: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching jurnl_auth: " + e.getMessage(), e);
        }
    }

    // ===================== UTIL =====================
    private String trimString(String value) {
        return value != null ? value.trim() : null;
    }
    
    private String safeString(String value) {
        return value != null ? value.trim() : "N/A";
    }
}
