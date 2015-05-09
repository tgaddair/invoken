package com.eldritch.invoken.ui;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class Minimap {
    private final LocationMap map;
    private final Pixmap pixmap;
    private final Texture backTexture;
    private final Set<ConnectedRoom> visited = new HashSet<>();
    
    private Texture texture;
    private int lastColor;
    private NaturalVector2 lastPosition;
    private ConnectedRoom lastRoom;

    public Minimap(LocationMap map, long seed, Optional<PlayerActor> state) {
        this.map = map;
        
        Pixmap backMap = new Pixmap(map.getWidth(), map.getHeight(), Format.RGBA8888);
        backMap.setColor(0, 0, 0, 0.75f);
        backMap.fill();
        backTexture = new Texture(backMap);
        backMap.dispose();
        
        pixmap = new Pixmap(map.getWidth(), map.getHeight(), Format.RGBA8888);
        
        // can be enabled if we wish to mark of where we've been
        Pixmap.setBlending(Blending.None);
        
        if (Settings.DEBUG_MAP) {
            Random rand = new Random(seed);
            ConnectedRoomManager rooms = map.getRooms();
            for (ConnectedRoom room : rooms.getRooms()) {
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                pixmap.setColor(r, g, b, 0.5f);
                
                for (NaturalVector2 point : room.getPoints()) {
                    pixmap.drawPixel(point.x, pixmap.getHeight() - point.y - 1);
                }
            }
        }
        
        if (state.isPresent()) {
            PlayerActor data = state.get();
            Set<Integer> visitedIndices = new HashSet<>(data.getVisitedRoomsList());
            
            int i = 0;
            for (ConnectedRoom room : map.getRooms().getRooms()) {
                if (visitedIndices.contains(i)) {
                    visit(room);
                }
                i++;
            }
        }
        
        texture = new Texture(pixmap);
    }
    
    public void update(Player player) {
        if (player.getNaturalPosition() != lastPosition) {
            if (lastPosition != null) {
                // resets the old color
                pixmap.drawPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1, lastColor);
            }
            lastPosition = player.getNaturalPosition();
            
            // check for new room
            ConnectedRoom room = map.getRooms().getRoom(lastPosition.x, lastPosition.y);
            if (room != null && room != lastRoom && !visited.contains(room)) {
                visit(room);
                lastRoom = room;
            }
            
            lastColor = pixmap.getPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1);
            pixmap.drawPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1, 0xFFFFFFFF);
            texture = new Texture(pixmap);
        }
    }
    
    public void render(Batch batch, int width, int height) {
        // divide screen into fifths
        // we draw to the 2nd through 4th slices
        int length = 3 * height / 5;
        batch.draw(backTexture, height / 5, height / 5, length, length);
        batch.draw(texture, height / 5, height / 5, length, length);
    }
    
    private void visit(ConnectedRoom room) {
        pixmap.setColor(0, 0, 1, 0.5f);
        for (NaturalVector2 point : room.getPoints()) {
            pixmap.drawPixel(point.x, pixmap.getHeight() - point.y - 1);
        }
        visited.add(room);
    }
}
