package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;

public abstract class ProjectileAugmentation extends Augmentation {
    public ProjectileAugmentation(String asset) {
        super(asset);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner, target.getPosition());
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.canTargetProjectile(target);
    }
}
