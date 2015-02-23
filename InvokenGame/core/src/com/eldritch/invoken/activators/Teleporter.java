package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;

public class Teleporter extends BasicActivator implements ProximityActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/teleporter.png", 64, 64);

    private final ProximityCache proximityCache = new ProximityCache(1);
    private final Vector2 center;
    
    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;

    public Teleporter(NaturalVector2 position) {
        super(NaturalVector2.of(position.x + 1, position.y + 1));
        center = new Vector2(position.x + 2f, position.y + 2f);
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(float delta, Location location) {
        if (inProximity(location.getPlayer())) {
            activate(location.getPlayer(), location);
        }
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
            }
        }

        TextureRegion frame = animation.getKeyFrame(stateTime);
        Vector2 position = getPosition();

        Batch batch = renderer.getSpriteBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
    }

    @Override
    public void activate(Agent agent, Location location) {
        activating = true;
//        location.transition(destination);
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }
    
    @Override
    public float getZ() {
        return Float.POSITIVE_INFINITY;
    }
}
