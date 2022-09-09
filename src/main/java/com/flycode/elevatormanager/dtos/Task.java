package com.flycode.elevatormanager.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Serializable {
    private Long elevatorID;
    private Integer floorTo;
}
