package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishElevatorMovementsServiceTest {
    @InjectMocks
    PublishElevatorMovementsService inTesting;

    @Mock
    ElevatorRepository elevatorRepository;

    @Mock
    Pusher pusher;

    @DisplayName("Test that elevator movements are being saved on elevator model and published via Pusher")
    @Test()
    void publishesElevatorEvents() throws ExecutionException, InterruptedException {
        // Arrange
        var elevator = new Elevator(1L, 1L, 0, Constants.ElevatorStates.STATIONARY, Constants.ElevatorDirection.NONE, Constants.DoorStates.CLOSED);

        Result result = mock(Result.class);
        when(result.getHttpStatus()).thenReturn(200);

        when(pusher.trigger(anyString(), anyString(), any(Elevator.class)))
                .thenReturn(result);

        // Act
        inTesting.execute(elevator).get();

        // Assert
        verify(elevatorRepository).save(any(Elevator.class));
    }

}