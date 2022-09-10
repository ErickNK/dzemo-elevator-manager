package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.flycode.elevatormanager.utils.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MoveElevatorService {

    @Autowired
    ElevatorRepository elevatorRepository;

    @Autowired
    PublishElevatorMovementsService publishElevatorMovementService;

    @Value("${elevator-configs.elevators-time-per-floor}")
    Integer elevatorsTimePerFloor;

    @Value("${elevator-configs.doors-time-per-action}")
    Integer doorsTimePerAction;

    @Async
    public CompletableFuture<Void> execute(Task task) {
        try {
            var optionalElevator = elevatorRepository.findByElevatorTag(task.getElevatorID());
            if (optionalElevator.isEmpty()) {
                LogHelper.builder(log)
                        .logMsg("Elevator does not exits")
                        .info();

                return CompletableFuture.completedFuture(null);
            }
            var elevator = optionalElevator.get();

            var direction = elevator.getFloor() > task.getFloorTo()
                    ? Constants.ElevatorDirection.DOWN
                    : Constants.ElevatorDirection.UP;

            var floorsToMove = Math.abs(task.getFloorTo() - elevator.getFloor());
            var totalTimeToMove = (elevatorsTimePerFloor * floorsToMove) + doorsTimePerAction;

            var doorNotInitiallyClosed = false;
            if (elevator.getDoorState().equals(Constants.DoorStates.OPEN)) {
                totalTimeToMove += doorsTimePerAction;
                doorNotInitiallyClosed = true;
            }

            moveElevator(elevator, totalTimeToMove, doorNotInitiallyClosed, direction);

            LogHelper.builder(log)
                    .logMsg("Elevator moved to floor: " + task.getFloorTo())
                    .logDetailedMsg("Elevator: " + elevator.getElevatorTag())
                    .info();
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            LogHelper.builder(log)
                    .logMsg("Encountered error when moving elevator")
                    .logDetailedMsg(e.getMessage())
                    .info();

            return CompletableFuture.completedFuture(null);
        }
    }

    private void moveElevator(Elevator elevator, int totalTimeToMove, boolean doorNotInitiallyClosed, String direction) throws InterruptedException {
        var counter = 0;
        var floorCounter = 0;

        elevator.setDirection(direction);
        elevator.setState(Constants.ElevatorStates.MOVING);
        publishElevatorMovementService.execute(elevator);

        while (true) {
            Thread.sleep(1000);

            if (doorNotInitiallyClosed) { // on first two seconds
                if (counter < doorsTimePerAction) {
                    elevator.setState(Constants.ElevatorStates.DOOR_CLOSING);
                } else if (counter == doorsTimePerAction) {
                    elevator.setState(Constants.ElevatorStates.DOOR_CLOSED);
                    elevator.setDoorState(Constants.DoorStates.CLOSED);
                    doorNotInitiallyClosed = false;
                }
            } else if (totalTimeToMove - counter <= doorsTimePerAction) { // last two seconds
                if (totalTimeToMove - counter == 0) {
                    elevator.setState(Constants.ElevatorStates.DOOR_OPEN);
                    elevator.setDoorState(Constants.DoorStates.OPEN);
                } else {
                    elevator.setState(Constants.ElevatorStates.DOOR_OPENING);
                }
            } else {
                floorCounter+=1;
                if(floorCounter == elevatorsTimePerFloor){
                    var currentFloor = direction.equals(Constants.ElevatorDirection.UP)
                                    ? elevator.getFloor() + 1
                                    : elevator.getFloor() - 1;
                    elevator.setFloor(currentFloor);
                    floorCounter = 0;
                }
                elevator.setState(Constants.ElevatorStates.MOVING);
            }

            publishElevatorMovementService.execute(elevator);

            if (counter == totalTimeToMove) {
                break;
            }

            counter = counter + 1;
        }

        elevator.setState(Constants.ElevatorStates.STATIONARY);
        elevator.setDirection(Constants.ElevatorDirection.NONE);
        publishElevatorMovementService.execute(elevator);
    }

}
