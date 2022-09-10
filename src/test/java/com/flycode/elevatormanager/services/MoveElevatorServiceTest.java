package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-integration-tests.properties")
class MoveElevatorServiceTest {
    @InjectMocks
    MoveElevatorService inTesting;

    @Autowired
    Environment environment;

    @Value("${elevator-configs.elevators-time-per-floor}")
    Integer elevatorsTimePerFloor;

    @Value("${elevator-configs.doors-time-per-action}")
    Integer doorsTimePerAction;

    @Mock
    PublishElevatorMovementsService publishElevatorMovementService;

    @Mock
    ElevatorRepository elevatorRepositoryMock;

    @BeforeEach
    void initMocks() {
        ReflectionTestUtils.setField(inTesting, "elevatorsTimePerFloor", elevatorsTimePerFloor);
        ReflectionTestUtils.setField(inTesting, "doorsTimePerAction", doorsTimePerAction);
    }

    @DisplayName("Moves elevator to specified floor at exact timing")
    @Test
    void movesElevatorToFloor() throws ExecutionException, InterruptedException {
        // Arrange
        AtomicLong startTime = new AtomicLong(0);
        Task task = new Task(1L, 1);

        var elevator = new Elevator(1L, 1L, 0, Constants.ElevatorStates.STATIONARY, Constants.ElevatorDirection.NONE, Constants.DoorStates.CLOSED);
        when(elevatorRepositoryMock.findByElevatorTag(anyLong()))
                .thenReturn(Optional.of(elevator));

        // set start time on first invocation of publishElevatorMovementService
        doAnswer((Answer<Void>) invocation -> {
            if(startTime.get() == 0) {
                startTime.set(System.currentTimeMillis());
            }
            return null;
        }).when(publishElevatorMovementService).execute(any(Elevator.class));

        var floorsToMove = Math.abs(task.getFloorTo() - 0);
        var expectedTimeToMove = (elevatorsTimePerFloor * floorsToMove) + doorsTimePerAction;

        // Act
        inTesting.execute(task).get();
        var totalTime = (System.currentTimeMillis() - startTime.get()) / 1000;

        // Assert
        var errorDiff = 2;
        assert (totalTime >= expectedTimeToMove - errorDiff) && (totalTime <= expectedTimeToMove + errorDiff);
        verify(publishElevatorMovementService, times(10)).execute(any(Elevator.class));
    }

    @DisplayName("Returns error elevator for elevator tag does not exists.")
    @Test
    void checkElevatorExists() throws ExecutionException, InterruptedException {
        // Arrange
        Task task = new Task(1L, 1);
        when(elevatorRepositoryMock.findByElevatorTag(anyLong())).thenReturn(Optional.empty());

        // Act
        inTesting.execute(task).get();

        // Assert
        verifyNoInteractions(publishElevatorMovementService);
    }

}