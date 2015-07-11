package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class WorldText {
    private final Vector3 screen = new Vector3();
    private final BitmapFont font = new BitmapFont();
    private final SpriteBatch batch = new SpriteBatch();
    
    public WorldText() {
        font.getData().setScale(1.5f);
    }

    public void render(String text, Camera camera, float worldX, float worldY) {
        camera.project(screen.set(worldX, worldY, 0));
        batch.begin();
        font.draw(batch, text, screen.x, screen.y);
        batch.end();
    }
    
    public void setColor(Color color) {
        font.setColor(color);
    }
}
