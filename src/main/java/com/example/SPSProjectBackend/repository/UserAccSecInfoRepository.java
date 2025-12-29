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
    
    // Find by EPF number
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.epfNum = :epfNum")
    Optional<UserAccSecInfo> findByEpfNum(@Param("epfNum") String epfNum);
    
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
    
    // Check if user exists by EPF number
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccSecInfo u WHERE u.epfNum = :epfNum")
    boolean existsByEpfNum(@Param("epfNum") String epfNum);
    
    // Custom query for login validation - only for active users
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userId = :userId AND u.passwd = :passwd AND u.status = 1")
    Optional<UserAccSecInfo> findByUserIdAndPasswd(@Param("userId") String userId, @Param("passwd") String passwd);
    
    // Find all active users
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.status = 1")
    List<UserAccSecInfo> findAllActiveUsers();
    
    // Find all inactive users
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.status = 0")
    List<UserAccSecInfo> findAllInactiveUsers();
    
    // Find users by status
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.status = :status")
    List<UserAccSecInfo> findByStatus(@Param("status") Integer status);
    
    // Update user status (toggle between active/inactive)
    @Modifying
    @Transactional
    @Query("UPDATE UserAccSecInfo u SET u.status = :status WHERE u.userId = :userId")
    void updateUserStatus(@Param("userId") String userId, @Param("status") Integer status);
    
    // Check if user is active
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccSecInfo u WHERE u.userId = :userId AND u.status = 1")
    boolean isUserActive(@Param("userId") String userId);

    // Enhanced login validation - explicitly for active users only
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userId = :userId AND u.status = 1")
    Optional<UserAccSecInfo> findActiveUserById(@Param("userId") String userId);

    // Find active user by username
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userName = :userName AND u.status = 1")
    Optional<UserAccSecInfo> findActiveUserByUserName(@Param("userName") String userName);

    // Validate login credentials for active users only
    @Query("SELECT u FROM UserAccSecInfo u WHERE u.userId = :userId AND u.passwd = :passwd AND u.status = 1")
    Optional<UserAccSecInfo> validateActiveUserLogin(@Param("userId") String userId, @Param("passwd") String passwd);

    // Check if user exists and is active
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccSecInfo u WHERE u.userId = :userId AND u.status = 1")
    boolean isUserActiveById(@Param("userId") String userId);
    
    // Explicit insert method for debugging (updated with status)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO testdbnew.sec_info (user_id, user_name, passwd, user_cat, region_code, province_code, area_code, epf_num, status) VALUES (:userId, :userName, :passwd, :userCat, :regionCode, :provinceCode, :areaCode, :epfNum, :status)", nativeQuery = true)
    void insertUserManually(@Param("userId") String userId, 
                           @Param("userName") String userName, 
                           @Param("passwd") String passwd, 
                           @Param("userCat") String userCat,
                           @Param("regionCode") String regionCode,
                           @Param("provinceCode") String provinceCode,
                           @Param("areaCode") String areaCode,
                           @Param("epfNum") String epfNum,
                           @Param("status") Integer status);
}