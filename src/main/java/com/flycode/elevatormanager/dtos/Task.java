package com.flycode.elevatormanager.dtos;

import com.flycode.elevatormanager.configs.SecurityConfig;
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
