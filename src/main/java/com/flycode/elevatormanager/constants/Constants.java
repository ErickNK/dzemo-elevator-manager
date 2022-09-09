package com.flycode.elevatormanager.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    @UtilityClass
    static class DoorStates {
        public static final String OPEN = "open";
        public static final String CLOSED = "closed";
    }

    @UtilityClass
    static class ElevatorStates {
        public static final String MOVING = "open";
        public static final String STATIONARY = "stationary";
        public static final String DOOR_OPEN = "door_open";
        public static final String DOOR_CLOSED = "door_closed";
        public static final String DOOR_CLOSING = "door_closing";
        public static final String DOOR_OPENING = "door_opening";
    }

    @UtilityClass
    static class ElevatorDirection {
        public static final String UP = "up";
        public static final String DOWN = "down";
    }
}
