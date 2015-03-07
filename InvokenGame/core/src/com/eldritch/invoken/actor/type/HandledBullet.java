package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class HandledBullet extends Projectile implements HandledProjectile {
    public HandledBullet(Agent owner, TextureRegion region, float speed, float damage) {
        super(owner, region, speed, damage);
    }

    @Override
    protected void handleAgentContact(Agent agent) {
        agent.handleProjectile(this);
    }

    @Override
    protected void handleObstacleContact() {
        cancel();
    }

    public void apply(Agent target) {
        apply(getOwner(), target);
        cancel();
    }

    protected abstract void apply(Agent owner, Agent target);
}