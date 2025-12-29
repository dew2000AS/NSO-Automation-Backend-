package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_acc_nbr", columnList = "acc_nbr"),
    @Index(name = "idx_area_cd", columnList = "area_cd"),
    @Index(name = "idx_job_nbr", columnList = "job_nbr"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_tariff", columnList = "tariff"),
    @Index(name = "idx_zone_area", columnList = "zone, area_cd")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCustomer {
    
    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;
    
    @Column(name = "job_nbr", length = 15)
    private String jobNbr;
    
    @Column(name = "area_cd", length = 2)
    private String areaCd;
    
    @Column(name = "bill_cycle")
    private Integer billCycle;
    
    @Column(name = "cus_cat", length = 1)
    private String cusCat;
    
    @Column(name = "red_code", length = 2)
    private String redCode;
    
    @Column(name = "dly_pack", length = 2)
    private String dlyPack;
    
    @Column(name = "wlk_ord", length = 6)
    private String wlkOrd;
    
    @Column(name = "nat_sup", length = 1)
    private String natSup;
    
    @Column(name = "name", length = 45, nullable = false)
    private String name;
    
    @Column(name = "address_l1", length = 45, nullable = false)
    private String addressL1;
    
    @Column(name = "address_l2", length = 30)
    private String addressL2;
    
    @Column(name = "city", length = 15)
    private String city;
    
    @Column(name = "p_code", length = 5)
    private String pCode;
    
    @Column(name = "tel_nbr", length = 16)
    private String telNbr;
    
    @Column(name = "id_nbr", length = 12)
    private String idNbr;
    
    @Column(name = "id_type", length = 1)
    private String idType;
    
    @Column(name = "issued_dt")
    @Temporal(TemporalType.DATE)
    private Date issuedDt;
    
    @Column(name = "no_of_phases")
    private Integer noOfPhases;
    
    @Column(name = "est_amnt", precision = 10, scale = 2)
    private BigDecimal estAmnt;
    
    @Column(name = "espay_dt")
    @Temporal(TemporalType.DATE)
    private Date espayDt;
    
    @Column(name = "est_piv_nbr", length = 10)
    private String estPivNbr;
    
    @Column(name = "cd_prmses", length = 1)
    private String cdPrmses;
    
    @Column(name = "ind_type", length = 6)
    private String indType;
    
    @Column(name = "deposit_amt", precision = 13, scale = 2)
    private BigDecimal depositAmt;
    
    @Column(name = "dep_date")
    @Temporal(TemporalType.DATE)
    private Date depDate;
    
    @Column(name = "dep_piv_nbr", length = 10)
    private String depPivNbr;
    
    @Column(name = "add_dep_amt", precision = 13, scale = 2)
    private BigDecimal addDepAmt;
    
    @Column(name = "add_dep_date")
    @Temporal(TemporalType.DATE)
    private Date addDepDate;
    
    @Column(name = "add_dep_piv", length = 10)
    private String addDepPiv;
    
    @Column(name = "tot_sec_dep", precision = 13, scale = 2)
    private BigDecimal totSecDep;
    
    @Column(name = "cntr_dmnd", precision = 8, scale = 2)
    private BigDecimal cntrDmnd;
    
    @Column(name = "tariff", length = 3, nullable = false)
    private String tariff;
    
    @Column(name = "gst_apl", length = 1)
    private String gstApl;
    
    @Column(name = "tax_inv", length = 1)
    private String taxInv;
    
    @Column(name = "tax_num", length = 20)
    private String taxNum;
    
    @Column(name = "auth_letter", length = 1)
    private String authLetter;
    
    @Column(name = "agrmnt_no", length = 15)
    private String agrmntNo;
    
    @Column(name = "cnect_date")
    @Temporal(TemporalType.DATE)
    private Date cnectDate;
    
    @Column(name = "op_stat", length = 1)
    private String opStat;
    
    @Column(name = "ah_arh_stat", length = 1)
    private String ahArhStat;
    
    @Column(name = "alt_addr_stat", length = 1)
    private String altAddrStat;
    
    @Column(name = "adl_dpst_st", length = 1)
    private String adlDpstSt;
    
    @Column(name = "slf_gen_st", length = 1)
    private String slfGenSt;
    
    @Column(name = "supsd_acc")
    private Integer supsdAcc;
    
    @Column(name = "refund_dep", length = 1)
    private String refundDep;
    
    @Column(name = "no_loans")
    private Integer noLoans;
    
    @Column(name = "cst_st")
    private Integer cstSt;
    
    @Column(name = "inst_id", length = 8)
    private String instId;
    
    @Column(name = "zone", length = 2)
    private String zone;
    
    @Column(name = "date_addt")
    @Temporal(TemporalType.DATE)
    private Date dateAddt;
    
    @Column(name = "cust_cd", length = 4)
    private String custCd;
    
    @Column(name = "entered_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDtime;
    
    @Column(name = "edited_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedDtime;
    
    @Column(name = "user_id", length = 8)
    private String userId;
    
    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;
    
    @Column(name = "mobile_no", length = 15)
    private String mobileNo;
    
    @Column(name = "cust_type", length = 1)
    private String custType;
    
    @Column(name = "net_type", length = 1)
    private String netType;
    
    @Column(name = "cat_code", length = 4)
    private String catCode;
    
    @Column(name = "cnnct_type")
    private Integer cnnctType;
    
    @Column(name = "bess")
    private Integer bess;
}