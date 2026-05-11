package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.NcreDeveloperDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import com.example.SPSProjectBackend.exception.DeveloperAlreadyExistsException;

@Service
public class NcreDeveloperService {

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Transactional
    public NcreDeveloper saveDeveloper(NcreDeveloperDTO dto) {
        NcreDeveloper entity = new NcreDeveloper();

        // set acc_nbr from incoming payload (accountNumber)
        String accNbr = dto.getAccountNumber() != null ? dto.getAccountNumber().trim() : null;
        if (accNbr == null || accNbr.isEmpty()) {
            throw new IllegalArgumentException("accountNumber is required");
        }
        if (ncreDeveloperRepository.existsById(accNbr)) {
            throw new DeveloperAlreadyExistsException(accNbr);
        }
        entity.setAccNbr(accNbr);

        entity.setFolioNo(dto.getFolioNumber());
        entity.setFileRefNo(dto.getFileReferenceNo());
        entity.setSrNoUptoDate(dto.getSrNoUptoDate());
        entity.setTariffType(dto.getTariffType());
        entity.setFileNo(dto.getFileNo());
        entity.setProvince(dto.getProvince());
        entity.setDeveloperName(dto.getDeveloperName());
        entity.setFacilityName(dto.getProjectName());
        entity.setCommissionedCapacityMw(dto.getCommissionedCapacityMw());
        entity.setLoiIssued(dto.getLoiIssued());
        entity.setSppaSigned(toDate(dto.getSppaSignedDate()));
        entity.setGridConnectionDate(toDate(dto.getGridConnectionDate()));
        entity.setReferenceCode(dto.getReferenceCode());
        entity.setRegion(dto.getRegion());
        entity.setSrNo(dto.getSrNo());
        entity.setArea(dto.getArea());
        entity.setType(dto.getNcreType());
        entity.setGridSubstation(dto.getGridSubstation());
        entity.setInitialTariff(dto.getInitialTariff());
        entity.setExpirationDate(toDate(dto.getExpirationDate()));
        entity.setExpirationExtensionDate(toDate(dto.getExpirationExtensionDate()));
        entity.setSppaCapacityMw(dto.getSppaSignedCapacityMw());
        entity.setFeederNo(dto.getFeederNo());
        entity.setCommissionedYear(dto.getCommissionedYear());
        entity.setAcExpirationWithExtension(toDate(dto.getAcExpirationWithExtension()));
        entity.setEx(toDate(dto.getExDate()));
        entity.setAc(toDate(dto.getAcDate()));
        entity.setFlat(toDate(dto.getFlatDate()));
        entity.setTtt(toDate(dto.getTttDate()));
        entity.setFirstTier(toDate(dto.getFirstTierDate()));
        entity.setSecondTier(toDate(dto.getSecondTierDate()));
        entity.setThirdTier(toDate(dto.getThirdTierDate()));
        entity.setNewSppaSigned(dto.getNewSppaSigned());
        entity.setValidityStart(toDate(dto.getValidityStart()));
        entity.setValidityExpiry(toDate(dto.getValidityExpiry()));
        entity.setInitialTariffRevised(dto.getInitialTariffRevised());
        entity.setRecommissionedOn(toDate(dto.getRecommissionedOn()));
        entity.setEpExpired(toDate(dto.getEpExpired()));
        entity.setGlExpired(toDate(dto.getGlExpired()));
        entity.setProjectStatus(dto.getProjectStatus());
        entity.setVoltageLevelKv(dto.getVoltageLevelKv());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setAddressLine3(dto.getAddressLine3());
        entity.setContactPerson(dto.getContactPerson());
        entity.setTelephone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompanyGroup(dto.getCompanyGroup());
        entity.setTenderedOrNot(dto.getTenderOrNot());
        entity.setReductions(dto.getReductions());
        entity.setAgreementType(dto.getAgreementType());
        entity.setLongitude(dto.getLongitude());
        entity.setLatitude(dto.getLatitude());

        return ncreDeveloperRepository.save(entity);
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public NcreDeveloper findDeveloperByField(String field, String value) {
        switch (field) {
            case "acc_nbr":
                return ncreDeveloperRepository.findByAccNbr(value).orElse(null);
            case "project_name":
                return ncreDeveloperRepository.findByFacilityName(value).orElse(null); // Add to repository
            case "file_no":
                return ncreDeveloperRepository.findByFileNo(Short.valueOf(value)).orElse(null); // Add to repository
            case "folio_no":
                return ncreDeveloperRepository.findByFolioNo(Short.valueOf(value)).orElse(null); // Add to repository
            default:
                throw new IllegalArgumentException("Invalid search field");
        }
    }

    @Transactional
    public NcreDeveloper updateDeveloper(String accNbr, NcreDeveloperDTO dto) {
        NcreDeveloper existing = ncreDeveloperRepository.findByAccNbr(accNbr)
                .orElseThrow(() -> new RuntimeException("Developer not found"));

        // Update fields from DTO (similar to saveDeveloper, but update existing entity)
        existing.setFolioNo(dto.getFolioNumber());
        existing.setFileRefNo(dto.getFileReferenceNo());
        existing.setSrNoUptoDate(dto.getSrNoUptoDate());
        existing.setTariffType(dto.getTariffType());
        existing.setFileNo(dto.getFileNo());
        existing.setProvince(dto.getProvince());
        existing.setDeveloperName(dto.getDeveloperName());
        existing.setFacilityName(dto.getProjectName());
        existing.setCommissionedCapacityMw(dto.getCommissionedCapacityMw());
        existing.setLoiIssued(dto.getLoiIssued());
        existing.setSppaSigned(toDate(dto.getSppaSignedDate()));
        existing.setGridConnectionDate(toDate(dto.getGridConnectionDate()));
        existing.setReferenceCode(dto.getReferenceCode());
        existing.setRegion(dto.getRegion());
        existing.setSrNo(dto.getSrNo());
        existing.setArea(dto.getArea());
        existing.setType(dto.getNcreType());
        existing.setGridSubstation(dto.getGridSubstation());
        existing.setInitialTariff(dto.getInitialTariff());
        existing.setExpirationDate(toDate(dto.getExpirationDate()));
        existing.setExpirationExtensionDate(toDate(dto.getExpirationExtensionDate()));
        existing.setSppaCapacityMw(dto.getSppaSignedCapacityMw());
        existing.setFeederNo(dto.getFeederNo());
        existing.setCommissionedYear(dto.getCommissionedYear());
        existing.setAcExpirationWithExtension(toDate(dto.getAcExpirationWithExtension()));
        existing.setEx(toDate(dto.getExDate()));
        existing.setAc(toDate(dto.getAcDate()));
        existing.setFlat(toDate(dto.getFlatDate()));
        existing.setTtt(toDate(dto.getTttDate()));
        existing.setFirstTier(toDate(dto.getFirstTierDate()));
        existing.setSecondTier(toDate(dto.getSecondTierDate()));
        existing.setThirdTier(toDate(dto.getThirdTierDate()));
        existing.setNewSppaSigned(dto.getNewSppaSigned());
        existing.setValidityStart(toDate(dto.getValidityStart()));
        existing.setValidityExpiry(toDate(dto.getValidityExpiry()));
        existing.setInitialTariffRevised(dto.getInitialTariffRevised());
        existing.setRecommissionedOn(toDate(dto.getRecommissionedOn()));
        existing.setEpExpired(toDate(dto.getEpExpired()));
        existing.setGlExpired(toDate(dto.getGlExpired()));
        existing.setProjectStatus(dto.getProjectStatus());
        existing.setVoltageLevelKv(dto.getVoltageLevelKv());
        existing.setAddressLine1(dto.getAddressLine1());
        existing.setAddressLine2(dto.getAddressLine2());
        existing.setAddressLine3(dto.getAddressLine3());
        existing.setContactPerson(dto.getContactPerson());
        existing.setTelephone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setCompanyGroup(dto.getCompanyGroup());
        existing.setTenderedOrNot(dto.getTenderOrNot());
        existing.setReductions(dto.getReductions());
        existing.setAgreementType(dto.getAgreementType());
        existing.setLongitude(dto.getLongitude());
        existing.setLatitude(dto.getLatitude());

        return ncreDeveloperRepository.save(existing);
    }

    
}

