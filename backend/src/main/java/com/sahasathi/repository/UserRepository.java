package com.sahasathi.repository;

import com.sahasathi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
            SELECT DISTINCT u FROM User u
            WHERE u.active = true
            AND u.id != :userId
            AND u.city = :city
            AND (:locality IS NULL OR u.locality = :locality)
            """)
    Page<User> findNearbyUsers(
            @Param("userId") Long userId,
            @Param("city") String city,
            @Param("locality") String locality,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT u FROM User u
            WHERE u.active = true
            AND u.id != :userId
            AND u.city = :city
            """)
    Page<User> findNearbyUsersByCity(
            @Param("userId") Long userId,
            @Param("city") String city,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT u FROM User u
            WHERE u.active = true
            AND (:keyword IS NULL OR u.name LIKE %:keyword%)
            AND (:city IS NULL OR u.city = :city)
            AND (:locality IS NULL OR u.locality = :locality)
            ORDER BY u.name ASC
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("locality") String locality,
            Pageable pageable);
}
