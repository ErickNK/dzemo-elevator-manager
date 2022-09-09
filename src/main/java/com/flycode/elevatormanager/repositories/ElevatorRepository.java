package com.flycode.elevatormanager.repositories;

import com.flycode.elevatormanager.models.Elevator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ElevatorRepository extends JpaRepository<Elevator, Long> {
}
