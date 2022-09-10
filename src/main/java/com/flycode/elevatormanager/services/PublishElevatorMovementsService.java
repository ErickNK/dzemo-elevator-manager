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
