package com.flycode.elevatormanager.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String ELEVATOR_QUEUE_PREFIX = "elevator-";
    public static final String ELEVATOR_PUSHER_CHANNEL = "elevator-movements";

    @UtilityClass
    public static class DoorStates {
        public static final String OPEN = "open";
        public static final String CLOSED = "closed";
    }

    @UtilityClass
    public static class ElevatorStates {
        public static final String MOVING = "moving";
        public static final String STATIONARY = "stationary";
        public static final String DOOR_OPEN = "door_open";
        public static final String DOOR_CLOSED = "door_closed";
        public static final String DOOR_CLOSING = "door_closing";
        public static final String DOOR_OPENING = "door_opening";
    }

    @UtilityClass
    public static class ElevatorDirection {
        public static final String UP = "up";
        public static final String DOWN = "down";
        public static final String NONE = "none";
    }
}
