package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.ConnectedRoomManager;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.proc.EncounterGenerator.EncounterRoom;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Strings;

public class Teleporter extends BasicActivator implements ProximityActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/teleporter.png", 64, 64);

    private final ProximityCache proximityCache = new ProximityCache(1);
    private final NaturalVector2 origin;
    private final Vector2 center;
    
    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;
    private boolean transitioned = false;
    
    private String destination;

    public Teleporter(NaturalVector2 position) {
        super(NaturalVector2.of(position.x + 1, position.y + 1));
        this.origin = position;
        center = new Vector2(position.x + 2f, position.y + 2f);
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(float delta, Location location) {
        if (inProximity(location.getPlayer())) {
            activate(location.getPlayer(), location);
        }
        
        // actually do the teleportation
        if (transitioned) {
            transition(location);
            transitioned = false;
        }
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (activating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                activating = false;
                stateTime = 0;
                transitioned = true;
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
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }
    
    @Override
    public void register(Location location) {
        ConnectedRoomManager rooms = location.getConnections();
        EncounterRoom encounter = rooms.getEncounter(rooms.getRoom(origin.x, origin.y));
        if (encounter != null && encounter.getEncounter().hasSuccessor()) {
            destination = encounter.getEncounter().getSuccessor();
        }
    }
    
    @Override
    public float getZ() {
        return Float.POSITIVE_INFINITY;
    }
    
    private void transition(Location location) {
        if (!Strings.isNullOrEmpty(destination)) {
            location.transition(destination);
        }
    }
}
