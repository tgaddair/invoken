package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.screens.GameScreen;

public class BackgroundManager {
    private final Texture bg = GameScreen.getTexture("sprite/starfield.png");

    public void update(float delta) {
    }

    public void render(OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getBatch();
        Rectangle bounds = renderer.getViewBounds();
        float width = bounds.width;
        float height = bounds.height;
        
        // render the background
        float w;
        float h;
        if (width < height) {
            w = width;
            h = w * (bg.getHeight() / bg.getWidth());
        } else {
            h = height;
            w = h * (bg.getWidth() / bg.getHeight());
        }
        
        batch.begin();
        batch.draw(bg, bounds.x, bounds.y, w, h);
        batch.end();
    }
}
