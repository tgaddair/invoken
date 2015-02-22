package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.encounter.Location;

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
        return owner.isAiming();
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return owner.isAiming() && target != null
                && target != owner && owner.canTargetProjectile(target);
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        return owner.getWeaponSentry().hasLineOfSight(target) ? 1 : 0;
    }
}
