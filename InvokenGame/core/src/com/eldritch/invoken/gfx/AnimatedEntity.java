package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Callback;

public class AnimatedEntity implements TemporaryEntity {
    private final List<Callback> actions = new ArrayList<>();
    private final Animation animation;
    private final Vector2 position;
    private final float width;
    private final float height;
    private float elapsed = 0;
    
    public AnimatedEntity(TextureRegion[] regions, Vector2 position, Vector2 size, float frameDuration) {
        animation = new Animation(frameDuration, regions);
        this.width = size.x;
        this.height = size.y;
        this.position = position;
    }
    
    @Override
    public void update(float delta, Level level) {
        elapsed += delta;
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(animation.getKeyFrame(elapsed),
                position.x - width / 2, position.y - height / 2,
                width, height);
        batch.end();
    }

    @Override
    public float getZ() {
        return 0;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public boolean isFinished() {
        return animation.isAnimationFinished(elapsed);
    }

    @Override
    public void dispose() {
        for (Callback callback : actions) {
            callback.call();
        }
    }
    
    public AnimatedEntity withCallback(Callback callback) {
        actions.add(callback);
        return this;
    }
}