package com.flycode.elevatormanager.listeners;

import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.models.ElevatorAuditTrail;
import com.flycode.elevatormanager.repositories.ElevatorAuditTrailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.time.Clock;
import java.time.OffsetDateTime;

@Component
@Slf4j
public class ElevatorAuditTrailListener {

    private static Clock clock;
    private static ElevatorAuditTrailRepository elevatorAuditTrailRepository;

    @Autowired
    public void setClock(Clock clock) {
        ElevatorAuditTrailListener.clock = clock;
    }

    @Autowired
    public void setKafkaTemplate(ElevatorAuditTrailRepository elevatorAuditTrailRepository) {
        ElevatorAuditTrailListener.elevatorAuditTrailRepository = elevatorAuditTrailRepository;
    }

    @PostPersist
    @PostUpdate
    @PostRemove
    private void afterAnyUpdate(final Elevator session) {
            ElevatorAuditTrail auditTrail = getAuditTrailFromElevator(session);
            elevatorAuditTrailRepository.save(auditTrail);
    }

    private ElevatorAuditTrail getAuditTrailFromElevator(Elevator elevator) {
        ElevatorAuditTrail auditTrail = new ElevatorAuditTrail();
        auditTrail.setElevatorId(elevator.getElevatorTag());
        auditTrail.setFloor(elevator.getFloor());
        auditTrail.setState(elevator.getState());
        auditTrail.setDirection(elevator.getDirection());
        auditTrail.setCreatedDate(OffsetDateTime.now(clock));
        return auditTrail;
    }

}