package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.BillCycleConfig;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillCycleService {

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    private static final Integer DEFAULT_BILL_CYCLE = 0; // Default value when no bill cycle exists

    /**
     * Get bill cycles based on user's access level
     */
    public BillCycleDTO.BillCycleResponse getBillCyclesForUser(String sessionId, String userId) {
        try {
            // Get user info from session
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();
            List<BillCycleDTO.AreaBillCycleDTO> billCycles = new ArrayList<>();

            switch (userCategory) {
                case "Admin":
                    billCycles = getAllAreasBillCycles();
                    break;
                case "Region User":
                    billCycles = getRegionBillCycles(userInfo.getRegionCode());
                    break;
                case "Province User":
                case "Accountant Revenue":
                case "Acc Assistance":
                case "Accountant Clark":
                    billCycles = getProvinceBillCycles(userInfo.getProvinceCode());
                    break;
                case "Area User":
                    billCycles = getAreaBillCycles(userInfo.getAreaCode());
                    break;
                default:
                    return createErrorResponse("Unknown user category: " + userCategory);
            }

            return createSuccessResponse(userCategory, billCycles);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve bill cycles: " + e.getMessage());
        }
    }

    /**
     * Get bill cycles for a specific area
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getAreaBillCycles(String areaCode) {
        List<BillCycleDTO.AreaBillCycleDTO> result = new ArrayList<>();
        
        try {
            // Get area details
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCode(areaCode);
            if (!areaOpt.isPresent()) {
                return result;
            }

            HsbArea area = areaOpt.get();
            
            // Get active bill cycle for the area
            Optional<Integer> activeBillCycle = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
            
            BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
            dto.setAreaCode(area.getAreaCode());
            dto.setAreaName(area.getAreaName());
            dto.setProvinceCode(area.getProvCode());
            dto.setRegionCode(area.getRegion());
            dto.setActiveBillCycle(activeBillCycle.orElse(DEFAULT_BILL_CYCLE));
            dto.setHasBillCycle(activeBillCycle.isPresent());
            
            // Get province name
            locationService.getProvinceByCode(area.getProvCode())
                .ifPresent(province -> dto.setProvinceName(province.getProvName()));
            
            result.add(dto);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get area bill cycles: " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Get bill cycles for all areas in a province
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getProvinceBillCycles(String provinceCode) {
        try {
            // Get all areas in the province
            List<HsbArea> areas = areaRepository.findByProvCode(provinceCode);
            
            if (areas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get area codes
            List<String> areaCodes = areas.stream()
                    .map(HsbArea::getAreaCode)
                    .collect(Collectors.toList());

            // Get active bill cycles for all areas
            Map<String, Integer> activeBillCycles = getActiveBillCyclesForAreas(areaCodes);

            // Get province name
            String provinceName = locationService.getProvinceByCode(provinceCode)
                    .map(p -> p.getProvName())
                    .orElse("");

            // Build result
            return areas.stream()
                    .map(area -> {
                        BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                        dto.setAreaCode(area.getAreaCode());
                        dto.setAreaName(area.getAreaName());
                        dto.setProvinceCode(area.getProvCode());
                        dto.setProvinceName(provinceName);
                        dto.setRegionCode(area.getRegion());
                        dto.setActiveBillCycle(activeBillCycles.getOrDefault(area.getAreaCode(), DEFAULT_BILL_CYCLE));
                        dto.setHasBillCycle(activeBillCycles.containsKey(area.getAreaCode()));
                        return dto;
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get province bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycles for all areas in a region
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getRegionBillCycles(String regionCode) {
        try {
            // Get all areas in the region
            List<HsbArea> areas = areaRepository.findByRegionCode(regionCode);
            
            if (areas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get area codes
            List<String> areaCodes = areas.stream()
                    .map(HsbArea::getAreaCode)
                    .collect(Collectors.toList());

            // Get active bill cycles with full details
            List<Object[]> billCycleData = billCycleConfigRepository.findActiveBillCyclesWithFullAreaDetails(areaCodes);
            
            // Create a map for quick lookup
            Map<String, BillCycleDTO.AreaBillCycleDTO> billCycleMap = new HashMap<>();
            for (Object[] data : billCycleData) {
                BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                dto.setAreaCode((String) data[0]);
                dto.setActiveBillCycle((Integer) data[1]);
                dto.setAreaName((String) data[2]);
                dto.setProvinceCode((String) data[3]);
                dto.setRegionCode((String) data[4]);
                dto.setProvinceName((String) data[5]);
                dto.setHasBillCycle(true);
                billCycleMap.put(dto.getAreaCode(), dto);
            }

            // Build complete result including areas without bill cycles
            return areas.stream()
                    .map(area -> {
                        if (billCycleMap.containsKey(area.getAreaCode())) {
                            return billCycleMap.get(area.getAreaCode());
                        } else {
                            // Area without bill cycle
                            BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                            dto.setAreaCode(area.getAreaCode());
                            dto.setAreaName(area.getAreaName());
                            dto.setProvinceCode(area.getProvCode());
                            dto.setRegionCode(area.getRegion());
                            dto.setActiveBillCycle(DEFAULT_BILL_CYCLE);
                            dto.setHasBillCycle(false);
                            
                            // Get province name
                            locationService.getProvinceByCode(area.getProvCode())
                                .ifPresent(province -> dto.setProvinceName(province.getProvName()));
                            
                            return dto;
                        }
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get region bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycles for all areas (Admin view)
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getAllAreasBillCycles() {
        try {
            // Get all areas
            List<HsbArea> allAreas = areaRepository.findAllOrderByAreaCode();
            
            if (allAreas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get all area codes
            List<String> areaCodes = allAreas.stream()
                    .map(HsbArea::getAreaCode)
                    .collect(Collectors.toList());

            // Get active bill cycles with full details
            List<Object[]> billCycleData = billCycleConfigRepository.findActiveBillCyclesWithFullAreaDetails(areaCodes);
            
            // Create a map for quick lookup
            Map<String, BillCycleDTO.AreaBillCycleDTO> billCycleMap = new HashMap<>();
            for (Object[] data : billCycleData) {
                BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                dto.setAreaCode((String) data[0]);
                dto.setActiveBillCycle((Integer) data[1]);
                dto.setAreaName((String) data[2]);
                dto.setProvinceCode((String) data[3]);
                dto.setRegionCode((String) data[4]);
                dto.setProvinceName((String) data[5]);
                dto.setHasBillCycle(true);
                billCycleMap.put(dto.getAreaCode(), dto);
            }

            // Build complete result including areas without bill cycles
            return allAreas.stream()
                    .map(area -> {
                        if (billCycleMap.containsKey(area.getAreaCode())) {
                            return billCycleMap.get(area.getAreaCode());
                        } else {
                            // Area without bill cycle
                            BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                            dto.setAreaCode(area.getAreaCode());
                            dto.setAreaName(area.getAreaName());
                            dto.setProvinceCode(area.getProvCode());
                            dto.setRegionCode(area.getRegion());
                            dto.setActiveBillCycle(DEFAULT_BILL_CYCLE);
                            dto.setHasBillCycle(false);
                            
                            // Get province name
                            locationService.getProvinceByCode(area.getProvCode())
                                .ifPresent(province -> dto.setProvinceName(province.getProvName()));
                            
                            return dto;
                        }
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getRegionCode)
                            .thenComparing(BillCycleDTO.AreaBillCycleDTO::getProvinceCode)
                            .thenComparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all areas bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycle summary for areas
     */
    public List<BillCycleDTO.BillCycleSummaryDTO> getBillCycleSummary(List<String> areaCodes) {
        try {
            List<Object[]> summaryData = billCycleConfigRepository.getBillCycleSummaryForAreas(areaCodes);
            Map<String, HsbArea> areaMap = areaRepository.findByAreaCodeIn(areaCodes).stream()
                    .collect(Collectors.toMap(HsbArea::getAreaCode, area -> area));

            return summaryData.stream()
                    .map(data -> {
                        String areaCode = (String) data[0];
                        Long totalCycles = (Long) data[1];
                        Long activeCycles = (Long) data[2];
                        Long inactiveCycles = (Long) data[3];

                        BillCycleDTO.BillCycleSummaryDTO summary = new BillCycleDTO.BillCycleSummaryDTO();
                        summary.setAreaCode(areaCode);
                        summary.setAreaName(areaMap.get(areaCode) != null ? areaMap.get(areaCode).getAreaName() : "");
                        summary.setTotalCycles(totalCycles.intValue());
                        summary.setActiveCycles(activeCycles.intValue());
                        summary.setInactiveCycles(inactiveCycles.intValue());

                        // Get active bill cycle
                        Optional<Integer> activeBillCycle = billCycleConfigRepository.findMaxActiveBillCycleNumberByAreaCode(areaCode);
                        summary.setActiveBillCycle(activeBillCycle.orElse(DEFAULT_BILL_CYCLE));

                        return summary;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get bill cycle summary: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user has access to view bill cycles for an area
     */
    public boolean hasAccessToArea(String sessionId, String userId, String targetAreaCode) {
        try {
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCode(targetAreaCode);
            if (!areaOpt.isPresent()) {
                return false;
            }

            HsbArea area = areaOpt.get();
            return sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);

        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods
    private Map<String, Integer> getActiveBillCyclesForAreas(List<String> areaCodes) {
        List<Object[]> results = billCycleConfigRepository.findMaxActiveBillCyclesByAreaCodes(areaCodes);
        Map<String, Integer> billCycleMap = new HashMap<>();
        for (Object[] result : results) {
            String areaCode = (String) result[0];
            Integer billCycle = (Integer) result[1];
            billCycleMap.put(areaCode, billCycle);
        }
        return billCycleMap;
    }

    private BillCycleDTO.BillCycleResponse createSuccessResponse(String userCategory, List<BillCycleDTO.AreaBillCycleDTO> billCycles) {
        BillCycleDTO.BillCycleResponse response = new BillCycleDTO.BillCycleResponse();
        response.setSuccess(true);
        response.setMessage("Bill cycles retrieved successfully");
        response.setUserCategory(userCategory);
        response.setBillCycles(billCycles);
        response.setTimestamp(java.time.LocalDateTime.now());
        return response;
    }

    private BillCycleDTO.BillCycleResponse createErrorResponse(String message) {
        BillCycleDTO.BillCycleResponse response = new BillCycleDTO.BillCycleResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setBillCycles(new ArrayList<>());
        response.setTimestamp(java.time.LocalDateTime.now());
        return response;
    }

    /**
     * Get all bill cycle configurations for an area
     */
    public List<BillCycleDTO.BillCycleConfigDTO> getAllBillCyclesForArea(String areaCode) {
        try {
            List<BillCycleConfig> configs = billCycleConfigRepository.findByAreaCode(areaCode);
            
            return configs.stream()
                    .map(this::convertToConfigDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bill cycle configurations: " + e.getMessage(), e);
        }
    }

    /**
     * Convert BillCycleConfig entity to DTO
     */
    private BillCycleDTO.BillCycleConfigDTO convertToConfigDTO(BillCycleConfig config) {
        BillCycleDTO.BillCycleConfigDTO dto = new BillCycleDTO.BillCycleConfigDTO();
        dto.setBillCycle(config.getBillCycle());
        dto.setAreaCode(config.getAreaCode());
        dto.setUserId(config.getUserId());
        dto.setEnteredDate(config.getEnteredDate());
        dto.setCycleStat(config.getCycleStat());
        dto.setIsActive(config.getCycleStat() != null && config.getCycleStat() == 1);
        return dto;
    }
}