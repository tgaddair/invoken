package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

public class LevelDoorActivator extends ClickActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/level-door-trim.png", 96, 64);
    private static final NaturalVector2 offset = NaturalVector2.of(1, 1);

    private final Vector2 center;
    private final Animation animation;
    private final int increment;

    private boolean open = false;

    private final Light light;

    private boolean activating = false;
    private float stateTime = 0;

    public LevelDoorActivator(int x, int y, int increment) {
        super(NaturalVector2.of(x + offset.x, y + offset.y), 3, 2);
        this.increment = increment;

        final float magnitude = 0.25f;
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        center = new Vector2(x + offset.x, y + offset.y);
        this.light = new StaticLight(center.cpy().add(2f, 1f), magnitude, false);
        setColor();
    }

    @Override
    public void activate(Agent agent, Level level) {
        if (!activating) {
            activating = true;
        }
    }

    @Override
    public void update(float delta, Level level) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
                open = !open;
                if (open) {
                    transition(level);
                }
            }
        }
    }

    private void transition(Level level) {
        level.transition(increment);
    }

    private void setColor() {
        light.setColor(1, 1, 1, 1f);
    }

    @Override
    public void postRegister(Level level) {
        level.addLight(light);
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getRenderPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x + 0.5f, position.y, frame.getRegionWidth()
                * Settings.SCALE, frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }

    @Override
    public float getZ() {
        // always draw below everything else
        return Float.POSITIVE_INFINITY;
    }
}
