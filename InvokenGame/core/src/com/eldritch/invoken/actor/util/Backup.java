package com.eldritch.invoken.actor.util;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.proto.Actors.PlayerActor;

public class Backup {
    private final int floor;
    private final String region;
    private final String roomName;
    private final Vector2 position = new Vector2();

    public Backup(PlayerActor.Backup proto) {
        this.floor = proto.getFloor();
        this.region = proto.getRegion();
        this.roomName = proto.getRoom();
        this.position.set(proto.getX(), proto.getY());
    }
    
    public Backup(int floor, String region, Vector2 position) {
        this(floor, region, "", position);
    }

    public Backup(int floor, String region, String roomName, Vector2 position) {
        this.floor = floor;
        this.region = region;
        this.roomName = roomName;
        this.position.set(position);
    }

    public int getFloor() {
        return floor;
    }

    public String getRegion() {
        return region;
    }

    public String getRoomName() {
        return roomName;
    }

    public Vector2 getPosition() {
        return position;
    }

    public PlayerActor.Backup toProto() {
        return PlayerActor.Backup.newBuilder().setFloor(floor).setRegion(region).setRoom(roomName)
                .setX(position.x).setY(position.y).build();
    }
}
