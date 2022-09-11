package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.CallElevatorRequest;
import com.flycode.elevatormanager.dtos.Response;
import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.flycode.elevatormanager.utils.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HandleCallElevatorRequestService {

    @Autowired
    Environment environment;

    @Autowired
    ElevatorRepository elevatorRepository;

    @Value("${elevator-configs.floor-count}")
    Integer floorCount;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * Handles a call for an elevator. It checks whether the floor is valid, if the floor is the same as
     * the elevator is already on. Also checks that the elevator being called exists.
     *
     * @param callElevatorRequest CallElevatorRequest data.
     * @return Response with true value if a task has being sent for the call. If an error occurs returns a Response with an error message.
     */
    @Async
    public CompletableFuture<Response<Boolean>> execute(CallElevatorRequest callElevatorRequest) {

        try {
            // check movement possible
            if (callElevatorRequest.getFloorNumber() < 0 || callElevatorRequest.getFloorNumber() > (floorCount - 1)) {
                return CompletableFuture.completedFuture(Response.withBadRequestError("Floor is out of bounds"));
            }

            var optionalElevator = elevatorRepository.findByElevatorTag(callElevatorRequest.getElevatorId());
            if (optionalElevator.isEmpty()) {
                return CompletableFuture.completedFuture(Response.withBadRequestError("Elevator does not exists"));
            }
            var elevator = optionalElevator.get();

            //TODO: check for existing task

            // calling on same floor
            if (elevator.getFloor().equals(callElevatorRequest.getFloorNumber())) {
                // TODO: disrupt elevator if already on same floor
                return CompletableFuture.completedFuture(Response.withBadRequestError("Elevator already on same floor"));
            }

            // queue task
            Task task = new Task(elevator.getElevatorTag(), callElevatorRequest.getFloorNumber());
            rabbitTemplate.convertAndSend(
                    environment.getRequiredProperty("mq.main.exchange"),
                    Constants.ELEVATOR_QUEUE_PREFIX + elevator.getElevatorTag() + ".routing-key",
                    task
            );

            LogHelper.builder(log)
                    .logMsg("Elevator call queued.")
                    .error();

            Response<Boolean> response = new Response<>(
                    HttpStatus.OK.value(),
                    Boolean.TRUE,
                    null
            );
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            LogHelper.builder(log)
                    .logMsg("Error when handling elevator call request")
                    .logDetailedMsg(e.getMessage())
                    .error();

            Response<Boolean> response = new Response<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    e.getMessage()
            );
            return CompletableFuture.completedFuture(response);
        }
    }
}
