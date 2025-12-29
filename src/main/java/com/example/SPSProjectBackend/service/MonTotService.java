package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MonTotDTO;
import com.example.SPSProjectBackend.model.MonTot;
import com.example.SPSProjectBackend.model.MonTotId;
import com.example.SPSProjectBackend.repository.MonTotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonTotService {

    @Autowired
    private MonTotRepository repository;

    public List<MonTotDTO> getAllMonTots() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MonTotDTO> getByAccNbrAndBillCycle(String accNbr, String billCycle) {
        MonTotId id = new MonTotId();
        id.setAccNbr(accNbr);
        id.setBillCycle(billCycle);
        return repository.findById(id).map(this::convertToDTO);
    }

    public List<MonTotDTO> getByAccNbr(String accNbr) {
        return repository.findByAccNbr(accNbr).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MonTotDTO> getByAreaCodeAndBillCycle(String areaCode, String billCycle) {
        return repository.findByAreaCodeAndBillCycle(areaCode, billCycle).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MonTotDTO convertToDTO(MonTot entity) {
        MonTotDTO dto = new MonTotDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setBillCycle(entity.getBillCycle());
        dto.setErrStat(entity.getErrStat());
        dto.setBfBal(entity.getBfBal());
        dto.setTotUntsKwo(entity.getTotUntsKwo());
        dto.setTotUntsKwd(entity.getTotUntsKwd());
        dto.setTotUntsKwp(entity.getTotUntsKwp());
        dto.setTotKva(entity.getTotKva());
        dto.setTotKwoChg(entity.getTotKwoChg());
        dto.setTotKwdChg(entity.getTotKwdChg());
        dto.setTotKwpChg(entity.getTotKwpChg());
        dto.setTotKvaChg(entity.getTotKvaChg());
        dto.setTotCharge(entity.getTotCharge());
        dto.setFixedChg(entity.getFixedChg());
        dto.setTotGst(entity.getTotGst());
        dto.setTotAmt(entity.getTotAmt());
        dto.setDebtTot(entity.getDebtTot());
        dto.setCrdtTot(entity.getCrdtTot());
        dto.setPayTot(entity.getPayTot());
        dto.setCrntBal(entity.getCrntBal());
        dto.setKwoAvg3(entity.getKwoAvg3());
        dto.setKwoAvg6(entity.getKwoAvg6());
        dto.setKwoAvg12(entity.getKwoAvg12());
        dto.setKwdAvg3(entity.getKwdAvg3());
        dto.setKwdAvg6(entity.getKwdAvg6());
        dto.setKwdAvg12(entity.getKwdAvg12());
        dto.setKwpAvg3(entity.getKwpAvg3());
        dto.setKwpAvg6(entity.getKwpAvg6());
        dto.setKwpAvg12(entity.getKwpAvg12());
        dto.setKvaAvg3(entity.getKvaAvg3());
        dto.setKvaAvg6(entity.getKvaAvg6());
        dto.setKvaAvg12(entity.getKvaAvg12());
        dto.setTotBillStat(entity.getTotBillStat());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}