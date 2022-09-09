package com.flycode.elevatormanager.configs;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class DbConfig implements CommandLineRunner {
    @Autowired
    ElevatorRepository elevatorRepository;

    @Value("${elevator-configs.elevators-count}")
    Integer elevatorsCount;

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(OffsetDateTime.now(clock));
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Override
    public void run(String... args) throws Exception {
        // initialize elevator models if missing
        try {
            for (int i = 1; i <= elevatorsCount; i++) {
                if (!elevatorRepository.existsByElevatorTag((long) i)) {
                    var elevator = new Elevator();
                    elevator.setElevatorTag((long) i);
                    elevator.setFloor(0);
                    elevator.setState(Constants.ElevatorStates.STATIONARY);
                    elevator.setDirection(Constants.ElevatorDirection.NONE);
                    elevator.setDoorState(Constants.DoorStates.CLOSED);
                    elevatorRepository.save(elevator);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
