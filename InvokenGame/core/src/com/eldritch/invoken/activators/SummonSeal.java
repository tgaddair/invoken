package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.google.common.base.Optional;

public class SummonSeal extends ProximityActivator {
    private static final float RADIUS = 1.5f;
    private static final float MAX_DST2 = (RADIUS * 2) * (RADIUS * 2);

    private static final TextureRegion region = GameScreen.ATLAS.findRegion("trap/advanced");

    private final List<Agent> agents = new ArrayList<>();
    private final Color color = new Color(Color.WHITE);
    private Optional<ConnectedRoom> room = Optional.absent();
    private boolean released = false;

    public SummonSeal(NaturalVector2 position) {
        super(position, ProximityParams.of(new Vector2(position.x + 0.5f, position.y + 0.5f),
                Vector2.Zero, RADIUS));
    }

    @Override
    protected void postUpdate(float delta, Level level) {
        setAlpha(level);
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = getRenderPosition();
        float w = 1;
        float h = 1;
        Batch batch = renderer.getBatch();
        batch.setColor(color);
        batch.begin();
        batch.draw(region, position.x, position.y, w, h);
        batch.end();
        batch.setColor(Color.WHITE);
    }

    @Override
    protected boolean onProximityChange(boolean hasProximity, Level level) {
        if (hasProximity && !released && hasNonResident()) {
            release();
        }
        return false;
    }

    @Override
    public float getZ() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    protected void postRegister(Level level) {
        room = level.getRoomFor(NaturalVector2.of(getCenter()));

        // select some subset of the residents to be in stasis under this seal
        if (room.isPresent()) {
            for (Agent resident : room.get().getResidents()) {
                add(resident);
            }
        }
    }

    private void add(Agent agent) {
        // stasis means both inactive (not rendered or affected by physics)
        // and paralyzed (unable to act)
        agent.setActive(false);
        agent.setParalyzed(true);
        agents.add(agent);
    }

    private void release() {
        for (Agent agent : agents) {
            agent.setActive(true);
            agent.setParalyzed(false);
        }
        released = true;
    }

    private boolean hasNonResident() {
        if (!room.isPresent()) {
            // no room means the activator is definitely not a resident
            return true;
        }

        // when at least one of the triggering agents is a non-resident, then allow it
        for (Agent agent : getProximityAgents()) {
            if (!room.get().isResident(agent)) {
                return true;
            }
        }
        return false;
    }

    private void setAlpha(Level level) {
        if (released) {
            // opaque
            color.a = 1f;
            return;
        }
        
        // set alpha component
        float dst2 = getCenter().dst2(level.getPlayer().getPosition());
        if (dst2 > MAX_DST2) {
            // invisible
            color.a = 0f;
        } else {
            float a = MathUtils.lerp(0f, 1f, 1f - dst2 / MAX_DST2);
            color.a = a;
        }
    }
}
