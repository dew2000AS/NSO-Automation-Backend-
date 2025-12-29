package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "tmp_customer")
public class TmpCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cust_type", columnDefinition = "CHAR(1) NOT NULL")
    private String custType;

    @Column(name = "acc_nbr", length = 10)
    private String accNbr;

    @Column(name = "job_nbr", length = 15)
    private String jobNbr;

    @Column(name = "gen_acc_nbr", length = 10)
    private String genAccNbr;

    @Column(name = "area_cd", length = 2)
    private String areaCd;

    @Column(name = "cust_num", length = 2)
    private String custNum;

    @Column(name = "bill_cycle")
    private Short billCycle;

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

    @Column(name = "address_l1", length = 30, nullable = false)
    private String addressL1;

    @Column(name = "address_l2", length = 30)
    private String addressL2;

    @Column(name = "city", length = 15)
    private String city;

    @Column(name = "p_code", length = 5)
    private String pCode;

    @Column(name = "tel_nbr", length = 16)
    private String telNbr;

    @Column(name = "id_type", length = 1)
    private String idType;

    @Column(name = "id_nbr", length = 12)
    private String idNbr;

    @Temporal(TemporalType.DATE)
    @Column(name = "issued_dt")
    private Date issuedDt;

    @Column(name = "no_of_phases")
    private Short noOfPhases;

    @Column(name = "est_amnt", precision = 10, scale = 2)
    private BigDecimal estAmnt;

    @Temporal(TemporalType.DATE)
    @Column(name = "espay_dt")
    private Date espayDt;

    @Column(name = "est_piv_nbr", length = 10)
    private String estPivNbr;

    @Column(name = "cd_prmses", length = 1)
    private String cdPrmses;

    @Column(name = "ind_type", length = 6)
    private String indType;

    @Column(name = "deposit_amt", precision = 10, scale = 2)
    private BigDecimal depositAmt;

    @Temporal(TemporalType.DATE)
    @Column(name = "dep_date")
    private Date depDate;

    @Column(name = "dep_piv_nbr", length = 10)
    private String depPivNbr;

    @Column(name = "add_dep_amt", precision = 10, scale = 2)
    private BigDecimal addDepAmt;

    @Temporal(TemporalType.DATE)
    @Column(name = "add_dep_date")
    private Date addDepDate;

    @Column(name = "add_dep_piv", length = 10)
    private String addDepPiv;

    @Column(name = "cntr_dmnd", precision = 8, scale = 2)
    private BigDecimal cntrDmnd;

    @Column(name = "type_met", length = 2)
    private String typeMet;

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

    @Temporal(TemporalType.DATE)
    @Column(name = "cnect_date")
    private Date cnectDate;

    @Column(name = "op_stat", length = 1)
    private String opStat;

    @Column(name = "ah_arh_stat", length = 1)
    private String ahArhStat;

    @Column(name = "alt_addr_stat", length = 1)
    private String altAddrStat;

    @Column(name = "adl_dpst_st", length = 1)
    private String adlDpstSt;

    @Column(name = "supsd_acc")
    private Short supsdAcc;

    @Column(name = "no_loans")
    private Short noLoans;

    @Column(name = "cst_st")
    private Short cstSt;

    @Column(name = "sig_name", length = 30)
    private String sigName;

    @Column(name = "sig_desig", length = 20)
    private String sigDesig;

    @Column(name = "sig_address1", length = 30)
    private String sigAddress1;

    @Column(name = "sig_address2", length = 30)
    private String sigAddress2;

    @Column(name = "sig_address3", length = 30)
    private String sigAddress3;

    @Column(name = "sig_city", length = 15)
    private String sigCity;

    @Column(name = "sig_tel_1", length = 16)
    private String sigTel1;

    @Column(name = "sig_tel_2", length = 16)
    private String sigTel2;

    @Column(name = "sig_fax_1", length = 16)
    private String sigFax1;

    @Column(name = "sig_fax_2", length = 16)
    private String sigFax2;

    @Column(name = "sig_email", length = 20)
    private String sigEmail;

    @Column(name = "cnt_name", length = 30)
    private String cntName;

    @Column(name = "cnt_desig", length = 20)
    private String cntDesig;

    @Column(name = "cnt_address1", length = 30)
    private String cntAddress1;

    @Column(name = "cnt_address2", length = 30)
    private String cntAddress2;

    @Column(name = "cnt_city", length = 15)
    private String cntCity;

    @Column(name = "cnt_tel_1", length = 12)
    private String cntTel1;

    @Column(name = "cnt_tel_2", length = 12)
    private String cntTel2;

    @Column(name = "cnt_tel_3", length = 12)
    private String cntTel3;

    @Column(name = "cnt_fax_1", length = 13)
    private String cntFax1;

    @Column(name = "cnt_fax_2", length = 13)
    private String cntFax2;

    @Column(name = "cnt_email", length = 20)
    private String cntEmail;

    @Column(name = "own_tent", length = 6)
    private String ownTent;

    @Column(name = "deed_chk", length = 1)
    private String deedChk;

    @Column(name = "dcopy_atch", length = 1)
    private String dcopyAtch;

    @Column(name = "ll_name", length = 30)
    private String llName;

    @Column(name = "ll_address1", length = 30)
    private String llAddress1;

    @Column(name = "ll_address2", length = 30)
    private String llAddress2;

    @Column(name = "ll_address3", length = 30)
    private String llAddress3;

    @Column(name = "ll_address4", length = 15)
    private String llAddress4;

    @Column(name = "ll_pcode", length = 5)
    private String llPcode;

    @Column(name = "tagrm_chk", length = 1)
    private String tagrmChk;

    @Column(name = "acopy_atch", length = 1)
    private String acopyAtch;

    @Column(name = "cr_address1", length = 30)
    private String crAddress1;

    @Column(name = "cr_address2", length = 30)
    private String crAddress2;

    @Column(name = "cr_address3", length = 30)
    private String crAddress3;

    @Column(name = "cr_address4", length = 15)
    private String crAddress4;

    @Column(name = "cr_pcode", length = 5)
    private String crPcode;

    @Column(name = "cr_tel", length = 8)
    private String crTel;

    @Column(name = "oacc_yn", length = 1)
    private String oaccYn;

    @Column(name = "no_ins")
    private Short noIns;

    @Column(name = "oth_anbr_1", length = 10)
    private String othAnbr1;

    @Column(name = "oth_anbr_2", length = 10)
    private String othAnbr2;

    @Column(name = "oth_anbr_3", length = 10)
    private String othAnbr3;

    @Column(name = "oth_anbr_4", length = 10)
    private String othAnbr4;

    @Column(name = "sld_atch", length = 1)
    private String sldAtch;

    @Column(name = "csa_atch", length = 1)
    private String csaAtch;

    @Column(name = "capacity", length = 5)
    private String capacity;

    @Column(name = "type", length = 7)
    private String type;

    @Column(name = "no_lam")
    private Short noLam;

    @Column(name = "rat_lam", precision = 7, scale = 2)
    private BigDecimal ratLam;

    @Column(name = "no_mot")
    private Short noMot;

    @Column(name = "rat_mot", precision = 7, scale = 2)
    private BigDecimal ratMot;

    @Column(name = "no_acon")
    private Short noAcon;

    @Column(name = "rat_acon", precision = 7, scale = 2)
    private BigDecimal ratAcon;

    @Column(name = "no_whet")
    private Short noWhet;

    @Column(name = "no_ele_fan")
    private Short noEleFan;

    @Column(name = "rat_fan", precision = 7, scale = 2)
    private BigDecimal ratFan;

    @Column(name = "rat_whet", precision = 7, scale = 2)
    private BigDecimal ratWhet;

    @Column(name = "no_wplt")
    private Short noWplt;

    @Column(name = "rat_wplt", precision = 7, scale = 2)
    private BigDecimal ratWplt;

    @Column(name = "no_sol")
    private Short noSol;

    @Column(name = "rat_sol", precision = 7, scale = 2)
    private BigDecimal ratSol;

    @Column(name = "no_otha")
    private Short noOtha;

    @Column(name = "rat_otha", precision = 7, scale = 2)
    private BigDecimal ratOtha;

    @Column(name = "ans_sbsup", length = 1)
    private String ansSbsup;

    @Column(name = "nbr_rbsup")
    private Short nbrRbsup;

    @Column(name = "inst_id", length = 8)
    private String instId;

    @Column(name = "zone", length = 2)
    private String zone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_addt")
    private Date dateAddt;

    @Column(name = "type_metr", length = 1)
    private String typeMetr;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entered_dtime")
    private Date enteredDtime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_dtime")
    private Date editedDtime;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "cus_cd", length = 15)
    private String cusCd;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;
}