package com.flycode.elevatormanager.controllers;

import com.flycode.elevatormanager.dtos.CallElevatorRequest;
import com.flycode.elevatormanager.dtos.Response;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.services.GetAllElevatorsService;
import com.flycode.elevatormanager.services.HandleCallElevatorRequestService;
import com.flycode.elevatormanager.utils.LogHelper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequestMapping("/api")
@RestController
@Slf4j
public class MainController {

    @Autowired
    HandleCallElevatorRequestService handleCallElevatorRequestService;

    @Autowired
    GetAllElevatorsService getAllElevatorsService;


    @Operation(summary = "Call elevator to a specific floor.")
    @PostMapping("/v1/call-elevator")
    public CompletableFuture<Response<Boolean>> callElevatorRequest(
            @RequestBody CallElevatorRequest callElevatorRequest
    ) {
        LogHelper.builder(log)
                .logMsg("Handling call elevator request.")
                .logDetailedMsg("Floor :" + callElevatorRequest.getFloorNumber() + " Elevator :" + callElevatorRequest.getElevatorId())
                .info();

        return handleCallElevatorRequestService.execute(callElevatorRequest);
    }

    @Operation(summary = "List all elevators from database.")
    @GetMapping("/v1/elevators")
    public CompletableFuture<Response<List<Elevator>>> getAllElevators() {
        LogHelper.builder(log)
                .logMsg("Listing all elevators")
                .info();

        return getAllElevatorsService.execute();
    }
}
