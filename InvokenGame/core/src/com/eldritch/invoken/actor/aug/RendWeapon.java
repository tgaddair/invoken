package com.eldritch.invoken.actor.aug;

import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Agent.Activity;
import com.eldritch.invoken.actor.items.MeleeWeapon;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.encounter.Location;

public class RendWeapon extends Augmentation {
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

    public class RendAction extends AnimatedAction {
        private final Agent target;

        public RendAction(Agent actor, Agent target) {
            super(actor, Activity.Swipe);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            MeleeWeapon weapon = owner.getInventory().getMeleeWeapon();
            if (owner.dst2(target) <= weapon.getRange()) {
                target.addEffect(new Bleed(owner, target, weapon.getDamage()));
            }
        }
    }
}
