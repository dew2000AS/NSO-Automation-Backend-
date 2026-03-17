package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalSummaryDTO {
    private String accNbr;
    private String jnlType;
    private Integer jnlNo;
    private BigDecimal adjustAmt;
}