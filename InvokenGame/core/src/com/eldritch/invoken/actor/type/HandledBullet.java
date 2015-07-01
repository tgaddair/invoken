package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Utils;

public abstract class HandledBullet extends Projectile implements HandledProjectile {
    public HandledBullet(Agent owner, TextureRegion region, float speed, Damage damage) {
        super(owner, region, speed, damage);
    }

    public HandledBullet(Agent owner, TextureRegion region, float majorSize, float speed,
            Damage damage) {
        super(owner, Utils.getWidth(region, majorSize), Utils.getHeight(region, majorSize), speed,
                damage);
    }

    public HandledBullet(Agent owner, float width, float height, float speed, Damage damage) {
        super(owner, width, height, speed, damage);
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
        finish();
    }

    protected abstract void apply(Agent owner, Agent target);
}