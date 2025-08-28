package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.UserAccSecInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccSecInfoRepository extends JpaRepository<UserAccSecInfo, String> {
    
    // Find by username
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userName = :userName")
    Optional<UserAccSecInfo> findByUserName(@Param("userName") String userName);
    
    // Find by user category
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userCat = :userCat")
    List<UserAccSecInfo> findByUserCat(@Param("userCat") String userCat);
    
    // Find by region code
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.regionCode = :regionCode")
    List<UserAccSecInfo> findByRegionCode(@Param("regionCode") String regionCode);
    
    // Find by province code
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.provinceCode = :provinceCode")
    List<UserAccSecInfo> findByProvinceCode(@Param("provinceCode") String provinceCode);
    
    // Find by area code
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.areaCode = :areaCode")
    List<UserAccSecInfo> findByAreaCode(@Param("areaCode") String areaCode);
    
    // Find by region and province
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.regionCode = :regionCode AND u.provinceCode = :provinceCode")
    List<UserAccSecInfo> findByRegionAndProvince(@Param("regionCode") String regionCode, 
                                                 @Param("provinceCode") String provinceCode);
    
    // Find by region, province, and area
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.regionCode = :regionCode AND u.provinceCode = :provinceCode AND u.areaCode = :areaCode")
    List<UserAccSecInfo> findByRegionProvinceAndArea(@Param("regionCode") String regionCode, 
                                                     @Param("provinceCode") String provinceCode,
                                                     @Param("areaCode") String areaCode);
    
    // Check if user exists by userId
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccSecInfo u WHERE u.userId = :userId")
    boolean existsByUserId(@Param("userId") String userId);
    
    // Check if user exists by userName
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccSecInfo u WHERE u.userName = :userName")
    boolean existsByUserName(@Param("userName") String userName);
    
    // Custom query for login validation
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userId = :userId AND u.passwd = :passwd")
    Optional<UserAccSecInfo> findByUserIdAndPasswd(@Param("userId") String userId, @Param("passwd") String passwd);
    
    // Explicit insert method for debugging
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO testdbnew.sec_info (user_id, user_name, passwd, user_cat, region_code, province_code, area_code) VALUES (:userId, :userName, :passwd, :userCat, :regionCode, :provinceCode, :areaCode)", nativeQuery = true)
    void insertUserManually(@Param("userId") String userId, 
                           @Param("userName") String userName, 
                           @Param("passwd") String passwd, 
                           @Param("userCat") String userCat,
                           @Param("regionCode") String regionCode,
                           @Param("provinceCode") String provinceCode,
                           @Param("areaCode") String areaCode);
    
    // Explicit delete method for debugging
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM testdbnew.sec_info WHERE user_id = :userId", nativeQuery = true)
    void deleteUserManually(@Param("userId") String userId);
}