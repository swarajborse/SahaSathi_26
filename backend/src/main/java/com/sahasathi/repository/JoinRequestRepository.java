package com.sahasathi.repository;

import com.sahasathi.model.JoinRequest;
import com.sahasathi.model.RequestStatus;
import com.sahasathi.model.RequestTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    List<JoinRequest> findByTargetTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
            RequestTargetType targetType, Long targetId, RequestStatus status);

    Optional<JoinRequest> findByTargetTypeAndTargetIdAndRequesterIdAndStatus(
            RequestTargetType targetType, Long targetId, Long requesterId, RequestStatus status);

    boolean existsByTargetTypeAndTargetIdAndRequesterIdAndStatus(
            RequestTargetType targetType, Long targetId, Long requesterId, RequestStatus status);

    List<JoinRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<JoinRequest> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            RequestTargetType targetType, Long targetId);
}
