package com.flycode.elevatormanager.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallElevatorRequest implements Serializable {
    private Long elevatorId;
    private Integer floorNumber;
}
