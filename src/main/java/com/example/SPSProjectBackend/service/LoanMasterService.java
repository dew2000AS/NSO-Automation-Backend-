package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.LoanMasterDTO;
import com.example.SPSProjectBackend.model.LoanMaster;
import com.example.SPSProjectBackend.repository.LoanMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanMasterService {

    @Autowired
    private LoanMasterRepository repository;

    public List<LoanMasterDTO> getAllLoans() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<LoanMasterDTO> getLoanByAccNbr(String accNbr) {
        return repository.findById(accNbr).map(this::convertToDTO);
    }

    private LoanMasterDTO convertToDTO(LoanMaster entity) {
        LoanMasterDTO dto = new LoanMasterDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setLoanType(entity.getLoanType());
        dto.setLoanAmt(entity.getLoanAmt());
        dto.setNoMonths(entity.getNoMonths());
        dto.setStBillCycle(entity.getStBillCycle());
        dto.setEndBillCycle(entity.getEndBillCycle());
        dto.setIntRate(entity.getIntRate());
        dto.setActiveSt(entity.getActiveSt());
        dto.setMonPmnt(entity.getMonPmnt());
        dto.setEnteredUser(entity.getEnteredUser());
        dto.setEditedUser(entity.getEditedUser());
        return dto;
    }
}
