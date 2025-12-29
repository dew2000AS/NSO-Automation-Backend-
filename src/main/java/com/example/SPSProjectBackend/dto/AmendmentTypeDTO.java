package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmendmentTypeDTO {

    private String amendmentType;
    private String amendmentDescription;
    private String updateTableName;
    private String fieldName;
    private String newValueTableName;
    private String newValueFieldName;
    private String dataType;
    private String valueJournal;
    private String amendmentStatus;
    private String userId;
    private LocalDateTime enteredDateTime;
    private String editedUserId;
    private LocalDateTime editedDateTime;

}
