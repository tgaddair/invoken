package com.eldritch.invoken.location.proc;

import com.eldritch.invoken.proto.Locations.ControlPoint;

public class ControlPointGenerator {
    public static ControlPoint generateOrigin(int floor) {
        ControlPoint.Builder builder = ControlPoint.newBuilder().setOrigin(true);
        if (floor == 0) {
            builder.addRoomId("Bedroom");
        }
        return builder.build();
    }
    
    public static ControlPoint generate(int min, int max) {
        return ControlPoint.newBuilder().setMin(min).setMax(max).build();
    }
    
    public static ControlPoint generateExit(int floor) {
        return ControlPoint.newBuilder().setExit(true).addRoomId("Exit").build();
    }
    
    private ControlPointGenerator() {}
}
