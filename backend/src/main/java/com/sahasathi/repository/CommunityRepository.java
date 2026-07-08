package com.sahasathi.repository;

import com.sahasathi.model.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    @Query("""
            SELECT c FROM Community c
            WHERE (:city IS NULL OR c.city = :city)
            AND (:locality IS NULL OR c.locality = :locality)
            ORDER BY c.createdAt DESC
            """)
    Page<Community> findCommunities(
            @Param("city") String city,
            @Param("locality") String locality,
            Pageable pageable);

    Page<Community> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT c FROM Community c
            WHERE (:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)
            AND (:city IS NULL OR c.city = :city)
            AND (:locality IS NULL OR c.locality = :locality)
            ORDER BY c.createdAt DESC
            """)
    Page<Community> searchCommunities(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("locality") String locality,
            Pageable pageable);
}
