package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.NetmeterDTO;
import com.example.SPSProjectBackend.model.Netmeter;
import com.example.SPSProjectBackend.repository.NetmeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NetmeterService {

    @Autowired
    private NetmeterRepository repository;

    public List<NetmeterDTO> getAllNetmeters() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<NetmeterDTO> getNetmeterByAccNbr(String accNbr) {
        return repository.findById(accNbr)
                .map(this::convertToDTO);
    }

    private NetmeterDTO convertToDTO(Netmeter entity) {
        NetmeterDTO dto = new NetmeterDTO();
        dto.setAccNbr(entity.getAccNbr());
        dto.setBfUnits(entity.getBfUnits());
        dto.setAvgImp(entity.getAvgImp());
        dto.setAvgExp(entity.getAvgExp());
        dto.setNetType(entity.getNetType());
        dto.setAgrmntDate(entity.getAgrmntDate());
        dto.setRate1(entity.getRate1());
        dto.setRate2(entity.getRate2());
        dto.setPeriod1(entity.getPeriod1());
        dto.setPeriod2(entity.getPeriod2());
        dto.setBankCode(entity.getBankCode());
        dto.setBranCode(entity.getBranCode());
        dto.setBkAcNo(entity.getBkAcNo());
        dto.setGenCap(entity.getGenCap());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setAddedBlcy(entity.getAddedBlcy());
        dto.setSetoff(entity.getSetoff());
        dto.setSchm(entity.getSchm());
        dto.setTotReten(entity.getTotReten());
        dto.setRate3(entity.getRate3());
        return dto;
    }
}
