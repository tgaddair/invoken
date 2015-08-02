package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Shield;
import com.eldritch.invoken.effects.Shield.FixedShield;
import com.eldritch.invoken.location.Level;

public class Aegis extends SelfAugmentation {
    private static final String TOOLTIP = "Barrier\n\n"
            + "Absorbs up to 100 damage from incoming projectiles in the direction "
            + "the user is currently facing.  Sustained the shield reduces movement speed.\n\n"
            + "Mode: Sustained\n"
            + "Cost: 0";
    
    private static class Holder {
        private static final Aegis INSTANCE = new Aegis();
    }

    public static Aegis getInstance() {
        return Holder.INSTANCE;
    }

    private Aegis() {
        super("barrier");
    }

    @Override
    public void release(Agent owner) {
        owner.toggleOff(Shield.class);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new ShieldAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ShieldAction(owner);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }

    @Override
    public int getCost(Agent owner) {
        return owner.isToggled(Shield.class) ? 0 : 1;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!owner.isToggled(Shield.class)) {
            // shield is currently inactive
            if (owner.getInfo().getEnergyPercent() > 0.75f) {
                // it's only worth using the shield if we have enough reserve energy to follow it
                // up with an attack
                // TODO: it would also help if we wished to do a flee or hide routine
                for (Agent enemy : owner.getThreat().getEnemies()) {
                    if (enemy.getInventory().hasRangedWeapon()) {
                        return 2;
                    }
                }
            }
            return 0;
        } else {
            for (Agent enemy : owner.getThreat().getEnemies()) {
                if (enemy.getInventory().hasRangedWeapon()) {
                    // don't turn off the shield if we still have enemies shooting at us
                    return 0;
                }
            }
            
            // turn off the shield if not facing an enemy with a ranged weapon
            return 2;
        }
    }
    
    @Override
    public String getTooltip() {
        return TOOLTIP;
    }

    public class ShieldAction extends AnimatedAction {
        public ShieldAction(Agent actor) {
            super(actor, Activity.Cast, Aegis.this);
        }

        @Override
        public void apply(Level level) {
            if (owner.toggle(Shield.class)) {
                owner.addEffect(new FixedShield(owner, Aegis.this));
            }
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
}
