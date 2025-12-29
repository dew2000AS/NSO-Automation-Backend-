package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "amnd_types", schema = "testdbnew")
@IdClass(AmendmentType.AmendmentTypeId.class)
@NoArgsConstructor
@AllArgsConstructor
public class AmendmentType {

    @Id
    @Column(name = "amd_type", length = 4, nullable = false)
    private String amendmentType;

    @Column(name = "amd_desc", length = 45)
    private String amendmentDescription;

    @Id
    @Column(name = "uptbl_name", length = 25)
    private String updateTableName;

    @Id
    @Column(name = "field_name", length = 15)
    private String fieldName;

    @Column(name = "nval_tblnm", length = 25)
    private String newValueTableName;

    @Column(name = "nval_fldnm", length = 15)
    private String newValueFieldName;

    @Column(name = "dt_type", length = 1)
    private String dataType;

    @Column(name = "val_jrnl", length = 1)
    private String valueJournal;

    @Id
    @Column(name = "amdt_status", length = 1)
    private String amendmentStatus;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private LocalDateTime enteredDateTime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private LocalDateTime editedDateTime;

    // Composite Primary Key Class
    public static class AmendmentTypeId implements Serializable {
        private String amendmentType;
        private String updateTableName;
        private String fieldName;
        private String amendmentStatus;

        // Default constructor
        public AmendmentTypeId() {}

        // Parameterized constructor
        public AmendmentTypeId(String amendmentType, String updateTableName, String fieldName, String amendmentStatus) {
            this.amendmentType = amendmentType;
            this.updateTableName = updateTableName;
            this.fieldName = fieldName;
            this.amendmentStatus = amendmentStatus;
        }

        // Getters and setters
        public String getAmendmentType() {
            return amendmentType;
        }

        public void setAmendmentType(String amendmentType) {
            this.amendmentType = amendmentType;
        }

        public String getUpdateTableName() {
            return updateTableName;
        }

        public void setUpdateTableName(String updateTableName) {
            this.updateTableName = updateTableName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getAmendmentStatus() {
            return amendmentStatus;
        }

        public void setAmendmentStatus(String amendmentStatus) {
            this.amendmentStatus = amendmentStatus;
        }

        // equals() method comparing all four fields
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AmendmentTypeId)) return false;
            AmendmentTypeId that = (AmendmentTypeId) o;
            return Objects.equals(amendmentType, that.amendmentType) &&
                   Objects.equals(updateTableName, that.updateTableName) &&
                   Objects.equals(fieldName, that.fieldName) &&
                   Objects.equals(amendmentStatus, that.amendmentStatus);
        }

        // hashCode() method using all four fields
        @Override
        public int hashCode() {
            return Objects.hash(amendmentType, updateTableName, fieldName, amendmentStatus);
        }
    }

}
