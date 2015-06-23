package com.eldritch.invoken.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class Splash implements TemporaryEntity {
    private final TextureRegion region;
    private final Vector2 position;
    private final float duration;
    private float elapsed = 0;
    
    public Splash(TextureRegion region, Vector2 position, float duration) {
        this.region = region;
        this.position = position;
        this.duration = duration;
    }
    
    @Override
    public void update(float delta, Level level) {
        elapsed += delta;
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        final float width = region.getRegionWidth() * Settings.SCALE;
        final float height = region.getRegionHeight() * Settings.SCALE;
        
        Batch batch = renderer.getBatch();
        batch.begin();
        
        float alpha = MathUtils.lerp(1f, 0f, elapsed / duration);
        batch.setColor(1, 1, 1, alpha);
        batch.draw(region, position.x - width / 2, position.y - height / 2, width, height);
        
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    @Override
    public float getZ() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public boolean isFinished() {
        return elapsed > duration;
    }

    @Override
    public void dispose() {
    }
}