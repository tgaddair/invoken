package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Strings;

public class LevelDoorActivator extends ClickActivator implements ProximityActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/level-door.png", 80, 56);

    private final ProximityCache proximityCache = new ProximityCache(3);

    private final Vector2 center;
    private final Animation animation;
    private final boolean down;

    private boolean open = false;

    private final Light light;
    private final List<Body> bodies = new ArrayList<Body>();

    private boolean activating = false;
    private float stateTime = 0;

    public LevelDoorActivator(int x, int y, boolean down) {
        super(NaturalVector2.of(x, y), 2, 1);
        this.down = down;

        final float magnitude = 0.1f;
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        center = new Vector2(x + 1, y + 0.5f);
        this.light = new StaticLight(center.cpy().add(0.5f, 0.5f), magnitude, false);
        setColor();
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }

    @Override
    public void update(float delta, Level level) {
        // if a single agent is in the proximity, then open the door, otherwise close it
        boolean shouldOpen = false;
        for (Agent agent : level.getActiveEntities()) {
            if (inProximity(agent)) {
                shouldOpen = true;
                break;
            }
        }

        // only change the state of the door if it differs from the current state
        // must click to unlock
        if (shouldOpen != open) {
            setOpened(shouldOpen, level);
        }
        
        // when the animation finishes and the door is open, handle the event
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                PlayMode mode = animation.getPlayMode();
                animation
                        .setPlayMode(mode == PlayMode.NORMAL ? PlayMode.REVERSED : PlayMode.NORMAL);
                
                if (open) {
                    transition(level);
                }
            }
        }
    }
    
    
    private void transition(Level level) {
        if (down) {
            // increment level
            level.transition(1);
        } else {
            // decrement level
            level.transition(-1);
        }
    }

    @Override
    public void activate(Agent agent, Level level) {
        setOpened(!open, level);
    }

    private synchronized void setOpened(boolean opened, Level level) {
        if (activating) {
            // cannot interrupt
            return;
        }

        activating = true;
        open = opened;
        for (Body body : bodies) {
            body.setActive(!opened);
        }
        InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.DOOR_OPEN, getPosition());
    }

    private void setColor() {
        light.setColor(1, 1, 1, 1f);
    }

    @Override
    public void register(Level level) {
        level.addLight(light);
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }

    @Override
    public float getZ() {
        // always draw below everything else
        return Float.POSITIVE_INFINITY;
    }
}
