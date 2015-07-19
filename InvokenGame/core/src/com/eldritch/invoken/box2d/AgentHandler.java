package com.eldritch.invoken.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public interface AgentHandler {
    boolean handle(Agent agent);
    
    boolean handle(Object userData);
    
    boolean handleEnd(Agent agent);
    
    boolean handleEnd(Object userData);
    
    short getCollisionMask();
    
    public static class DefaultAgentHandler implements AgentHandler {
        @Override
        public boolean handle(Agent agent) {
            return false;
        }

        @Override
        public boolean handle(Object userData) {
            return false;
        }
        
        @Override
        public boolean handleEnd(Agent agent) {
            return false;
        }

        @Override
        public boolean handleEnd(Object userData) {
            return false;
        }

        @Override
        public short getCollisionMask() {
            return Settings.BIT_ANYTHING;
        }
    }
    
    public static abstract class DamagingAgentHandler implements AgentHandler, Damager {
        private AgentHandler delegate;
        private Damage damage;
        
        public void setup(AgentHandler delegate, Damage damage) {
            this.delegate = delegate;
            this.damage = damage;
            
            // collision filters
            for (Fixture fixture : getBody().getFixtureList()) {
                Filter filter = fixture.getFilterData();
                if (filter.maskBits != delegate.getCollisionMask()) {
                    filter.maskBits = delegate.getCollisionMask();
                    fixture.setFilterData(filter);
                }
            }
        }
        
        public Damage getDamage() {
            return damage;
        }

        public void setActive(boolean active) {
            getBody().setActive(active);
        }

        public Vector2 getPosition() {
            return getBody().getPosition();
        }

        public Vector2 getVelocity() {
            return getBody().getLinearVelocity();
        }

        public void setVelocity(Vector2 velocity) {
            getBody().setLinearVelocity(velocity);
        }
        
        @Override
        public boolean handle(Agent agent) {
            return delegate.handle(agent);
        }

        @Override
        public boolean handle(Object userData) {
            return delegate.handle(userData);
        }
        
        @Override
        public boolean handleEnd(Agent agent) {
            return delegate.handleEnd(agent);
        }

        @Override
        public boolean handleEnd(Object userData) {
            return delegate.handleEnd(userData);
        }

        @Override
        public short getCollisionMask() {
            return delegate.getCollisionMask();
        }
        
        public abstract Body getBody();
    }
}
