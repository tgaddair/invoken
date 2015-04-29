package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Blast extends Augmentation {
    private static final float RANGE = 3;
    private static final int COST = 2;
    private static final int DAMAGE_SCALE = 25;

    private static class Holder {
        private static final Blast INSTANCE = new Blast();
    }

    public static Blast getInstance() {
        return Holder.INSTANCE;
    }

    private Blast() {
        super(false);
    }

    @Override
    public boolean isValid(Agent owner) {
        return true;
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return isValid(owner);
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return isValid(owner);
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new BlastAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new BlastAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Location location) {
        float x = owner.dst2(target);
        if (x > RANGE * RANGE) {
            return 0;
        }
        return Heuristics.distanceScore(x, 0);
    }

    public class BlastAction extends AnimatedAction {
        public BlastAction(Agent actor) {
            super(actor, Activity.Cast, Blast.this);
        }

        @Override
        public void apply(Location location) {
            Damage damage = Damage.from(owner, DamageType.PHYSICAL, getBaseDamage(owner));
            for (Agent neighbor : owner.getNeighbors()) {
                if (owner.dst2(neighbor) < RANGE * RANGE) {
                    Vector2 direction = neighbor.getPosition().cpy().sub(owner.getPosition()).nor();
                    neighbor.addEffect(new Stunned(owner, neighbor, 1f));
                    neighbor.addEffect(new Bleed(neighbor, damage, direction.scl(500)));
                }
            }
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.BUFF, owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }
    
    private static int getBaseDamage(Agent owner) {
        return (int) (DAMAGE_SCALE * owner.getInfo().getExecuteModifier());
    }
}
