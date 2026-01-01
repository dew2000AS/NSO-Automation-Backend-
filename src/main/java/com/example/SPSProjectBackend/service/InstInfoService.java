package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InstInfoDTO;
import com.example.SPSProjectBackend.model.InstInfo;
import com.example.SPSProjectBackend.repository.InstInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstInfoService {

    @Autowired
    private InstInfoRepository repository;

    public List<InstInfoDTO> getAllInstInfo() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<InstInfoDTO> getByInstId(String instId) {
        // Use native query and trim for safety against whitespace/dialect issues
        return repository.findByInstIdNative(instId.trim())
                .map(this::convertToDTO);
    }

    private InstInfoDTO convertToDTO(InstInfo entity) {
        InstInfoDTO dto = new InstInfoDTO();
        dto.setInstId(entity.getInstId());
        dto.setDateCnnct(entity.getDateCnnct());
        dto.setMtrSet(entity.getMtrSet());
        dto.setNbrMet(entity.getNbrMet());
        dto.setCustNum(entity.getCustNum());
        dto.setTypeMet(entity.getTypeMet());
        dto.setDpCode(entity.getDpCode());
        dto.setTrCb(entity.getTrCb());
        dto.setCnnctTrpnl(entity.getCnnctTrpnl());
        dto.setTrpnlVolt(entity.getTrpnlVolt());
        dto.setTrpnlAmps(entity.getTrpnlAmps());
        dto.setUserId(entity.getUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEditedDtime(entity.getEditedDtime());
        return dto;
    }
}