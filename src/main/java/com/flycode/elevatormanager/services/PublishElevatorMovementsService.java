package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.flycode.elevatormanager.utils.LogHelper;
import com.pusher.rest.Pusher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PublishElevatorMovementsService {
    @Autowired
    ElevatorRepository elevatorRepository;

    @Autowired
    Pusher pusher;

    /**
     * Publish elevator events. This function updates elevator with new position. Updating elevator triggers the ElevatorAuditTrailListener that
     * adds the position to ElevatorAuditTrail table. The function also publishes the event via Pusher that will be received by
     * frontend webpage via PusherJs
     *
     * @param elevator Elevator to publish events.
     * @return void
     */
    @Async
    public CompletableFuture<Void> execute(Elevator elevator) {
        try {
            elevatorRepository.save(elevator);

            var response = pusher.trigger(
                    Constants.ELEVATOR_PUSHER_CHANNEL,
                    Constants.ELEVATOR_QUEUE_PREFIX + elevator.getElevatorTag(),
                    elevator
            );

            LogHelper.builder(log)
                    .logMsg("Saved elevator movements")
                    .logDetailedMsg("Pusher response: " + response.getHttpStatus())
                    .info();

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            LogHelper.builder(log)
                    .logMsg("Saving elevator movements audit trail and pushing events failed")
                    .logDetailedMsg(e.getMessage())
                    .error();

            return CompletableFuture.completedFuture(null);
        }
    }
}
