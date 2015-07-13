package com.eldritch.invoken.ui;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Locations.ControlPoint;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class Minimap {
    private static final int PADDING = 10;
    
    private final Level level;
    private final LocationMap map;
    private final Pixmap pixmap;
    private final Texture backTexture;
    private final Set<ConnectedRoom> visited = new HashSet<>();
    private final Color color = new Color();
    private final BitmapFont font = new BitmapFont();
    
    private Texture texture;
    private int lastColor;
    private NaturalVector2 lastPosition;
    private ConnectedRoom lastRoom;

    public Minimap(Level level, long seed, Optional<PlayerActor> state) {
        this.level = level;
        this.map = level.getMap();
        
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
        int origin = height / 5;
        int length = 3 * origin;
        batch.draw(backTexture, origin, origin, length, length);
        batch.draw(texture, origin, origin, length, length);
        font.draw(batch, level.getFullName(), origin + PADDING, origin + length - PADDING);
    }
    
    private void visit(ConnectedRoom room) {
        float a = 0.5f;
        color.set(0, 0, 1, a);
        
        ConnectedRoomManager rooms = map.getRooms();
        if (Settings.DEBUG_CRITICAL_PATH) {
            if (room.onCriticalPath()) {
                color.set(0, 1, 0, a);
            }
        } else if (room.isChamber()) {
            ControlPoint cp = rooms.getControlRoom(room).getControlPoint();
            if (cp.getOrigin()) {
                color.set(1, 0, 1, a);
            } else if (cp.getExit()) {
                color.set(0, 1, 1, a);
            }
        }
        
        pixmap.setColor(color);
        for (NaturalVector2 point : room.getPoints()) {
            pixmap.drawPixel(point.x, pixmap.getHeight() - point.y - 1);
        }
        visited.add(room);
    }
}
