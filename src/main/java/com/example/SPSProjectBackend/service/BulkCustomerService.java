package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BulkCustomerDTO;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BulkCustomerService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    // Get all customers
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getAllCustomers() {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findAllNcreCustomersTrimmed();
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all customers: " + e.getMessage(), e);
        }
    }

    // Get customer by account number - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public Optional<BulkCustomerDTO> getCustomerByAccNbr(String accNbr) {
        try {
            // Use trimmed version for lookup
            Optional<BulkCustomer> customer = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
            return customer.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer by account number: " + accNbr + " - " + e.getMessage(), e);
        }
    }

    // Get customers by area code - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByAreaCd(String areaCd) {
        try {
            // Use trimmed version for lookup
            List<BulkCustomer> customers = bulkCustomerRepository.findByAreaCdTrimmed(areaCd);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by area code: " + e.getMessage(), e);
        }
    }

    // Get customers by zone
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByZone(String zone) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByZone(zone);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by zone: " + e.getMessage(), e);
        }
    }

    // Get customers by zone and area
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByZoneAndArea(String zone, String areaCd) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByZoneAndAreaCd(zone, areaCd);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by zone and area: " + e.getMessage(), e);
        }
    }

    // Search customers by name - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomersByName(String name) {
        try {
            String searchPattern = "%" + name + "%";
            List<BulkCustomer> customers = bulkCustomerRepository.findByNameContainingTrimmed(searchPattern);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customers by name: " + e.getMessage(), e);
        }
    }

    // Get customers by tariff - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByTariff(String tariff) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByTariffTrimmed(tariff);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by tariff: " + e.getMessage(), e);
        }
    }

    // Get customers by operational status - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByOpStat(String opStat) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByOpStatTrimmed(opStat);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by operational status: " + e.getMessage(), e);
        }
    }

    // Get customer by mobile number - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public Optional<BulkCustomerDTO> getCustomerByMobileNo(String mobileNo) {
        try {
            Optional<BulkCustomer> customer = bulkCustomerRepository.findByMobileNoTrimmed(mobileNo);
            return customer.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer by mobile number: " + e.getMessage(), e);
        }
    }

    // Get customers by city - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByCity(String city) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByCityTrimmed(city);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by city: " + e.getMessage(), e);
        }
    }

    // Get customers by bill cycle
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByBillCycle(Integer billCycle) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByBillCycle(billCycle);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by bill cycle: " + e.getMessage(), e);
        }
    }

    // Search customers by multiple criteria - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomersByMultipleCriteria(String areaCd, String zone, String tariff, String opStat, String cusCat) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByMultipleCriteriaTrimmed(areaCd, zone, tariff, opStat, cusCat);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customers by multiple criteria: " + e.getMessage(), e);
        }
    }

    // General search customers
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomers(String searchTerm) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.searchCustomers(searchTerm);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customers: " + e.getMessage(), e);
        }
    }

    // Create new customer - USING TRIMMED VERSION FOR VALIDATION
    @Transactional
    public BulkCustomerDTO createCustomer(BulkCustomerDTO customerDTO) {
        try {
            // Validate required fields
            validateCustomer(customerDTO);

            // Check if customer already exists - USING TRIMMED VERSION
            if (bulkCustomerRepository.existsByAccNbrTrimmed(customerDTO.getAccNbr())) {
                throw new RuntimeException("Customer with account number " + customerDTO.getAccNbr() + " already exists");
            }

            BulkCustomer customer = convertToEntity(customerDTO);
            
            // Set timestamps
            customer.setEnteredDtime(new Date());
            
            BulkCustomer savedCustomer = bulkCustomerRepository.save(customer);
            bulkCustomerRepository.flush();
            
            return convertToDTO(savedCustomer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    // Update existing customer - USING TRIMMED VERSION FOR LOOKUP
    @Transactional
    public BulkCustomerDTO updateCustomer(String accNbr, BulkCustomerDTO customerDTO) {
        try {
            // Validate required fields
            validateCustomer(customerDTO);

            // Use trimmed version for lookup
            Optional<BulkCustomer> existingCustomer = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
            if (!existingCustomer.isPresent()) {
                throw new RuntimeException("Customer with account number " + accNbr + " not found");
            }

            BulkCustomer customer = existingCustomer.get();
            
            // Update fields
            updateCustomerFields(customer, customerDTO);
            
            // Set edit timestamp
            customer.setEditedDtime(new Date());
            if (customerDTO.getEditedUserId() != null) {
                customer.setEditedUserId(customerDTO.getEditedUserId());
            }

            BulkCustomer updatedCustomer = bulkCustomerRepository.save(customer);
            bulkCustomerRepository.flush();
            
            return convertToDTO(updatedCustomer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    // Get distinct values for dropdowns
    @Transactional(readOnly = true)
    public List<String> getDistinctAreaCodes() {
        try {
            return bulkCustomerRepository.findDistinctAreaCodes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct area codes: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctZones() {
        try {
            return bulkCustomerRepository.findDistinctZones();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct zones: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctTariffs() {
        try {
            return bulkCustomerRepository.findDistinctTariffs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct tariffs: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCusCategories() {
        try {
            return bulkCustomerRepository.findDistinctCusCategories();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct customer categories: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        try {
            return bulkCustomerRepository.findDistinctCities();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct cities: " + e.getMessage(), e);
        }
    }

    // Count methods - USING TRIMMED VERSIONS
    @Transactional(readOnly = true)
    public Long countCustomersByArea(String areaCd) {
        try {
            return bulkCustomerRepository.countByAreaCd(areaCd);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count customers by area: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Long countCustomersByZone(String zone) {
        try {
            return bulkCustomerRepository.countByZone(zone);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count customers by zone: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Long countCustomersByOpStat(String opStat) {
        try {
            return bulkCustomerRepository.countByOpStat(opStat);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count customers by operational status: " + e.getMessage(), e);
        }
    }

    // Get customers by multiple account numbers - USING TRIMMED VERSIONS
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByAccNbrs(List<String> accNbrs, String areaCd) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByAccNbrInAndAreaCd(accNbrs, areaCd);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by account numbers: " + e.getMessage(), e);
        }
    }

    // Check if customer exists - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public boolean customerExists(String accNbr) {
        try {
            return bulkCustomerRepository.existsByAccNbrTrimmed(accNbr);
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to validate customer
    private void validateCustomer(BulkCustomerDTO customerDTO) {
        if (customerDTO.getAccNbr() == null || customerDTO.getAccNbr().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (customerDTO.getName() == null || customerDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Customer name is required");
        }
        if (customerDTO.getAddressL1() == null || customerDTO.getAddressL1().trim().isEmpty()) {
            throw new RuntimeException("Address line 1 is required");
        }
        if (customerDTO.getTariff() == null || customerDTO.getTariff().trim().isEmpty()) {
            throw new RuntimeException("Tariff is required");
        }
        
        // Validate account number length
        if (customerDTO.getAccNbr().length() > 10) {
            throw new RuntimeException("Account number cannot exceed 10 characters");
        }
        
        // Validate name length
        if (customerDTO.getName().length() > 45) {
            throw new RuntimeException("Customer name cannot exceed 45 characters");
        }
        
        // Validate address length
        if (customerDTO.getAddressL1().length() > 45) {
            throw new RuntimeException("Address line 1 cannot exceed 45 characters");
        }
        
        // Validate tariff length
        if (customerDTO.getTariff().length() > 3) {
            throw new RuntimeException("Tariff cannot exceed 3 characters");
        }
    }

    // Helper method to update customer fields
    private void updateCustomerFields(BulkCustomer customer, BulkCustomerDTO dto) {
        if (dto.getJobNbr() != null) customer.setJobNbr(dto.getJobNbr());
        if (dto.getAreaCd() != null) customer.setAreaCd(dto.getAreaCd());
        if (dto.getBillCycle() != null) customer.setBillCycle(dto.getBillCycle());
        if (dto.getCusCat() != null) customer.setCusCat(dto.getCusCat());
        if (dto.getRedCode() != null) customer.setRedCode(dto.getRedCode());
        if (dto.getDlyPack() != null) customer.setDlyPack(dto.getDlyPack());
        if (dto.getWlkOrd() != null) customer.setWlkOrd(dto.getWlkOrd());
        if (dto.getNatSup() != null) customer.setNatSup(dto.getNatSup());
        if (dto.getName() != null) customer.setName(dto.getName());
        if (dto.getAddressL1() != null) customer.setAddressL1(dto.getAddressL1());
        if (dto.getAddressL2() != null) customer.setAddressL2(dto.getAddressL2());
        if (dto.getCity() != null) customer.setCity(dto.getCity());
        if (dto.getPCode() != null) customer.setPCode(dto.getPCode());
        if (dto.getTelNbr() != null) customer.setTelNbr(dto.getTelNbr());
        if (dto.getIdNbr() != null) customer.setIdNbr(dto.getIdNbr());
        if (dto.getIdType() != null) customer.setIdType(dto.getIdType());
        if (dto.getIssuedDt() != null) customer.setIssuedDt(dto.getIssuedDt());
        if (dto.getNoOfPhases() != null) customer.setNoOfPhases(dto.getNoOfPhases());
        if (dto.getEstAmnt() != null) customer.setEstAmnt(dto.getEstAmnt());
        if (dto.getEspayDt() != null) customer.setEspayDt(dto.getEspayDt());
        if (dto.getEstPivNbr() != null) customer.setEstPivNbr(dto.getEstPivNbr());
        if (dto.getCdPrmses() != null) customer.setCdPrmses(dto.getCdPrmses());
        if (dto.getIndType() != null) customer.setIndType(dto.getIndType());
        if (dto.getDepositAmt() != null) customer.setDepositAmt(dto.getDepositAmt());
        if (dto.getDepDate() != null) customer.setDepDate(dto.getDepDate());
        if (dto.getDepPivNbr() != null) customer.setDepPivNbr(dto.getDepPivNbr());
        if (dto.getAddDepAmt() != null) customer.setAddDepAmt(dto.getAddDepAmt());
        if (dto.getAddDepDate() != null) customer.setAddDepDate(dto.getAddDepDate());
        if (dto.getAddDepPiv() != null) customer.setAddDepPiv(dto.getAddDepPiv());
        if (dto.getTotSecDep() != null) customer.setTotSecDep(dto.getTotSecDep());
        if (dto.getCntrDmnd() != null) customer.setCntrDmnd(dto.getCntrDmnd());
        if (dto.getTariff() != null) customer.setTariff(dto.getTariff());
        if (dto.getGstApl() != null) customer.setGstApl(dto.getGstApl());
        if (dto.getTaxInv() != null) customer.setTaxInv(dto.getTaxInv());
        if (dto.getTaxNum() != null) customer.setTaxNum(dto.getTaxNum());
        if (dto.getAuthLetter() != null) customer.setAuthLetter(dto.getAuthLetter());
        if (dto.getAgrmntNo() != null) customer.setAgrmntNo(dto.getAgrmntNo());
        if (dto.getCnectDate() != null) customer.setCnectDate(dto.getCnectDate());
        if (dto.getOpStat() != null) customer.setOpStat(dto.getOpStat());
        if (dto.getAhArhStat() != null) customer.setAhArhStat(dto.getAhArhStat());
        if (dto.getAltAddrStat() != null) customer.setAltAddrStat(dto.getAltAddrStat());
        if (dto.getAdlDpstSt() != null) customer.setAdlDpstSt(dto.getAdlDpstSt());
        if (dto.getSlfGenSt() != null) customer.setSlfGenSt(dto.getSlfGenSt());
        if (dto.getSupsdAcc() != null) customer.setSupsdAcc(dto.getSupsdAcc());
        if (dto.getRefundDep() != null) customer.setRefundDep(dto.getRefundDep());
        if (dto.getNoLoans() != null) customer.setNoLoans(dto.getNoLoans());
        if (dto.getCstSt() != null) customer.setCstSt(dto.getCstSt());
        if (dto.getInstId() != null) customer.setInstId(dto.getInstId());
        if (dto.getZone() != null) customer.setZone(dto.getZone());
        if (dto.getDateAddt() != null) customer.setDateAddt(dto.getDateAddt());
        if (dto.getCustCd() != null) customer.setCustCd(dto.getCustCd());
        if (dto.getUserId() != null) customer.setUserId(dto.getUserId());
        if (dto.getMobileNo() != null) customer.setMobileNo(dto.getMobileNo());
        if (dto.getCustType() != null) customer.setCustType(dto.getCustType());
        if (dto.getNetType() != null) customer.setNetType(dto.getNetType());
        if (dto.getCatCode() != null) customer.setCatCode(dto.getCatCode());
    }

    // Convert Entity to DTO
    private BulkCustomerDTO convertToDTO(BulkCustomer customer) {
        BulkCustomerDTO dto = new BulkCustomerDTO();
        dto.setAccNbr(customer.getAccNbr());
        dto.setJobNbr(customer.getJobNbr());
        dto.setAreaCd(customer.getAreaCd());
        dto.setBillCycle(customer.getBillCycle());
        dto.setCusCat(customer.getCusCat());
        dto.setRedCode(customer.getRedCode());
        dto.setDlyPack(customer.getDlyPack());
        dto.setWlkOrd(customer.getWlkOrd());
        dto.setNatSup(customer.getNatSup());
        dto.setName(customer.getName());
        dto.setAddressL1(customer.getAddressL1());
        dto.setAddressL2(customer.getAddressL2());
        dto.setCity(customer.getCity());
        dto.setPCode(customer.getPCode());
        dto.setTelNbr(customer.getTelNbr());
        dto.setIdNbr(customer.getIdNbr());
        dto.setIdType(customer.getIdType());
        dto.setIssuedDt(customer.getIssuedDt());
        dto.setNoOfPhases(customer.getNoOfPhases());
        dto.setEstAmnt(customer.getEstAmnt());
        dto.setEspayDt(customer.getEspayDt());
        dto.setEstPivNbr(customer.getEstPivNbr());
        dto.setCdPrmses(customer.getCdPrmses());
        dto.setIndType(customer.getIndType());
        dto.setDepositAmt(customer.getDepositAmt());
        dto.setDepDate(customer.getDepDate());
        dto.setDepPivNbr(customer.getDepPivNbr());
        dto.setAddDepAmt(customer.getAddDepAmt());
        dto.setAddDepDate(customer.getAddDepDate());
        dto.setAddDepPiv(customer.getAddDepPiv());
        dto.setTotSecDep(customer.getTotSecDep());
        dto.setCntrDmnd(customer.getCntrDmnd());
        dto.setTariff(customer.getTariff());
        dto.setGstApl(customer.getGstApl());
        dto.setTaxInv(customer.getTaxInv());
        dto.setTaxNum(customer.getTaxNum());
        dto.setAuthLetter(customer.getAuthLetter());
        dto.setAgrmntNo(customer.getAgrmntNo());
        dto.setCnectDate(customer.getCnectDate());
        dto.setOpStat(customer.getOpStat());
        dto.setAhArhStat(customer.getAhArhStat());
        dto.setAltAddrStat(customer.getAltAddrStat());
        dto.setAdlDpstSt(customer.getAdlDpstSt());
        dto.setSlfGenSt(customer.getSlfGenSt());
        dto.setSupsdAcc(customer.getSupsdAcc());
        dto.setRefundDep(customer.getRefundDep());
        dto.setNoLoans(customer.getNoLoans());
        dto.setCstSt(customer.getCstSt());
        dto.setInstId(customer.getInstId());
        dto.setZone(customer.getZone());
        dto.setDateAddt(customer.getDateAddt());
        dto.setCustCd(customer.getCustCd());
        dto.setEnteredDtime(customer.getEnteredDtime());
        dto.setEditedDtime(customer.getEditedDtime());
        dto.setUserId(customer.getUserId());
        dto.setEditedUserId(customer.getEditedUserId());
        dto.setMobileNo(customer.getMobileNo());
        dto.setCustType(customer.getCustType());
        dto.setNetType(customer.getNetType());
        dto.setCatCode(customer.getCatCode());
        dto.setNcreType(customer.getNcre_type());
        return dto;
    }

    // Convert DTO to Entity
    private BulkCustomer convertToEntity(BulkCustomerDTO dto) {
        BulkCustomer customer = new BulkCustomer();
        customer.setAccNbr(dto.getAccNbr());
        customer.setJobNbr(dto.getJobNbr());
        customer.setAreaCd(dto.getAreaCd());
        customer.setBillCycle(dto.getBillCycle());
        customer.setCusCat(dto.getCusCat());
        customer.setRedCode(dto.getRedCode());
        customer.setDlyPack(dto.getDlyPack());
        customer.setWlkOrd(dto.getWlkOrd());
        customer.setNatSup(dto.getNatSup());
        customer.setName(dto.getName());
        customer.setAddressL1(dto.getAddressL1());
        customer.setAddressL2(dto.getAddressL2());
        customer.setCity(dto.getCity());
        customer.setPCode(dto.getPCode());
        customer.setTelNbr(dto.getTelNbr());
        customer.setIdNbr(dto.getIdNbr());
        customer.setIdType(dto.getIdType());
        customer.setIssuedDt(dto.getIssuedDt());
        customer.setNoOfPhases(dto.getNoOfPhases());
        customer.setEstAmnt(dto.getEstAmnt());
        customer.setEspayDt(dto.getEspayDt());
        customer.setEstPivNbr(dto.getEstPivNbr());
        customer.setCdPrmses(dto.getCdPrmses());
        customer.setIndType(dto.getIndType());
        customer.setDepositAmt(dto.getDepositAmt());
        customer.setDepDate(dto.getDepDate());
        customer.setDepPivNbr(dto.getDepPivNbr());
        customer.setAddDepAmt(dto.getAddDepAmt());
        customer.setAddDepDate(dto.getAddDepDate());
        customer.setAddDepPiv(dto.getAddDepPiv());
        customer.setTotSecDep(dto.getTotSecDep());
        customer.setCntrDmnd(dto.getCntrDmnd());
        customer.setTariff(dto.getTariff());
        customer.setGstApl(dto.getGstApl());
        customer.setTaxInv(dto.getTaxInv());
        customer.setTaxNum(dto.getTaxNum());
        customer.setAuthLetter(dto.getAuthLetter());
        customer.setAgrmntNo(dto.getAgrmntNo());
        customer.setCnectDate(dto.getCnectDate());
        customer.setOpStat(dto.getOpStat());
        customer.setAhArhStat(dto.getAhArhStat());
        customer.setAltAddrStat(dto.getAltAddrStat());
        customer.setAdlDpstSt(dto.getAdlDpstSt());
        customer.setSlfGenSt(dto.getSlfGenSt());
        customer.setSupsdAcc(dto.getSupsdAcc());
        customer.setRefundDep(dto.getRefundDep());
        customer.setNoLoans(dto.getNoLoans());
        customer.setCstSt(dto.getCstSt());
        customer.setInstId(dto.getInstId());
        customer.setZone(dto.getZone());
        customer.setDateAddt(dto.getDateAddt());
        customer.setCustCd(dto.getCustCd());
        customer.setEnteredDtime(dto.getEnteredDtime());
        customer.setEditedDtime(dto.getEditedDtime());
        customer.setUserId(dto.getUserId());
        customer.setEditedUserId(dto.getEditedUserId());
        customer.setMobileNo(dto.getMobileNo());
        customer.setCustType(dto.getCustType());
        customer.setNetType(dto.getNetType());
        customer.setCatCode(dto.getCatCode());
        return customer;
    }
}