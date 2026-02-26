package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalUpdateDTO {
    private Integer jnlNo;
    private String accNbr;
    private String areaCd;
    private Integer addedBlcy;
    private String jnlType;
    private BigDecimal adjustAmt;
    private String adjustStat;
    private String authCode;
    private String docAttch;
    private String confirmed;
    private String userId;
    private String editedUserId;
}