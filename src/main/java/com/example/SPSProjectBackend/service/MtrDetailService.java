package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.MtrDetailDTO;
import com.example.SPSProjectBackend.model.MtrDetail;
import com.example.SPSProjectBackend.repository.MtrDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MtrDetailService {

    @Autowired
    private MtrDetailRepository repository;

    public List<MtrDetailDTO> getAllMtrDetails() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MtrDetailDTO> getByInstId(String instId) {
        List<MtrDetail> details = repository.findByInstId(instId);
        if (details.isEmpty()) {
            return Optional.empty();
        }
        // Assuming one record or taking the first; adjust if multiple are expected
        return Optional.of(convertToDTO(details.get(0)));
    }

    private MtrDetailDTO convertToDTO(MtrDetail entity) {
        MtrDetailDTO dto = new MtrDetailDTO();
        dto.setInstId(entity.getInstId());
        dto.setAddedBlcy(entity.getAddedBlcy());
        dto.setMtr1set(entity.getMtr1set());
        dto.setMtr2set(entity.getMtr2set());
        dto.setMtr3set(entity.getMtr3set());
        dto.setMtrSeq(entity.getMtrSeq());
        dto.setMtrsetType(entity.getMtrsetType());
        dto.setMtrOrder(entity.getMtrOrder());
        dto.setMtrType(entity.getMtrType());
        dto.setNoOfPhases(entity.getNoOfPhases());
        dto.setMtrNbr(entity.getMtrNbr());
        dto.setPrsntRdn(entity.getPrsntRdn());
        dto.setCtRatio(entity.getCtRatio());
        dto.setMtrRatio(entity.getMtrRatio());
        dto.setMFactor(entity.getMFactor());
        dto.setEffctBlcy(entity.getEffctBlcy());
        dto.setEffctDate(entity.getEffctDate() == null ? null : new java.sql.Date(entity.getEffctDate().getTime()));
        dto.setAvgCnsp3(entity.getAvgCnsp3());
        dto.setAvgCnsp6(entity.getAvgCnsp6());
        dto.setAvgCnsp12(entity.getAvgCnsp12());
        dto.setBrCode(entity.getBrCode());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime() == null ? null : new java.sql.Timestamp(entity.getEnteredDtime().getTime()));
        dto.setEditedUserid(entity.getEditedUserid());
        dto.setEditedDtime(entity.getEditedDtime() == null ? null : new java.sql.Timestamp(entity.getEditedDtime().getTime()));
        return dto;
    }
}
