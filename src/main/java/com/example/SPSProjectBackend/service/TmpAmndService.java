package com.example.SPSProjectBackend.service;
import com.example.SPSProjectBackend.dto.*;
import com.example.SPSProjectBackend.model.AmndType;
import com.example.SPSProjectBackend.model.TmpAmnd;
import com.example.SPSProjectBackend.model.TmpAmndPk;
import com.example.SPSProjectBackend.repository.AmndTypeRepository;
import com.example.SPSProjectBackend.repository.TmpAmndRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class TmpAmndService {
    @Autowired
    private TmpAmndRepository tmpAmndRepository;
    // NEW: Autowired dependencies for old value fetching
    @Autowired
    private AmndTypeRepository amndTypeRepository;
    @Autowired
    private BulkCustomerService bulkCustomerService;
    @Autowired
    private MonTotService monTotService;
    @Autowired
    private MtrDetailService mtrDetailService;
    @Autowired
    private InstInfoService instInfoService;
    @Autowired
    private LoanMasterService loanMasterService;
    @Autowired
    private NetmeterService netmeterService;
    // NEW: Field maps for each table (using DTO getters)
    private Map<String, Function<BulkCustomerDTO, Object>> customerFieldMap;
    private Map<String, Function<MonTotDTO, Object>> monTotFieldMap;
    private Map<String, Function<MtrDetailDTO, Object>> mtrDetailFieldMap;
    private Map<String, Function<InstInfoDTO, Object>> instInfoFieldMap;
    private Map<String, Function<LoanMasterDTO, Object>> loanMasterFieldMap;
    private Map<String, Function<NetmeterDTO, Object>> netmeterFieldMap;
    @PostConstruct
    private void initFieldMaps() {
        // Customer fields
        customerFieldMap = new HashMap<>();
        customerFieldMap.put("AREA_CD", BulkCustomerDTO::getAreaCd);
        customerFieldMap.put("TARIFF", BulkCustomerDTO::getTariff);
        customerFieldMap.put("AGRMNT_NO", BulkCustomerDTO::getAgrmntNo);
        customerFieldMap.put("CST_ST", BulkCustomerDTO::getCstSt);
        customerFieldMap.put("NO_LOANS", BulkCustomerDTO::getNoLoans);
        customerFieldMap.put("TOT_SEC_DEP", BulkCustomerDTO::getTotSecDep);
        customerFieldMap.put("WLK_ORD", BulkCustomerDTO::getWlkOrd);
        customerFieldMap.put("DEPOSIT_AMT", BulkCustomerDTO::getDepositAmt);
        customerFieldMap.put("EST_AMNT", BulkCustomerDTO::getEstAmnt);
        customerFieldMap.put("ADD_DEP_AMT", BulkCustomerDTO::getAddDepAmt);
        customerFieldMap.put("IND_TYPE", BulkCustomerDTO::getIndType);
        customerFieldMap.put("TEL_NBR", BulkCustomerDTO::getTelNbr);
        customerFieldMap.put("DEP_DATE", BulkCustomerDTO::getDepDate);
        customerFieldMap.put("DEP_PIV_NBR", BulkCustomerDTO::getDepPivNbr);
        customerFieldMap.put("CUS_CAT", BulkCustomerDTO::getCusCat);
        customerFieldMap.put("GST_APL", BulkCustomerDTO::getGstApl);
        customerFieldMap.put("TAX_INV", BulkCustomerDTO::getTaxInv);
        customerFieldMap.put("AUTH_LETTER", BulkCustomerDTO::getAuthLetter);
        customerFieldMap.put("TAX_NUM", BulkCustomerDTO::getTaxNum);
        customerFieldMap.put("CUST_CD", BulkCustomerDTO::getCustCd);
        customerFieldMap.put("CUST_TYPE", BulkCustomerDTO::getCustType);
        customerFieldMap.put("NET_TYPE", BulkCustomerDTO::getNetType);
        customerFieldMap.put("CAT_CODE", BulkCustomerDTO::getCatCode);
        customerFieldMap.put("CNTR_DMND", BulkCustomerDTO::getCntrDmnd);
        // MON_TOT fields
        monTotFieldMap = new HashMap<>();
        monTotFieldMap.put("BF_BAL", MonTotDTO::getBfBal);
        monTotFieldMap.put("TOT_UNTSKWO", MonTotDTO::getTotUntsKwo);
        monTotFieldMap.put("TOT_UNTSKWD", MonTotDTO::getTotUntsKwd);
        monTotFieldMap.put("TOT_UNTSKWP", MonTotDTO::getTotUntsKwp);
        monTotFieldMap.put("TOT_KVA", MonTotDTO::getTotKva);
        monTotFieldMap.put("TOT_KWOCHG", MonTotDTO::getTotKwoChg);
        monTotFieldMap.put("TOT_KWDCHG", MonTotDTO::getTotKwdChg);
        monTotFieldMap.put("TOT_KWPCHG", MonTotDTO::getTotKwpChg);
        monTotFieldMap.put("TOT_KVACHG", MonTotDTO::getTotKvaChg);
        monTotFieldMap.put("TOT_CHARGE", MonTotDTO::getTotCharge);
        monTotFieldMap.put("FIXED_CHG", MonTotDTO::getFixedChg);
        monTotFieldMap.put("TOT_GST", MonTotDTO::getTotGst);
        monTotFieldMap.put("TOT_AMT", MonTotDTO::getTotAmt);
        monTotFieldMap.put("DEBT_TOT", MonTotDTO::getDebtTot);
        monTotFieldMap.put("CRDT_TOT", MonTotDTO::getCrdtTot);
        monTotFieldMap.put("PAY_TOT", MonTotDTO::getPayTot);
        monTotFieldMap.put("CRNT_BAL", MonTotDTO::getCrntBal);
        // MTR_DETAIL fields
        mtrDetailFieldMap = new HashMap<>();
        mtrDetailFieldMap.put("MTR_NBR", MtrDetailDTO::getMtrNbr);
        mtrDetailFieldMap.put("MTR_TYPE", MtrDetailDTO::getMtrType);
        mtrDetailFieldMap.put("NO_OF_PHASES", MtrDetailDTO::getNoOfPhases);
        mtrDetailFieldMap.put("PRSNT_RDN", MtrDetailDTO::getPrsntRdn);
        mtrDetailFieldMap.put("CT_RATIO", MtrDetailDTO::getCtRatio);
        mtrDetailFieldMap.put("MTR_RATIO", MtrDetailDTO::getMtrRatio);
        mtrDetailFieldMap.put("M_FACTOR", MtrDetailDTO::getMFactor);
        mtrDetailFieldMap.put("AVG_CNSP_3", MtrDetailDTO::getAvgCnsp3);
        mtrDetailFieldMap.put("AVG_CNSP_6", MtrDetailDTO::getAvgCnsp6);
        mtrDetailFieldMap.put("AVG_CNSP_12", MtrDetailDTO::getAvgCnsp12);
        // INST_INFO fields
        instInfoFieldMap = new HashMap<>();
        instInfoFieldMap.put("TR_CB", InstInfoDTO::getTrCb);
        instInfoFieldMap.put("TYPE_MET", InstInfoDTO::getTypeMet);
        instInfoFieldMap.put("TRPNL_VOLT", InstInfoDTO::getTrpnlVolt);
        instInfoFieldMap.put("TRPNL_AMPS", InstInfoDTO::getTrpnlAmps);
        instInfoFieldMap.put("MTR_SET", InstInfoDTO::getMtrSet);
        instInfoFieldMap.put("NBR_MET", InstInfoDTO::getNbrMet);
        // LOAN_MAS fields (note: table is LOAN_MASTER in code)
        loanMasterFieldMap = new HashMap<>();
        loanMasterFieldMap.put("LN_ST", LoanMasterDTO::getActiveSt);
        loanMasterFieldMap.put("INST_AMT", LoanMasterDTO::getMonPmnt);
        loanMasterFieldMap.put("LN_BAL", LoanMasterDTO::getLoanAmt);
        loanMasterFieldMap.put("LOAN_TYPE", LoanMasterDTO::getLoanType);
        loanMasterFieldMap.put("NO_MONTHS", LoanMasterDTO::getNoMonths);
        loanMasterFieldMap.put("ST_BILL_CYCLE", LoanMasterDTO::getStBillCycle);
        loanMasterFieldMap.put("END_BILL_CYCLE", LoanMasterDTO::getEndBillCycle);
        loanMasterFieldMap.put("INT_RATE", LoanMasterDTO::getIntRate);
        // NETMETER fields
        netmeterFieldMap = new HashMap<>();
        netmeterFieldMap.put("SETOFF", NetmeterDTO::getSetoff);
        netmeterFieldMap.put("SCHM", NetmeterDTO::getSchm);
        netmeterFieldMap.put("GEN_CAP", NetmeterDTO::getGenCap);
        netmeterFieldMap.put("RATE1", NetmeterDTO::getRate1);
        netmeterFieldMap.put("AGRMNT_DATE", NetmeterDTO::getAgrmntDate);
        netmeterFieldMap.put("BF_UNITS", NetmeterDTO::getBfUnits);
        netmeterFieldMap.put("AVG_IMP", NetmeterDTO::getAvgImp);
        netmeterFieldMap.put("AVG_EXP", NetmeterDTO::getAvgExp);
        netmeterFieldMap.put("NET_TYPE", NetmeterDTO::getNetType);
        netmeterFieldMap.put("RATE2", NetmeterDTO::getRate2);
        netmeterFieldMap.put("PERIOD1", NetmeterDTO::getPeriod1);
        netmeterFieldMap.put("PERIOD2", NetmeterDTO::getPeriod2);
        netmeterFieldMap.put("RATE3", NetmeterDTO::getRate3);
    }
    // NEW: Method to get old value using single logic
    public String getOldValue(String accNbr, String amdType, String billCycle) {
        Optional<AmndType> amndTypeOpt = amndTypeRepository.findById(amdType);
        if (amndTypeOpt.isEmpty()) {
            throw new RuntimeException("Amendment type not found: " + amdType);
        }
        AmndType amndType = amndTypeOpt.get();
        String uptblName = amndType.getUptblName();
        String fieldName = amndType.getFieldName();
        Object value = null;
        switch (uptblName) {
            case "CUSTOMER":
                Optional<BulkCustomerDTO> custOpt = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOpt.isPresent()) {
                    Function<BulkCustomerDTO, Object> custGetter = customerFieldMap.get(fieldName);
                    if (custGetter != null) {
                        value = custGetter.apply(custOpt.get());
                    }
                }
                break;
            case "MON_TOT":
                if (billCycle == null || billCycle.trim().isEmpty()) {
                    throw new RuntimeException("Bill cycle is required for MON_TOT amendments");
                }
                Optional<MonTotDTO> monOpt = monTotService.getByAccNbrAndBillCycle(accNbr, billCycle);
                if (monOpt.isPresent()) {
                    Function<MonTotDTO, Object> monGetter = monTotFieldMap.get(fieldName);
                    if (monGetter != null) {
                        value = monGetter.apply(monOpt.get());
                    }
                }
                break;
            case "MTR_DETAIL":
                Optional<BulkCustomerDTO> custOptMtr = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptMtr.isPresent() && custOptMtr.get().getInstId() != null) {
                    String instId = custOptMtr.get().getInstId();
                    Optional<MtrDetailDTO> mtrOpt = mtrDetailService.getByInstId(instId);
                    if (mtrOpt.isPresent()) {
                        Function<MtrDetailDTO, Object> mtrGetter = mtrDetailFieldMap.get(fieldName);
                        if (mtrGetter != null) {
                            value = mtrGetter.apply(mtrOpt.get());
                        }
                    }
                }
                break;
            case "INST_INFO":
                Optional<BulkCustomerDTO> custOptInst = bulkCustomerService.getCustomerByAccNbr(accNbr);
                if (custOptInst.isPresent() && custOptInst.get().getInstId() != null) {
                    String instId = custOptInst.get().getInstId();
                    Optional<InstInfoDTO> instOpt = instInfoService.getByInstId(instId);
                    if (instOpt.isPresent()) {
                        Function<InstInfoDTO, Object> instGetter = instInfoFieldMap.get(fieldName);
                        if (instGetter != null) {
                            value = instGetter.apply(instOpt.get());
                        }
                    }
                }
                break;
            case "LOAN_MAS":
                Optional<LoanMasterDTO> loanOpt = loanMasterService.getLoanByAccNbr(accNbr);
                if (loanOpt.isPresent()) {
                    Function<LoanMasterDTO, Object> loanGetter = loanMasterFieldMap.get(fieldName);
                    if (loanGetter != null) {
                        value = loanGetter.apply(loanOpt.get());
                    }
                }
                break;
            case "NETMETER":
                Optional<NetmeterDTO> netOpt = netmeterService.getNetmeterByAccNbr(accNbr);
                if (netOpt.isPresent()) {
                    Function<NetmeterDTO, Object> netGetter = netmeterFieldMap.get(fieldName);
                    if (netGetter != null) {
                        value = netGetter.apply(netOpt.get());
                    }
                }
                break;
            default:
                throw new RuntimeException("Unsupported table for old value: " + uptblName);
        }
        return value != null ? value.toString() : null;
    }
    public TmpAmndDTO createAmendment(TmpAmndDTO dto) {
        // Validate required for PK
        if (dto.getAccNbr() == null || dto.getAccNbr().trim().isEmpty() ||
                dto.getAmdType() == null || dto.getAmdType().trim().isEmpty() ||
                dto.getEffctBlcy() == null) {
            throw new RuntimeException("accNbr, amdType, and effctBlcy are required");
        }
        TmpAmnd entity = convertToEntity(dto);
        entity.setStatus("1");
        TmpAmnd saved = tmpAmndRepository.save(entity);
        return convertToDTO(saved);
    }
    public TmpAmndDTO updateAmendment(String accNbr, String amdType, Short effctBlcy, TmpAmndDTO dto) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        Optional<TmpAmnd> optional = tmpAmndRepository.findById(pk);
        if (!optional.isPresent()) {
            throw new RuntimeException("Amendment not found with key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        TmpAmnd entity = optional.get();
        updateAmendmentFields(entity, dto);
        // NEW: Always reset status to 1 (pending) on update
        entity.setStatus("1");
        // Ensure edited timestamp and user are set (if not already via DTO)
        if (entity.getEditedUserId() == null || entity.getEditedUserId().trim().isEmpty()) {
            entity.setEditedUserId("SYSTEM");
        }
        if (entity.getEditedDtime() == null) {
            entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        }
        TmpAmnd updated = tmpAmndRepository.save(entity);
        return convertToDTO(updated);
    }
    public List<TmpAmndDTO> getAmendmentsByAccNbr(String accNbr) {
        List<TmpAmnd> entities = tmpAmndRepository.findByIdAccNbr(accNbr);
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public void deleteAmendment(String accNbr, String amdType, Short effctBlcy) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        if (!tmpAmndRepository.existsById(pk)) {
            throw new RuntimeException("No amendment to delete for key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        tmpAmndRepository.deleteById(pk);
    }
    // UPDATED: Fetch only amendments with status == 1 (pending), optional filter by areaCd
    public List<TmpAmndDTO> getAllAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("1", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("1");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // UPDATED: Get all rejected amendments (status = 3), optional filter by areaCd
    public List<TmpAmndDTO> getRejectedAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("3", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("3");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    // UPDATED: Get all posted amendments (status = 2), optional filter by areaCd
    public List<TmpAmndDTO> getPostedAmendments(String areaCd) {
        List<TmpAmnd> entities;
        if (areaCd != null && !areaCd.isEmpty()) {
            entities = tmpAmndRepository.findByStatusAndAreaCd("2", areaCd);
        } else {
            entities = tmpAmndRepository.findByStatus("2");
        }
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public void updateStatus(String accNbr, String amdType, Short effctBlcy, String status) {
        TmpAmndPk pk = new TmpAmndPk(accNbr, amdType, effctBlcy);
        Optional<TmpAmnd> optional = tmpAmndRepository.findById(pk);
        if (!optional.isPresent()) {
            throw new RuntimeException("Amendment not found with key: " + accNbr + ", " + amdType + ", " + effctBlcy);
        }
        TmpAmnd entity = optional.get();
        entity.setStatus(status);
        entity.setEditedUserId("SYSTEM");
        entity.setEditedDtime(new Timestamp(System.currentTimeMillis()));
        tmpAmndRepository.save(entity);
    }
    private TmpAmnd convertToEntity(TmpAmndDTO dto) {
        TmpAmnd amendment = new TmpAmnd();
        TmpAmndPk pk = new TmpAmndPk(dto.getAccNbr(), dto.getAmdType(), dto.getEffctBlcy());
        amendment.setId(pk);
        if (dto.getAreaCd() != null && !dto.getAreaCd().trim().isEmpty())
            amendment.setAreaCd(dto.getAreaCd());
        if (dto.getAddedBlcy() != null)
            amendment.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getEffctDate() != null && !dto.getEffctDate().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                Date parsedDate = sdf.parse(dto.getEffctDate());
                amendment.setEffctDate(parsedDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid effective date format: " + dto.getEffctDate() + ". Expected yyyy-MM-dd", e);
            }
        }
        // Only set one of these two; do not set the other explicitly to null here
        if (dto.getNmrValue() != null) {
            amendment.setNmrValue(dto.getNmrValue());
        } else if (dto.getChrValue() != null && !dto.getChrValue().trim().isEmpty()) {
            amendment.setChrValue(dto.getChrValue());
        }
        if (dto.getAmauthCode() != null && !dto.getAmauthCode().trim().isEmpty())
            amendment.setAmauthCode(dto.getAmauthCode());
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty())
            amendment.setUserId(dto.getUserId());
        if (dto.getEditedUserId() != null && !dto.getEditedUserId().trim().isEmpty())
            amendment.setEditedUserId(dto.getEditedUserId());
        // NEW: Status - default to "1" if not provided
        if (dto.getStatus() != null) {
            amendment.setStatus(dto.getStatus());
        } else {
            amendment.setStatus("1"); // Default for create
        }
        return amendment;
    }
    private void updateAmendmentFields(TmpAmnd amendment, TmpAmndDTO dto) {
        if (dto.getAreaCd() != null && !dto.getAreaCd().trim().isEmpty())
            amendment.setAreaCd(dto.getAreaCd());
        if (dto.getAddedBlcy() != null)
            amendment.setAddedBlcy(dto.getAddedBlcy());
        if (dto.getEffctDate() != null && !dto.getEffctDate().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                Date parsedDate = sdf.parse(dto.getEffctDate());
                amendment.setEffctDate(parsedDate);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid effective date format in update: " + dto.getEffctDate() + ". Expected yyyy-MM-dd", e);
            }
        }
        // On update, explicitly nullify the other value field to avoid stale mismatch
        if (dto.getNmrValue() != null) {
            amendment.setNmrValue(dto.getNmrValue());
            amendment.setChrValue(null);
        } else if (dto.getChrValue() != null && !dto.getChrValue().trim().isEmpty()) {
            amendment.setChrValue(dto.getChrValue());
            amendment.setNmrValue(null);
        }
        if (dto.getAmauthCode() != null && !dto.getAmauthCode().trim().isEmpty())
            amendment.setAmauthCode(dto.getAmauthCode());
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty())
            amendment.setUserId(dto.getUserId());
        if (dto.getEditedUserId() != null && !dto.getEditedUserId().trim().isEmpty())
            amendment.setEditedUserId(dto.getEditedUserId());
        if (dto.getStatus() != null) {
            amendment.setStatus(dto.getStatus());
        }
    }
    private TmpAmndDTO convertToDTO(TmpAmnd entity) {
        TmpAmndDTO dto = new TmpAmndDTO();
        dto.setAccNbr(entity.getId().getAccNbr());
        dto.setAmdType(entity.getId().getAmdType());
        dto.setEffctBlcy(entity.getId().getEffctBlcy());
        dto.setAreaCd(entity.getAreaCd());
        dto.setAddedBlcy(entity.getAddedBlcy());
        if (entity.getEffctDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dto.setEffctDate(sdf.format(entity.getEffctDate()));
        }
        dto.setNmrValue(entity.getNmrValue());
        dto.setChrValue(entity.getChrValue());
        dto.setAmauthCode(entity.getAmauthCode());
        dto.setUserId(entity.getUserId());
        dto.setEditedUserId(entity.getEditedUserId());
        dto.setEnteredDtime(entity.getEnteredDtime());
        dto.setEditedDtime(entity.getEditedDtime());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}