package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PendingMeterReadingsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
        
        @JsonProperty("account_number")
        private String accountNumber; // For single customer requests
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("pending_readings")
        private List<PendingMeterReadingDetailsDTO> pendingReadings;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingDetailsDTO {
        // Customer Basic Information
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("tariff")
        private String tariff;
        
        @JsonProperty("reader_code")
        private String readerCode;
        
        @JsonProperty("daily_pack")
        private String dailyPack;
        
        @JsonProperty("walk_order")
        private String walkOrder;
        
        // Bill Cycle Information
        @JsonProperty("current_bill_cycle")
        private String currentBillCycle;
        
        @JsonProperty("bill_cycle_date")
        private Date billCycleDate;
        
        // Area Information
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        // Customer Details
        @JsonProperty("installation_id")
        private String installationId;
        
        @JsonProperty("customer_category")
        private String customerCategory;
        
        // Reading Dates
        @JsonProperty("reading_date")
        private Date readingDate; // Will be null - user will select
        
        @JsonProperty("previous_reading_date")
        private Date previousReadingDate;
        
        @JsonProperty("number_of_days")
        private Integer numberOfDays;
        
        // Meter Sequence and Balance
        @JsonProperty("meter_sequence")
        private Integer meterSequence;
        
        @JsonProperty("bf_balance")
        private BigDecimal bfBalance;
        
        // VAT Information
        @JsonProperty("vat_applicable")
        private String vatApplicable; // "Y" or null
        
        // Meter Types and Details
        @JsonProperty("meter_types")
        private List<MeterTypePendingDTO> meterTypes;
        
        // Summary
        @JsonProperty("total_meters")
        private Integer totalMeters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterTypePendingDTO {
        @JsonProperty("meter_type")
        private String meterType; // KWO, KWD, KWP, KVA, KVAH
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("present_reading")
        private Integer presentReading; // Will be null - user will enter
        
        @JsonProperty("previous_reading")
        private Integer previousReading;
        
        @JsonProperty("multiplied_by")
        private BigDecimal multipliedBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinglePendingReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("pending_reading")
        private PendingMeterReadingDetailsDTO pendingReading;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}