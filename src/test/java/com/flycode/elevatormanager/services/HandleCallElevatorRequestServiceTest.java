package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.CallElevatorRequest;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-integration-tests.properties")
class HandleCallElevatorRequestServiceTest {
    @InjectMocks
    HandleCallElevatorRequestService inTesting;

    @Autowired
    Environment environment;

    @Mock
    ElevatorRepository elevatorRepositoryMock;

    @Mock
    RabbitTemplate rabbitTemplateMock;

    @Value("${elevator-configs.floor-count}")
    Integer floorCount;

    @BeforeEach
    void initMocks() {
        ReflectionTestUtils.setField(inTesting, "environment", environment);
        ReflectionTestUtils.setField(inTesting, "floorCount", floorCount);
    }

    @DisplayName("Returns error when floor requested is out of bounds.")
    @Test
    void checkFloorOutOfBounds() throws ExecutionException, InterruptedException {
        // Arrange
        CallElevatorRequest callElevatorRequest = new CallElevatorRequest(1L, 20);

        // Act
        var response = inTesting.execute(callElevatorRequest).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Floor is out of bounds");
    }

    @DisplayName("Returns error elevator for elevator tag does not exists.")
    @Test
    void checkElevatorExists() throws ExecutionException, InterruptedException {
        // Arrange
        CallElevatorRequest callElevatorRequest = new CallElevatorRequest(1L, 5);
        when(elevatorRepositoryMock.findByElevatorTag(anyLong())).thenReturn(Optional.empty());

        // Act
        var response = inTesting.execute(callElevatorRequest).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Elevator does not exists");
    }

    @DisplayName("Accepts elevator call when floor in bounds and not on same floor")
    @Test
    void acceptsRequestForCallElevator() throws ExecutionException, InterruptedException {
        // Arrange
        CallElevatorRequest callElevatorRequest = new CallElevatorRequest(1L, 5);
        var elevator = new Elevator(1L, 1L, 0, Constants.ElevatorStates.STATIONARY, Constants.ElevatorDirection.NONE, Constants.DoorStates.OPEN);
        when(elevatorRepositoryMock.findByElevatorTag(anyLong()))
                .thenReturn(Optional.of(elevator));

        // Act
        var response = inTesting.execute(callElevatorRequest).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(rabbitTemplateMock).convertAndSend(
                anyString(),
                eq(Constants.ELEVATOR_QUEUE_PREFIX + elevator.getElevatorTag() + ".routing-key"),
                any(Task.class)
        );
    }

    @DisplayName("Refuses elevator call on same floor")
    @Test
    void refusesCallOnSameFloor() throws ExecutionException, InterruptedException {
        // Arrange
        CallElevatorRequest callElevatorRequest = new CallElevatorRequest(1L, 5);
        var elevator = new Elevator(1L, 1L, 5, Constants.ElevatorStates.STATIONARY, Constants.ElevatorDirection.NONE, Constants.DoorStates.OPEN);
        when(elevatorRepositoryMock.findByElevatorTag(anyLong()))
                .thenReturn(Optional.of(elevator));

        // Act
        var response = inTesting.execute(callElevatorRequest).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Elevator already on same floor");
    }


}