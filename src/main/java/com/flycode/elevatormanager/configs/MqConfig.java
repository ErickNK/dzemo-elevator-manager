package com.flycode.elevatormanager.configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.listeners.ElevatorCallListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MqConfig implements RabbitListenerConfigurer {
    @Value("${mq.main.exchange}")
    String mainExchange;

    @Value("${elevator-configs.elevators-count}")
    Integer elevatorsCount;

    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    ElevatorCallListener elevatorCallListener;

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(mainExchange);
    }

    @Bean
    public List<String> queueNames() {
        List<String> queues = new ArrayList<>();
        for (int i = 1; i <= elevatorsCount; i++) {
            var queueName = Constants.ELEVATOR_QUEUE_PREFIX + i;
            var queue = QueueBuilder
                    .durable(queueName)
                    .exclusive()
                    .build();
            var binding = BindingBuilder
                    .bind(queue)
                    .to(exchange())
                    .with(queueName + ".routing-key");
            var admin = amqpAdmin();
            admin.declareQueue(queue);
            admin.declareBinding(binding);

            queues.add(queueName);
        }

        return queues;
    }

    @Bean
    public MessageConverter converter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public DefaultMessageHandlerMethodFactory defaultMessageHandlerMethodFactory() {
        var defaultMessageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        defaultMessageHandlerMethodFactory.afterPropertiesSet();
        return defaultMessageHandlerMethodFactory;
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory, MessageConverter converter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        try {
            for (String queue : queueNames()) {
                MethodRabbitListenerEndpoint endpoint = new MethodRabbitListenerEndpoint();
                endpoint.setId(elevatorCallListener.getClass().getSimpleName() + "_" + queue);
                endpoint.setQueueNames(queue);
                endpoint.setBean(elevatorCallListener);
                endpoint.setMethod(elevatorCallListener.getClass().getMethod("receiveElevatorCallTask", Task.class));
                endpoint.setMessageHandlerMethodFactory(defaultMessageHandlerMethodFactory());
                endpoint.setMessageConverter(converter());
                endpoint.setAckMode(AcknowledgeMode.MANUAL);
                registrar.registerEndpoint(endpoint);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
