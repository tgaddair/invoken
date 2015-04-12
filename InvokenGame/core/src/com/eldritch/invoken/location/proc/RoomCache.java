package com.eldritch.invoken.location.proc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.proto.Locations.Room;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RoomCache {
    private final LoadingCache<String, Room> availableRooms = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Room>() {
              public Room load(String roomId) {
                  return InvokenGame.ROOM_READER.readAsset(roomId);
              }
            });
    
    private final Map<String, List<Rectangle>> roomsById = new HashMap<String, List<Rectangle>>();
    
    public void put(String roomId, Rectangle rect) {
        if (!roomsById.containsKey(roomId)) {
            roomsById.put(roomId, new ArrayList<Rectangle>());
        }
        roomsById.get(roomId).add(rect);
    }
    
    public Room lookupRoom(String roomId) {
        try {
            return availableRooms.get(roomId);
        } catch (Exception ex) {
            InvokenGame.error("Failed to load room: " + roomId, ex);
            return null;
        }
    }
}
