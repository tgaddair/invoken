package com.eldritch.invoken.activators;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.invoken.actor.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public abstract class ProximityActivator extends BasicActivator {
    private final Vector2 center = new Vector2();
    private final Vector2 offset = new Vector2();
    
    private final Set<Agent> proximityAgents = new HashSet<>();
    private final Set<Agent> lastProximityAgents = new HashSet<>();
    private final Set<Agent> triggerAgents = new HashSet<>();
    
    private Body sensor;
    private boolean proximityActive = false;

    public ProximityActivator(NaturalVector2 position, Vector2 center, Vector2 offset) {
        this(position.x, position.y, center, offset);
    }

    public ProximityActivator(float x, float y, Vector2 center, Vector2 offset) {
        super(x, y);
        this.center.set(center);
        this.offset.set(offset);
    }

    @Override
    public void update(float delta, Level level) {
        // only change the state of the door if it differs from the current
        // state must click to unlock
        boolean hasProximity = !proximityAgents.isEmpty();
        if (shouldActivate(hasProximity)) {
            lastProximityAgents.removeAll(proximityAgents);
            triggerAgents.clear();
            triggerAgents.addAll(lastProximityAgents);
            
            if (onProximityChange(hasProximity, level)) {
                proximityActive = hasProximity;
            }
        }

        lastProximityAgents.clear();
        lastProximityAgents.addAll(proximityAgents);
    }

    @Override
    public void activate(Agent agent, Level level) {
    }
    
    protected boolean shouldActivate(boolean hasProximity) {
        return hasProximity != proximityActive;
    }
    
    public boolean hasProximity(Agent agent) {
        return proximityAgents.contains(agent);
    }
    
    protected abstract boolean onProximityChange(boolean hasProximity, Level level);
    
    protected Body getSensor() {
        return sensor;
    }
    
    @Override
    public final void register(Level level) {
        sensor = createSensor(level, offset);
        postRegister(level);
    }
    
    protected abstract void postRegister(Level level);
    
    private Body createSensor(Level level, Vector2 offset) {
        float radius = 1.5f;
        CircleShape shape = new CircleShape();
        shape.setPosition(offset);
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.groupIndex = 0;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getPosition());
        bodyDef.type = BodyType.StaticBody;
        Body body = level.getWorld().createBody(bodyDef);

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(new ProximityHandler());

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_INVISIBLE; // does not interrupt targeting
        filter.maskBits = Settings.BIT_AGENT; // hit by agents
        fixture.setFilterData(filter);

        shape.dispose();
        return body;
    }
    
    private class ProximityHandler extends DefaultAgentHandler {
        @Override
        public boolean handle(Agent agent) {
            proximityAgents.add(agent);
            return true;
        }

        @Override
        public boolean handleEnd(Agent agent) {
            proximityAgents.remove(agent);
            return true;
        }
    }
}
