package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.encounter.Location;

public class RendWeapon extends Augmentation {
    private final Vector2 strike = new Vector2();
    
    public RendWeapon() {
        super("rend");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new RendAction(owner, target);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return target != null && target != owner && owner.getInventory().hasMeleeWeapon();
    }
    
    @Override
    public int getCost(Agent owner) {
        return 1;
    }
    
    @Override
    public float quality(Agent owner, Agent target, Location location) {
        MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
        return owner.dst2(target) <= weapon.getRange() ? 1 : 0;
    }

    public class RendAction extends AnimatedAction {
        private final Agent target;

        public RendAction(Agent actor, Agent target) {
            super(actor, Activity.Swipe, RendWeapon.this);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            strike.set(owner.getPosition());
            strike.add(owner.getForwardVector().scl(weapon.getRange() / 2));
            if (strike.dst2(target.getPosition()) <= weapon.getRange()) {
                target.addEffect(new Bleed(owner, target, weapon.getDamage()));
            }
        }
        
        @Override
        public Vector2 getPosition() {
            return target.getPosition();
        }
    }
}
