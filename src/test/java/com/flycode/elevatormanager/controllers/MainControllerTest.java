package com.flycode.elevatormanager.controllers;

import com.flycode.elevatormanager.constants.Constants;
import com.flycode.elevatormanager.dtos.CallElevatorRequest;
import com.flycode.elevatormanager.dtos.Response;
import com.flycode.elevatormanager.dtos.Task;
import com.flycode.elevatormanager.models.Elevator;
import com.flycode.elevatormanager.repositories.ElevatorRepository;
import com.pusher.rest.Pusher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest()
@TestPropertySource(
        locations = "classpath:application-integration-tests.properties",
        properties = {
                "spring.profiles.active=integration-tests",
                "spring.main.allow-bean-definition-overriding=true"
        })
@ActiveProfiles(profiles = {"integration-tests"})
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    Environment environment;

    @Autowired
    ElevatorRepository elevatorRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private RabbitTemplate rabbitTemplateMock;

    @MockBean
    private Pusher pusherMock;

    @Captor
    ArgumentCaptor<Task> taskArgumentCaptor;

    private HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<Object>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    protected String json(Object object) throws Exception {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(object, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("Test that a task is added to the elevator call queue if call is valid.")
    @Test
    void addsTaskToElevatorCallQueue() throws Exception {
        // Arrange
        CallElevatorRequest callElevatorRequest = new CallElevatorRequest(1L, 5);

        // Act
        ResultActions resultActions = mvc.perform(
                post("/api/v1/call-elevator")
                        .content(this.json(callElevatorRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        resultActions.andExpect(status().isOk());

        verify(rabbitTemplateMock).convertAndSend(
                eq(environment.getRequiredProperty("mq.main.exchange")),
                eq(Constants.ELEVATOR_QUEUE_PREFIX + callElevatorRequest.getElevatorId() + ".routing-key"),
                taskArgumentCaptor.capture()
        );
        assertThat(taskArgumentCaptor.getValue().getElevatorID()).isEqualTo(callElevatorRequest.getElevatorId());
        assertThat(taskArgumentCaptor.getValue().getFloorTo()).isEqualTo(callElevatorRequest.getFloorNumber());
    }

    @DisplayName("Test fetching elevators from db")
    @Test
    void fetchesAllElevators() throws Exception {
        // Act
        ResultActions resultActions = mvc.perform(
                get("/api/v1/elevators").contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        resultActions.andExpect(status().isOk());
        var data = (Response<List<Elevator>>) resultActions.andReturn().getAsyncResult(20L);
        assertThat(data.getData().size()).isEqualTo(environment.getRequiredProperty("elevator-configs.elevators-count", Integer.class));
    }
}