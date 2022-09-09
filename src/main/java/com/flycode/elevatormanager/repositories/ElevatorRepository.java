package com.flycode.elevatormanager.repositories;

import com.flycode.elevatormanager.models.Elevator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ElevatorRepository extends JpaRepository<Elevator, Long> {
    Optional<Elevator> findByElevatorTag(Long elevatorTag);
    Boolean existsByElevatorTag(Long elevatorTag);
}
