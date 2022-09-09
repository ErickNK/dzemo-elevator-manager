package com.flycode.elevatormanager.controllers;

import com.flycode.elevatormanager.dtos.CallElevatorRequest;
import com.flycode.elevatormanager.dtos.Response;
import com.flycode.elevatormanager.services.GetElevatorMovementStream;
import com.flycode.elevatormanager.services.HandleCallElevatorRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.concurrent.CompletableFuture;

@RequestMapping("/api")
@RestController
public class MainController {

    @Autowired
    HandleCallElevatorRequestService handleCallElevatorRequestService;

    @Autowired
    GetElevatorMovementStream getElevatorMovementStream;

    @PostMapping("/v1/call-elevator")
    public CompletableFuture<Response<Boolean>> callElevatorRequest(
            @RequestBody CallElevatorRequest callElevatorRequest
    ) {
        return handleCallElevatorRequestService.execute(callElevatorRequest);
    }

    @GetMapping("/v1/elevator-movement-stream")
    public ResponseEntity<StreamingResponseBody> getStream(
            @RequestParam("elevatorId") Long elevatorId
    ) {
        return new ResponseEntity<>(getElevatorMovementStream.execute(elevatorId), HttpStatus.OK);
    }
}
