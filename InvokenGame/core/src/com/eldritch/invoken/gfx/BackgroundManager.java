package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.screens.GameScreen;

public class BackgroundManager {
    private final TextureRegion region = new TextureRegion(
            GameScreen.getTexture("sprite/starfield.png"));

    public BackgroundManager() {
        region.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    }

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
            h = w * (region.getRegionHeight() / region.getRegionWidth());
        } else {
            h = height;
            w = h * (region.getRegionWidth() / region.getRegionHeight());
        }

        region.setU(bounds.x / w);
        region.setV(bounds.y / h);
        region.setU2(bounds.x / w + 1);
        region.setV2(bounds.y / h - 1);

        batch.begin();
        batch.draw(region, bounds.x, bounds.y, w, h);
        batch.end();
    }
}
