package com.flycode.elevatormanager.services;

import com.flycode.elevatormanager.dtos.Response;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.flycode.elevatormanager.utils.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class GetAllElevatorsService {
    @Autowired
    ElevatorRepository elevatorRepository;

    /**
     * Fetch all elevators from database.
     *
     * @return List of all Elevators
     */
    @Async
    public CompletableFuture<Response<List<Elevator>>> execute() {
        try {
            return CompletableFuture.completedFuture(Response.successResponse(elevatorRepository.findAll()));
        } catch (Exception exception) {
            LogHelper.builder(log)
                    .logMsg("Error when fetching elevators")
                    .logDetailedMsg(exception.getMessage())
                    .error();

            Response<List<Elevator>> response = new Response<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    exception.getMessage()
            );
            return CompletableFuture.completedFuture(response);
        }
    }
}
