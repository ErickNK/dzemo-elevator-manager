package com.flycode.elevatormanager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flycode.elevatormanager.constants.Constants;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class GetElevatorMovementStream {

    @Autowired
    Pusher pusherClient;

    @Autowired
    ObjectMapper objectMapper;

    public StreamingResponseBody execute(Long elevatorId) {
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        pusherClient.connect();

        try {
            Channel channel = pusherClient.subscribe(Constants.ELEVATOR_PUSHER_CHANNEL);

            return out -> {
                channel.bind(Constants.ELEVATOR_QUEUE_PREFIX + elevatorId, event -> {
                    try {
                        out.write(event.getData().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        pusherClient.disconnect();
                    }
                });
            };
        } catch (Exception e) {
            pusherClient.disconnect();
            return null;
        }
    }
}
