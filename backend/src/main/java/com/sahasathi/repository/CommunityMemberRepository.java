package com.sahasathi.repository;

import com.sahasathi.model.CommunityMember;
import com.sahasathi.model.CommunityRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {

    List<CommunityMember> findByCommunityIdAndStatus(Long communityId, String status);

    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);

    long countByCommunityIdAndStatus(Long communityId, String status);

    boolean existsByCommunityIdAndUserIdAndStatus(Long communityId, Long userId, String status);

    List<CommunityMember> findByUserIdAndStatusOrderByJoinedAtDesc(Long userId, String status);

    long countByUserIdAndStatus(Long userId, String status);

    boolean existsByCommunityIdAndUserIdAndRole(Long communityId, Long userId, CommunityRole role);
}
