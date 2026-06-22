package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequestEntity, Long> {

    List<ChangeRequestEntity> findAllByOrderByCreatedAtDesc();

    List<ChangeRequestEntity> findByStatusOrderByCreatedAtDesc(ChangeRequestStatus status);

    List<ChangeRequestEntity> findByRequestedByIdOrderByCreatedAtDesc(Long requestedById);

    List<ChangeRequestEntity> findByLocationIdOrderByCreatedAtDesc(Long locationId);

    long countByStatus(ChangeRequestStatus status);
}
