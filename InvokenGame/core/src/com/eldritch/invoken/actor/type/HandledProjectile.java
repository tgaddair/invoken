package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.AgentHandler;

public abstract class HandledProjectile extends Projectile implements AgentHandler {
    public HandledProjectile(TextureRegion region, float speed, float damage) {
        super(region, speed, damage);
    }
    
    @Override
    public boolean handle(Agent agent) {
        handleAgentContact(agent);
        return true;
    }
    
    @Override
    public boolean handle() {
        handleObstacleContact();
        return true;
    }
    
    @Override
    protected void handleAgentContact(Agent agent) {
    	agent.handleProjectile(this);
    }
    
    @Override
    protected void handleObstacleContact() {
    	cancel();
    }
    
    public static interface ProjectileHandler {
        boolean handle(HandledProjectile handledProjectile);
    }
    
    public void apply(Agent target) {
        apply(getOwner(), target);
        cancel();
    }
    
    protected abstract void apply(Agent owner, Agent target);
}