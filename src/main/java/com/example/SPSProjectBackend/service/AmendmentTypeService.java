package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.AmendmentTypeDTO;
import com.example.SPSProjectBackend.model.AmendmentType;
import com.example.SPSProjectBackend.repository.AmendmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AmendmentTypeService {

    @Autowired
    private AmendmentTypeRepository amendmentTypeRepository;

    @Transactional(readOnly = true)
    public List<AmendmentTypeDTO> getAllAmendmentTypes() {
        return amendmentTypeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AmendmentTypeDTO createAmendmentType(AmendmentTypeDTO dto) {
        // CRITICAL: Manual duplicate check using composite key (no DB constraint exists)
        // Create composite key from DTO
        AmendmentType.AmendmentTypeId compositeId = new AmendmentType.AmendmentTypeId(
            dto.getAmendmentType(),
            dto.getUpdateTableName(),
            dto.getFieldName(),
            dto.getAmendmentStatus()
        );

        // Check for duplicate composite key
        if (amendmentTypeRepository.existsById(compositeId)) {
            throw new IllegalArgumentException(
                String.format("Amendment type '%s' with table '%s', field '%s', and status '%s' already exists",
                    dto.getAmendmentType(), dto.getUpdateTableName(), dto.getFieldName(), dto.getAmendmentStatus())
            );
        }

        AmendmentType entity = convertToEntity(dto);
        AmendmentType saved = amendmentTypeRepository.save(entity);
        return convertToDTO(saved);
    }

    private AmendmentTypeDTO convertToDTO(AmendmentType entity) {
        return new AmendmentTypeDTO(
                entity.getAmendmentType(),
                entity.getAmendmentDescription(),
                entity.getUpdateTableName(),
                entity.getFieldName(),
                entity.getNewValueTableName(),
                entity.getNewValueFieldName(),
                entity.getDataType(),
                entity.getValueJournal(),
                entity.getAmendmentStatus(),
                entity.getUserId(),
                entity.getEnteredDateTime(),
                entity.getEditedUserId(),
                entity.getEditedDateTime()
        );
    }

    private AmendmentType convertToEntity(AmendmentTypeDTO dto) {
        return new AmendmentType(
                dto.getAmendmentType(),
                dto.getAmendmentDescription(),
                dto.getUpdateTableName(),
                dto.getFieldName(),
                dto.getNewValueTableName(),
                dto.getNewValueFieldName(),
                dto.getDataType(),
                dto.getValueJournal(),
                dto.getAmendmentStatus(),
                dto.getUserId(),
                dto.getEnteredDateTime(),
                dto.getEditedUserId(),
                dto.getEditedDateTime()
        );
    }

}
