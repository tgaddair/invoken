package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Level;
import com.google.common.base.Optional;

public abstract class ProjectileAugmentation extends ActiveAugmentation {
    public ProjectileAugmentation(String asset) {
        super(asset);
    }
    
    public ProjectileAugmentation(Optional<String> asset) {
        super(asset);
    }
    
    @Override
    public Action getAction(Agent owner, Agent target) {
        return getAction(owner, target.getPosition());
    }
    
    @Override
    public boolean isAimed() {
        return true;
    }
    
    @Override
    public boolean isValid(Agent owner) {
//        return owner.isAiming();
        return true;
    }
    
    @Override
    public boolean isValidWithAiming(Agent owner, Agent target) {
        return true;
    }
    
    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner) && target != null
                && target != owner && owner.canTargetProjectile(target);
    }
    
    @Override
    public float quality(Agent owner, Agent target, Level level) {
        return target.isAlive() ? 1 : 0;
    }
}
