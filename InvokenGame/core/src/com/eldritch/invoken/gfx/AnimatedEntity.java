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
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Callback;
import com.eldritch.invoken.util.Utils;

public class AnimatedEntity implements TemporaryEntity {
    private static final TextureRegion[] DISINTEGRATE_REGIONS = GameScreen
            .getMergedRegion(getDisintegrateAssets());
    private static final float DISINTEGRATE_SIZE = 1f;

    private static final TextureRegion[] SMOKE_REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/smoke-ring.png", 128, 128);

    private final List<Callback> actions = new ArrayList<>();
    private final Animation animation;
    private final Vector2 position;
    private final float width;
    private final float height;
    private float elapsed = 0;

    public AnimatedEntity(TextureRegion[] regions, Vector2 position, Vector2 size,
            float frameDuration) {
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
        batch.draw(animation.getKeyFrame(elapsed), position.x - width / 2, position.y - height / 2,
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

    public static AnimatedEntity createDisintegrate(Vector2 position) {
        return new AnimatedEntity(DISINTEGRATE_REGIONS, position, Utils.getSize(
                DISINTEGRATE_REGIONS[0], DISINTEGRATE_SIZE), 0.1f);
    }

    public static AnimatedEntity createSmokeRing(Vector2 position, float range) {
        return new AnimatedEntity(SMOKE_REGIONS, position, new Vector2(range * 1.5f, range * 1.5f),
                0.05f);
    }

    private static String[] getDisintegrateAssets() {
        String[] assets = new String[11];
        for (int i = 0; i <= 9; i++) {
            assets[i] = format("0" + i);
        }
        assets[10] = format("10");
        return assets;
    }

    private static String format(String i) {
        return "sprite/effects/disintegrate/disintegrate" + i + ".png";
    }
}