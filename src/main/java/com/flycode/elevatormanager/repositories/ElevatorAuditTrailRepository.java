package com.flycode.elevatormanager.repositories;

import com.flycode.elevatormanager.models.ElevatorAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElevatorAuditTrailRepository extends JpaRepository<ElevatorAuditTrail, Long> {
}
