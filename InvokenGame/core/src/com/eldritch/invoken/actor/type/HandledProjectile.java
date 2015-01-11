package com.eldritch.invoken.actor.type;

public abstract class HandledProjectile extends Projectile {
    public HandledProjectile(float width, float height, float speed, float damage) {
        super(width, height, speed, damage);
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
}