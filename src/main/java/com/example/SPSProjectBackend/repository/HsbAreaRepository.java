package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.HsbArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HsbAreaRepository extends JpaRepository<HsbArea, String> {
    
    // Find area by code
    @Query("SELECT a FROM HsbArea a WHERE a.areaCode = :areaCode")
    Optional<HsbArea> findByAreaCode(@Param("areaCode") String areaCode);
    
    // Find areas by province code
    @Query("SELECT a FROM HsbArea a WHERE a.provCode = :provCode ORDER BY a.areaCode ASC")
    List<HsbArea> findByProvCode(@Param("provCode") String provCode);
    
    // Find areas by region code
    @Query("SELECT a FROM HsbArea a WHERE a.region = :regionCode ORDER BY a.areaCode ASC")
    List<HsbArea> findByRegionCode(@Param("regionCode") String regionCode);
    
    // Find areas by region and province
    @Query("SELECT a FROM HsbArea a WHERE a.region = :regionCode AND a.provCode = :provCode ORDER BY a.areaCode ASC")
    List<HsbArea> findByRegionAndProvince(@Param("regionCode") String regionCode, @Param("provCode") String provCode);
    
    // Find all areas ordered by area code
    @Query("SELECT a FROM HsbArea a ORDER BY a.areaCode ASC")
    List<HsbArea> findAllOrderByAreaCode();
    
    // Get all unique regions ordered by region code
    @Query("SELECT DISTINCT a.region FROM HsbArea a WHERE a.region IS NOT NULL ORDER BY a.region ASC")
    List<String> findAllRegions();
    
    // Check if area code exists
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM HsbArea a WHERE a.areaCode = :areaCode")
    boolean existsByAreaCode(@Param("areaCode") String areaCode);
    
    // Get areas with province names (JOIN query)
    @Query("SELECT a.areaCode, a.provCode, a.areaName, a.region, p.provName " +
           "FROM HsbArea a " +
           "JOIN HsbProvince p ON a.provCode = p.provCode " +
           "ORDER BY a.areaCode ASC")
    List<Object[]> findAreasWithProvinceNames();
    
    // Get areas with province names by region
    @Query("SELECT a.areaCode, a.provCode, a.areaName, a.region, p.provName " +
           "FROM HsbArea a " +
           "JOIN HsbProvince p ON a.provCode = p.provCode " +
           "WHERE a.region = :regionCode " +
           "ORDER BY a.areaCode ASC")
    List<Object[]> findAreasWithProvinceNamesByRegion(@Param("regionCode") String regionCode);
    
    // Get areas with province names by province code
    @Query("SELECT a.areaCode, a.provCode, a.areaName, a.region, p.provName " +
           "FROM HsbArea a " +
           "JOIN HsbProvince p ON a.provCode = p.provCode " +
           "WHERE a.provCode = :provCode " +
           "ORDER BY a.areaCode ASC")
    List<Object[]> findAreasWithProvinceNamesByProvince(@Param("provCode") String provCode);

       // Find areas by a list of area codes
       @Query("SELECT a FROM HsbArea a WHERE a.areaCode IN :areaCodes ORDER BY a.areaCode ASC")
       List<HsbArea> findByAreaCodeIn(@Param("areaCodes") List<String> areaCodes);
}