package com.flycode.elevatormanager.models;

import com.flycode.elevatormanager.listeners.ElevatorAuditTrailListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "elevators")
@Entity()
@EntityListeners({AuditingEntityListener.class, ElevatorAuditTrailListener.class})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Elevator implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="elevator_tag")
    private Long elevatorTag;

    @Column()
    private Integer floor;

    @Column()
    private String state;

    @Column()
    private String direction;

    @Column(name = "door_state")
    private String doorState;

    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedDate;
}
