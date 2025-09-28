package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MeterReadingInfoDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingInfoResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("meter_reading_info")
        private MeterReadingInfoDetailsDTO meterReadingInfo;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingInfoDetailsDTO {
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
        private Date readingDate;
        
        @JsonProperty("previous_reading_date")
        private Date previousReadingDate;
        
        @JsonProperty("number_of_days")
        private Integer numberOfDays;
        
        // Meter Sequence and Balance
        @JsonProperty("meter_sequence")
        private Integer meterSequence;
        
        @JsonProperty("bf_balance")
        private BigDecimal bfBalance;
        
        // Meter Types and their readings
        @JsonProperty("meter_types")
        private List<MeterTypeDetailsDTO> meterTypes;
        
        // Charges Summary
        @JsonProperty("fixed_charge")
        private BigDecimal fixedCharge;
        
        @JsonProperty("monthly_charge")
        private BigDecimal monthlyCharge;
        
        @JsonProperty("vat_percentage")
        private BigDecimal vatPercentage = new BigDecimal("15.0");
        
        @JsonProperty("vat_amount")
        private BigDecimal vatAmount;
        
        @JsonProperty("total_amount")
        private BigDecimal totalAmount;
        
        // Status Information
        @JsonProperty("has_reading")
        private Boolean hasReading;
        
        @JsonProperty("reading_status")
        private String readingStatus; // "RECEIVED" or "PENDING"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterTypeDetailsDTO {
        @JsonProperty("meter_type")
        private String meterType; // KWO, KWD, KWP, KVA, KVAH
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("present_reading")
        private Integer presentReading;
        
        @JsonProperty("previous_reading")
        private Integer previousReading;
        
        @JsonProperty("units")
        private Integer units;
        
        @JsonProperty("assessed_code")
        private String assessedCode;
        
        @JsonProperty("multiplied_by")
        private BigDecimal multipliedBy;
        
        @JsonProperty("rate")
        private BigDecimal rate;
        
        @JsonProperty("amount")
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkMeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("account_numbers")
        private List<String> accountNumbers;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkMeterReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;
        
        @JsonProperty("total_customers")
        private Integer totalCustomers;
        
        @JsonProperty("customers_with_readings")
        private Integer customersWithReadings;
        
        @JsonProperty("customers_without_readings")
        private Integer customersWithoutReadings;
        
        @JsonProperty("meter_readings")
        private List<MeterReadingInfoDetailsDTO> meterReadings;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}