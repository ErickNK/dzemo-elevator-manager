package com.flycode.elevatormanager.configs;

import com.flycode.elevatormanager.utils.converters.GsonLocalDateTime;
import com.flycode.elevatormanager.utils.converters.GsonOffsetDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Configuration
public class PusherConfig {

    @Autowired
    Environment environment;

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(
                environment.getRequiredProperty("pusher.app-id"),
                environment.getRequiredProperty("pusher.key"),
                environment.getRequiredProperty("pusher.secret")
        );
        pusher.setCluster(environment.getRequiredProperty("pusher.cluster"));
        pusher.setEncrypted(true);
        pusher.setRequestTimeout(10000);

        var builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTime());
        builder.registerTypeAdapter(OffsetDateTime.class, new GsonOffsetDateTime());
        pusher.setGsonSerialiser(builder.create());

        return pusher;
    }


//    @Bean("clientPusher")
//    public com.pusher.client.Pusher clientPusher() {
//        PusherOptions options = new PusherOptions()
//                .setCluster(environment.getRequiredProperty("pusher.cluster"));
//        com.pusher.client.Pusher clientPusher = new com.pusher.client.Pusher(
//                environment.getRequiredProperty("pusher.key"),
//                options
//        );
//
//        clientPusher.connect(new ConnectionEventListener() {
//            @Override
//            public void onConnectionStateChange(ConnectionStateChange change) {
//                System.out.println("State changed to " + change.getCurrentState() +
//                        " from " + change.getPreviousState());
//            }
//
//            @Override
//            public void onError(String message, String code, Exception e) {
//                System.out.println("There was a problem connecting!");
//            }
//        }, ConnectionState.ALL);
//
//        return clientPusher;
//    }
}
