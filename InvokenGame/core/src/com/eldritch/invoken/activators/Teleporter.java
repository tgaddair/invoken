package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.proc.RoomGenerator.ControlRoom;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class Teleporter extends ClickActivator implements ProximityActivator {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(
            "sprite/activators/teleporter.png", 64, 64);

    private final ProximityCache proximityCache = new ProximityCache(1);
    private final NaturalVector2 origin;
    private final Vector2 center;
    
    private final Animation animation;
    private boolean activating = false;
    private float stateTime = 0;
    private boolean transitioned = false;
    private boolean canActivate = false;
    
    private Optional<String> destination = Optional.absent();
    private Optional<String> nextEncounter = Optional.absent();

    public Teleporter(NaturalVector2 position) {
        super(NaturalVector2.of(position.x, position.y), 2, 2);
        this.origin = position;
        center = new Vector2(position.x + 1f, position.y + 1f);
        animation = new Animation(0.05f, regions);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
    }
    
    @Override
    protected boolean canActivate(Agent agent) {
        return inProximity(agent);
    }

    @Override
    public void update(float delta, Level level) {
        canActivate = inProximity(level.getPlayer());
        
        // actually do the teleportation
        if (transitioned) {
            transition(level);
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

        Batch batch = renderer.getBatch();
        if (!canActivate) {
            batch.setColor(Color.GRAY);
        }
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
        if (!canActivate) {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void activate(Agent agent, Level level) {
        activating = true;
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }
    
    @Override
    public void register(Level level) {
        ConnectedRoomManager rooms = level.getConnections();
        ControlRoom encounter = rooms.getControlRoom(rooms.getRoom(origin.x, origin.y));
//        if (encounter != null && encounter.getEncounter().hasSuccessor()) {
//            destination = Optional.fromNullable(encounter.getEncounter().getSuccessor());
//            if (encounter.getEncounter().hasNextEncounter()) {
//                nextEncounter = Optional.fromNullable(encounter.getEncounter().getNextEncounter());
//            }
//        }
    }
    
    @Override
    public float getZ() {
        return Float.POSITIVE_INFINITY;
    }
    
    private void transition(Level level) {
        if (destination.isPresent() && !Strings.isNullOrEmpty(destination.get())) {
            level.transition(destination.get(), nextEncounter);
        }
    }
}
