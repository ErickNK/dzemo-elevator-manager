package com.flycode.elevatormanager.listeners;

import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.services.MoveElevatorService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ElevatorCallListener {

    @Autowired
    MoveElevatorService moveElevatorService;

    public void receiveElevatorCallTask(Task task, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long tag) throws IOException {
        channel.basicQos(1, true);
        moveElevatorService.execute(task)
                .thenRun(() -> {
                    try {
                        channel.basicAck(tag, false);
                    } catch (IOException e) {
                        //
                    }
                })
                .exceptionally(throwable -> {
                    try {
                        channel.basicNack(tag, false, true);
                    } catch (IOException e) {
                        //
                    }

                    return null;
                });
    }
}
