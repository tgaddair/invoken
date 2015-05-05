package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.screens.GameScreen;

public class FogMaskManager {
    private final Fader[] faders = { new Fader("mask1", 0),
            new Fader("mask2", (float) Math.random()), new Fader("mask3", 1) };

    public void update(float delta) {
        for (Fader fader : faders) {
            fader.update(delta);
        }
    }

    public void render(OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getBatch();
        batch.begin();
        for (Fader fader : faders) {
            fader.render(batch, renderer.getViewBounds());
        }
        batch.end();
    }

    private static class Fader {
        // game delta is very large at points, so just use a fixed increment
        private static final float MAX_FOG = 0.4f;
        private static final float DELTA = 0.01f;
        private static final float DURATION = 3f;
        private static final float SCALE = 32f;
        private final TextureRegion region;

        private int direction = 1;
        private float elapsed;

        public Fader(String asset, float start) {
            this.region = new TextureRegion(GameScreen.getTexture(String.format(
                    "sprite/mask/%s.png", asset)));
            region.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            this.elapsed = start * DURATION;
        }

        public void update(float delta) {
            elapsed += DELTA * direction;
            if (elapsed < 0 || elapsed > DURATION) {
                direction *= -1;
                elapsed = Math.max(Math.min(elapsed, DURATION), 0);
            }
        }

        public void render(Batch batch, Rectangle bounds) {
            float alpha = MathUtils.lerp(0, MAX_FOG, elapsed / DURATION);
            region.setU(bounds.x / SCALE);
            region.setV(bounds.y / SCALE);
            region.setU2(bounds.x / SCALE + 1);
            region.setV2(bounds.y / SCALE - 1);
            
            batch.setColor(1, 1, 1, alpha);
            batch.draw(region, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }
    }
}
