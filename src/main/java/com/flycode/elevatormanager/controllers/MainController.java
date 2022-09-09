package com.flycode.elevatormanager.controllers;

import com.flycode.elevatormanager.dtos.CallElevatorRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class MainController {

    @PostMapping("/v1/call-elevator")
    public void callElevatorRequest(
            @RequestBody CallElevatorRequest callElevatorRequest
    ) {

    }
}
