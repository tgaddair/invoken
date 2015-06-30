package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.effects.Detonation;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Detonate extends Augmentation {
    private static final float RANGE = 3f;
    private static final float DURATION = 1f;
    private static final int DAMAGE_SCALE = 100;

    private static class Holder {
        private static final Detonate INSTANCE = new Detonate();
    }

    public static Detonate getInstance() {
        return Holder.INSTANCE;
    }

    private Detonate() {
        super(false);
    }

    @Override
    public boolean isValid(Agent owner) {
        // only one at a time
        return !owner.isToggled(Detonate.class);
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
        return new DetonateAction(owner, target.getPosition());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new DetonateAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        float r = RANGE;
        if (!target.isAlive() || owner.dst2(target) < r * r) {
            return 0;
        }

        return Heuristics.randomizedDistanceScore(owner.dst2(target), r * r);
    }

    public class DetonateAction extends AnimatedAction {
        public DetonateAction(Agent actor, Vector2 target) {
            super(actor, Activity.Cast, Detonate.this);
        }

        @Override
        public void apply(Level level) {
            Damage damage = Damage.from(owner, DamageType.PHYSICAL, getBaseDamage(owner));
            owner.addEffect(new DetonateEffect(owner, damage));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.BUFF, owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class DetonateEffect extends BasicEffect {
        private final Damage damage;

        public DetonateEffect(Agent agent, Damage damage) {
            super(agent);
            this.damage = damage;
        }

        @Override
        protected void doApply() {
            target.toggleOn(Detonate.class);
        }

        @Override
        public void dispel() {
            target.toggleOff(Detonate.class);
            Detonation detonation = new Detonation(damage, target.getPosition().cpy(), RANGE);
            target.getLocation().addEntity(detonation);
        }

        @Override
        public boolean isFinished() {
            return getStateTime() > DURATION;
        }

        @Override
        protected void update(float delta) {
            float r = getStateTime() / DURATION;
            target.setRgb(r, 0, 0);
        }
    }
    
    private static int getBaseDamage(Agent owner) {
        return (int) (DAMAGE_SCALE * owner.getInfo().getStealthModifier());
    }
}
