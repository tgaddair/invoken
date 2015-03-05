package com.eldritch.invoken.ui;

import java.util.Random;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.encounter.ConnectedRoom;
import com.eldritch.invoken.encounter.ConnectedRoomManager;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;

public class Minimap {
    private final LocationMap map;
    private final Pixmap pixmap;
    private final Texture backTexture;
    
    private Texture texture;
    private int lastColor;
    private NaturalVector2 lastPosition;

    public Minimap(LocationMap map, long seed) {
        this.map = map;
        
        Pixmap backMap = new Pixmap(map.getWidth(), map.getHeight(), Format.RGBA8888);
        backMap.setColor(0, 0, 0, 0.75f);
        backMap.fill();
        backTexture = new Texture(backMap);
        backMap.dispose();
        
        pixmap = new Pixmap(map.getWidth(), map.getHeight(), Format.RGBA8888);
        
        // can be enabled if we wish to mark of where we've been
        Pixmap.setBlending(Blending.None);
        
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
        texture = new Texture(pixmap);
    }
    
    public void render(Player player, Batch batch, int width, int height) {
        if (player.getNaturalPosition() != lastPosition) {
            if (lastPosition != null) {
                // resets the old color
                pixmap.drawPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1, lastColor);
            }
            lastPosition = player.getNaturalPosition();
            lastColor = pixmap.getPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1);
            pixmap.drawPixel(lastPosition.x, pixmap.getHeight() - lastPosition.y - 1, 0xFFFFFFFF);
            texture = new Texture(pixmap);
        }
        
        // divide screen into fifths
        // we draw to the 2nd through 4th slices
        int length = 3 * height / 5;
        batch.draw(backTexture, height / 5, height / 5, length, length);
        batch.draw(texture, height / 5, height / 5, length, length);
    }
}
