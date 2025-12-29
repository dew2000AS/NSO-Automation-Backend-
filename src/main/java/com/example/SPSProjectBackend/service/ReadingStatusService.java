package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ReadingStatusDTO;
import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.dto.BulkCustomerDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReadingStatusService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private BulkCustomerService bulkCustomerService;

    @Autowired
    private TmpReadingsService tmpReadingsService;

    /**
     * Get reading status for user based on their access level
     */
    public ReadingStatusDTO.ReadingStatusResponse getReadingStatusForUser(String sessionId, String userId,
                                                                          boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            // Get user info from session
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus = new ArrayList<>();

            switch (userCategory) {
                case "Admin":
                    areaReadingStatus = getAllAreasReadingStatus(includeCustomerDetails, includeReadingDetails);
                    break;
                case "Region User":
                    areaReadingStatus = getRegionReadingStatus(userInfo.getRegionCode(), includeCustomerDetails, includeReadingDetails);
                    break;
                case "Province User":
                    areaReadingStatus = getProvinceReadingStatus(userInfo.getProvinceCode(), includeCustomerDetails, includeReadingDetails);
                    break;
                case "Area User":
                    areaReadingStatus = getAreaReadingStatus(userInfo.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    break;
                default:
                    return createErrorResponse("Unknown user category: " + userCategory);
            }

            // Create summary
            ReadingStatusDTO.ReadingStatusSummaryDTO summary = createSummary(areaReadingStatus);

            return createSuccessResponse(userCategory, areaReadingStatus, summary);

        } catch (Exception e) {
            System.err.println("ERROR in getReadingStatusForUser: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Failed to retrieve reading status: " + e.getMessage());
        }
    }

    /**
     * Get reading status for a specific area - FIXED VERSION with data type handling
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAreaReadingStatus(String areaCode,
                                                                            boolean includeCustomerDetails, boolean includeReadingDetails) {
        List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();

        try {
            // Add null check
            if (areaCode == null || areaCode.trim().isEmpty()) {
                throw new RuntimeException("Area code is required");
            }

            // Clean area code
            String cleanAreaCode = areaCode.trim();

            // Get area details
            Optional<HsbAreaDTO> areaOpt = locationService.getAreaByCode(cleanAreaCode);
            if (!areaOpt.isPresent()) {
                System.err.println("ERROR: Area not found: " + cleanAreaCode);
                throw new RuntimeException("Area not found: " + cleanAreaCode);
            }

            HsbAreaDTO area = areaOpt.get();

            // Get active bill cycle for the area
            Optional<Integer> activeBillCycleOpt = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCodeTrimmed(cleanAreaCode);

            if (!activeBillCycleOpt.isPresent()) {
                System.err.println("ERROR: No active bill cycle found for area: " + cleanAreaCode);
                // Area has no active bill cycle - all customers are considered pending
                ReadingStatusDTO.AreaReadingStatusDTO areaStatus = createAreaStatusWithoutActiveBillCycle(area, includeCustomerDetails);
                result.add(areaStatus);
                return result;
            }

            Integer activeBillCycle = activeBillCycleOpt.get();
            // Convert integer bill cycle to string for comparison with added_blcy
            String activeBillCycleStr = activeBillCycle.toString();

            System.out.println("DEBUG: Processing area " + cleanAreaCode + " with active bill cycle " + activeBillCycleStr);

            // Get all customers for the area
            List<BulkCustomer> allCustomers = bulkCustomerRepository.findByAreaCdTrimmed(cleanAreaCode);
            System.out.println("DEBUG: Found " + allCustomers.size() + " customers for area " + cleanAreaCode);

            if (allCustomers.isEmpty()) {
                // No customers in this area
                ReadingStatusDTO.AreaReadingStatusDTO areaStatus = createEmptyAreaStatus(area, activeBillCycle);
                result.add(areaStatus);
                return result;
            }

            // Get all temp readings for the area's active bill cycle
            // Use native query to handle string comparison properly
            List<TmpReadings> areaReadings = tmpReadingsRepository.findByAreaCdAndBillCycle(cleanAreaCode, activeBillCycleStr);
            System.out.println("DEBUG: Found " + areaReadings.size() + " readings for area " + cleanAreaCode + " and bill cycle " + activeBillCycleStr);

            // Create a set of account numbers that have readings for the active bill cycle
            Set<String> accountsWithReadings = areaReadings.stream()
                    .filter(reading -> reading != null && reading.getAccNbr() != null)
                    .map(TmpReadings::getAccNbr)
                    .collect(Collectors.toSet());

            // Separate customers into two groups
            List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithReadings = new ArrayList<>();
            List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithoutReadings = new ArrayList<>();

            for (BulkCustomer customer : allCustomers) {
                boolean hasReading = customer.getAccNbr() != null && accountsWithReadings.contains(customer.getAccNbr());

                ReadingStatusDTO.CustomerReadingStatusDTO customerStatus = convertToCustomerStatusDTO(
                        customer, hasReading, includeCustomerDetails, includeReadingDetails);

                if (includeReadingDetails && hasReading) {
                    // Get specific readings for this customer
                    List<TmpReadings> customerReadings = areaReadings.stream()
                            .filter(reading -> reading != null && reading.getAccNbr() != null &&
                                    customer.getAccNbr() != null &&
                                    reading.getAccNbr().equals(customer.getAccNbr()))
                            .collect(Collectors.toList());

                    List<TmpReadingsDTO> readingDTOs = customerReadings.stream()
                            .map(this::convertTmpReadingToDTO)
                            .collect(Collectors.toList());

                    customerStatus.setReadings(readingDTOs);
                    customerStatus.setReadingCount(readingDTOs.size());
                }

                if (hasReading) {
                    customersWithReadings.add(customerStatus);
                } else {
                    customersWithoutReadings.add(customerStatus);
                }
            }

            System.out.println("DEBUG: Customers with readings: " + customersWithReadings.size() +
                    ", without readings: " + customersWithoutReadings.size());

            // Create area status
            ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
            areaStatus.setAreaCode(area.getAreaCode());
            areaStatus.setAreaName(area.getAreaName());
            areaStatus.setProvinceCode(area.getProvCode());
            areaStatus.setProvinceName(area.getProvinceName());
            areaStatus.setRegionCode(area.getRegion());
            areaStatus.setActiveBillCycle(activeBillCycle);
            areaStatus.setTotalCustomers(allCustomers.size());
            areaStatus.setCustomersWithReadings(customersWithReadings.size());
            areaStatus.setCustomersWithoutReadings(customersWithoutReadings.size());

            // Calculate percentage
            double percentage = allCustomers.size() > 0 ?
                    (double) customersWithReadings.size() / allCustomers.size() * 100.0 : 0.0;
            areaStatus.setReadingPercentage(Math.round(percentage * 100.0) / 100.0);

            if (includeCustomerDetails) {
                areaStatus.setCustomersWithReadingsList(customersWithReadings);
                areaStatus.setCustomersWithoutReadingsList(customersWithoutReadings);
            }

            result.add(areaStatus);

        } catch (Exception e) {
            System.err.println("ERROR in getAreaReadingStatus for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for area " + areaCode + ": " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Get reading status for all areas in a province
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getProvinceReadingStatus(String provinceCode,
                                                                                boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            // Get areas by province
            List<HsbAreaDTO> areas = locationService.getAreasByProvince(provinceCode);

            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : areas) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(
                            area.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    result.addAll(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + " in province " + provinceCode + ": " + e.getMessage());
                    // Continue with other areas
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getProvinceReadingStatus for province " + provinceCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for province " + provinceCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get reading status for all areas in a region
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getRegionReadingStatus(String regionCode,
                                                                              boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            // Get areas by region
            List<HsbAreaDTO> areas = locationService.getAreasByRegion(regionCode);

            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : areas) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(
                            area.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    result.addAll(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + " in region " + regionCode + ": " + e.getMessage());
                    // Continue with other areas
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getRegionReadingStatus for region " + regionCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for region " + regionCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get reading status for all areas (Admin view)
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAllAreasReadingStatus(
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            List<HsbAreaDTO> allAreas = locationService.getAllAreas();

            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : allAreas) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(
                            area.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    result.addAll(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + ": " + e.getMessage());
                    // Continue with other areas
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getAllAreasReadingStatus: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for all areas: " + e.getMessage(), e);
        }
    }

    /**
     * Get pending readings (customers without readings) for an area
     */
    public ReadingStatusDTO.PendingReadingsDTO getPendingReadingsForArea(String areaCode) {
        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(areaCode, true, false);

            if (areaStatus.isEmpty()) {
                return new ReadingStatusDTO.PendingReadingsDTO();
            }

            ReadingStatusDTO.AreaReadingStatusDTO status = areaStatus.get(0);

            ReadingStatusDTO.PendingReadingsDTO pendingReadings = new ReadingStatusDTO.PendingReadingsDTO();
            pendingReadings.setAreaCode(status.getAreaCode());
            pendingReadings.setAreaName(status.getAreaName());
            pendingReadings.setActiveBillCycle(status.getActiveBillCycle());
            pendingReadings.setPendingCustomers(status.getCustomersWithoutReadingsList());
            pendingReadings.setPendingCount(status.getCustomersWithoutReadings());

            return pendingReadings;

        } catch (Exception e) {
            System.err.println("ERROR in getPendingReadingsForArea for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get pending readings for area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get completed readings (customers with readings) for an area
     */
    public ReadingStatusDTO.CompletedReadingsDTO getCompletedReadingsForArea(String areaCode) {
        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(areaCode, true, true);

            if (areaStatus.isEmpty()) {
                return new ReadingStatusDTO.CompletedReadingsDTO();
            }

            ReadingStatusDTO.AreaReadingStatusDTO status = areaStatus.get(0);

            ReadingStatusDTO.CompletedReadingsDTO completedReadings = new ReadingStatusDTO.CompletedReadingsDTO();
            completedReadings.setAreaCode(status.getAreaCode());
            completedReadings.setAreaName(status.getAreaName());
            completedReadings.setActiveBillCycle(status.getActiveBillCycle());
            completedReadings.setCompletedCustomers(status.getCustomersWithReadingsList());
            completedReadings.setCompletedCount(status.getCustomersWithReadings());

            return completedReadings;

        } catch (Exception e) {
            System.err.println("ERROR in getCompletedReadingsForArea for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get completed readings for area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    // Helper methods
    private ReadingStatusDTO.CustomerReadingStatusDTO convertToCustomerStatusDTO(BulkCustomer customer,
                                                                                 boolean hasReading, boolean includeDetails, boolean includeReadingDetails) {
        ReadingStatusDTO.CustomerReadingStatusDTO dto = new ReadingStatusDTO.CustomerReadingStatusDTO();

        dto.setAccNbr(customer.getAccNbr());
        dto.setHasReading(hasReading);

        if (includeDetails) {
            dto.setName(customer.getName());
            dto.setAddressL1(customer.getAddressL1());
            dto.setAreaCd(customer.getAreaCd());
            dto.setBillCycle(customer.getBillCycle());
            dto.setTariff(customer.getTariff());
            dto.setMobileNo(customer.getMobileNo());
            dto.setTelNbr(customer.getTelNbr());
        }

        if (!includeReadingDetails) {
            dto.setReadings(new ArrayList<>());
            dto.setReadingCount(0);
        }

        return dto;
    }

    private TmpReadingsDTO convertTmpReadingToDTO(TmpReadings reading) {
        TmpReadingsDTO dto = new TmpReadingsDTO();
        dto.setAccNbr(reading.getAccNbr());
        dto.setInstId(reading.getInstId());
        dto.setAreaCd(reading.getAreaCd());
        dto.setAddedBlcy(reading.getAddedBlcy());
        dto.setMtrSeq(reading.getMtrSeq());
        dto.setMtrType(reading.getMtrType());
        dto.setPrvDate(reading.getPrvDate());
        dto.setRdngDate(reading.getRdngDate());
        dto.setPrsntRdn(reading.getPrsntRdn());
        dto.setPrvRdn(reading.getPrvRdn());
        dto.setMtrNbr(reading.getMtrNbr());
        dto.setUnits(reading.getUnits());
        dto.setRate(reading.getRate());
        dto.setComputedChg(reading.getComputedChg());
        dto.setMntChg(reading.getMntChg());
        dto.setAcode(reading.getAcode());
        dto.setMFactor(reading.getMFactor());
        dto.setBillStat(reading.getBillStat());
        dto.setErrStat(reading.getErrStat());
        dto.setMtrStat(reading.getMtrStat());
        dto.setRdnStat(reading.getRdnStat());
        dto.setUserId(reading.getUserId());
        dto.setEnteredDtime(reading.getEnteredDtime());
        dto.setEditedUserId(reading.getEditedUserId());
        dto.setEditedDtime(reading.getEditedDtime());
        return dto;
    }

    private ReadingStatusDTO.AreaReadingStatusDTO createAreaStatusWithoutActiveBillCycle(HsbAreaDTO area,
                                                                                         boolean includeCustomerDetails) {
        // Get all customers for this area
        List<BulkCustomer> customers = bulkCustomerRepository.findByAreaCdTrimmed(area.getAreaCode());

        ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
        areaStatus.setAreaCode(area.getAreaCode());
        areaStatus.setAreaName(area.getAreaName());
        areaStatus.setProvinceCode(area.getProvCode());
        areaStatus.setProvinceName(area.getProvinceName());
        areaStatus.setRegionCode(area.getRegion());
        areaStatus.setActiveBillCycle(null);
        areaStatus.setTotalCustomers(customers.size());
        areaStatus.setCustomersWithReadings(0);
        areaStatus.setCustomersWithoutReadings(customers.size());
        areaStatus.setReadingPercentage(0.0);

        if (includeCustomerDetails) {
            List<ReadingStatusDTO.CustomerReadingStatusDTO> customerList = customers.stream()
                    .map(customer -> convertToCustomerStatusDTO(customer, false, true, false))
                    .collect(Collectors.toList());

            areaStatus.setCustomersWithReadingsList(new ArrayList<>());
            areaStatus.setCustomersWithoutReadingsList(customerList);
        }

        return areaStatus;
    }

    private ReadingStatusDTO.AreaReadingStatusDTO createEmptyAreaStatus(HsbAreaDTO area, Integer activeBillCycle) {
        ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
        areaStatus.setAreaCode(area.getAreaCode());
        areaStatus.setAreaName(area.getAreaName());
        areaStatus.setProvinceCode(area.getProvCode());
        areaStatus.setProvinceName(area.getProvinceName());
        areaStatus.setRegionCode(area.getRegion());
        areaStatus.setActiveBillCycle(activeBillCycle);
        areaStatus.setTotalCustomers(0);
        areaStatus.setCustomersWithReadings(0);
        areaStatus.setCustomersWithoutReadings(0);
        areaStatus.setReadingPercentage(0.0);
        areaStatus.setCustomersWithReadingsList(new ArrayList<>());
        areaStatus.setCustomersWithoutReadingsList(new ArrayList<>());

        return areaStatus;
    }

    private ReadingStatusDTO.ReadingStatusSummaryDTO createSummary(List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus) {
        int totalAreas = areaReadingStatus.size();
        int totalCustomers = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getTotalCustomers).sum();
        int totalCustomersWithReadings = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithReadings).sum();
        int totalCustomersWithoutReadings = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithoutReadings).sum();

        double overallPercentage = totalCustomers > 0 ? (double) totalCustomersWithReadings / totalCustomers * 100.0 : 0.0;

        int areasWithActiveBillCycles = (int) areaReadingStatus.stream()
                .filter(area -> area.getActiveBillCycle() != null)
                .count();

        ReadingStatusDTO.ReadingStatusSummaryDTO summary = new ReadingStatusDTO.ReadingStatusSummaryDTO();
        summary.setTotalAreas(totalAreas);
        summary.setTotalCustomers(totalCustomers);
        summary.setTotalCustomersWithReadings(totalCustomersWithReadings);
        summary.setTotalCustomersWithoutReadings(totalCustomersWithoutReadings);
        summary.setOverallReadingPercentage(Math.round(overallPercentage * 100.0) / 100.0);
        summary.setAreasWithActiveBillCycles(areasWithActiveBillCycles);
        summary.setAreasWithoutActiveBillCycles(totalAreas - areasWithActiveBillCycles);

        return summary;
    }

    private ReadingStatusDTO.ReadingStatusResponse createSuccessResponse(String userCategory,
                                                                         List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus,
                                                                         ReadingStatusDTO.ReadingStatusSummaryDTO summary) {
        ReadingStatusDTO.ReadingStatusResponse response = new ReadingStatusDTO.ReadingStatusResponse();
        response.setSuccess(true);
        response.setMessage("Reading status retrieved successfully");
        response.setUserCategory(userCategory);
        response.setAreaReadingStatus(areaReadingStatus);
        response.setSummary(summary);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private ReadingStatusDTO.ReadingStatusResponse createErrorResponse(String message) {
        ReadingStatusDTO.ReadingStatusResponse response = new ReadingStatusDTO.ReadingStatusResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setAreaReadingStatus(new ArrayList<>());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}