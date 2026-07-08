package com.sahasathi.repository;

import com.sahasathi.model.Activity;
import com.sahasathi.model.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("""
            SELECT a FROM Activity a
            WHERE a.status != 'CANCELLED'
            AND a.city = :city
            AND (:locality IS NULL OR a.locality = :locality)
            AND (:category IS NULL OR a.category = :category)
            AND (:fromDate IS NULL OR a.dateTime >= :fromDate)
            ORDER BY a.dateTime ASC
            """)
    Page<Activity> findActivities(
            @Param("city") String city,
            @Param("locality") String locality,
            @Param("category") String category,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);

    Page<Activity> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByCreatedByIdAndStatus(Long userId, ActivityStatus status);

    @Query("""
            SELECT a FROM Activity a
            WHERE a.status != 'CANCELLED'
            AND (:keyword IS NULL OR a.title LIKE %:keyword% OR a.description LIKE %:keyword%)
            AND (:city IS NULL OR a.city = :city)
            AND (:locality IS NULL OR a.locality = :locality)
            AND (:category IS NULL OR a.category = :category)
            ORDER BY a.dateTime ASC
            """)
    Page<Activity> searchActivities(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("locality") String locality,
            @Param("category") String category,
            Pageable pageable);

    @Query("""
            SELECT a FROM Activity a
            WHERE a.status != 'CANCELLED'
            AND a.dateTime >= :start AND a.dateTime < :end
            ORDER BY a.dateTime ASC
            """)
    List<Activity> findActivitiesBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
