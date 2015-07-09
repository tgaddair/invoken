package com.eldritch.invoken.actor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Projectile;
import com.eldritch.invoken.util.Damage;
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
    
    public static abstract class ProjectileAgentHandler implements AgentHandler {
        private Projectile projectile;
        
        public void setup(Projectile projectile) {
            this.projectile = projectile;
            
            Body body = getBody();
            body.setTransform(projectile.getPosition(), 0);
            body.setLinearVelocity(projectile.getVelocity());
            body.setActive(true);
            
            // collision filters
            for (Fixture fixture : body.getFixtureList()) {
                Filter filter = fixture.getFilterData();
                if (filter.maskBits != projectile.getCollisionMask()) {
                    filter.maskBits = projectile.getCollisionMask();
                    fixture.setFilterData(filter);
                }
            }
        }
        
        public Damage getDamage() {
            return projectile.getDamage();
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
            return projectile.handle(agent);
        }

        @Override
        public boolean handle(Object userData) {
            return projectile.handle(userData);
        }
        
        @Override
        public boolean handleEnd(Agent agent) {
            return projectile.handleEnd(agent);
        }

        @Override
        public boolean handleEnd(Object userData) {
            return projectile.handleEnd(userData);
        }

        @Override
        public short getCollisionMask() {
            return projectile.getCollisionMask();
        }
        
        public abstract Body getBody();
    }
}
