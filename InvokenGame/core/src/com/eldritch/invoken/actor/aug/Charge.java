package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation.ActiveAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.effects.BasicEffect;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.GenericDialogue;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public class Charge extends ActiveAugmentation {
    private static final float CHARGE_RANGE = 5;
    private static final float V_DELTA = 5f;
    private static final float A_DELTA = 5f;
    private static final float CHARGE_DURATION = 5f;

    private static class Holder {
        private static final Charge INSTANCE = new Charge();
    }

    public static Charge getInstance() {
        return Holder.INSTANCE;
    }

    private Charge() {
        super(Optional.<String>absent());
    }

    @Override
    public boolean isValid(Agent owner) {
        // only one at a time
        return !owner.isToggled(Charge.class);
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
        return new ChargeAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new ChargeAction(owner);
    }

    @Override
    public int getCost(Agent owner) {
        return 2;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive() || target.isAimingAt(owner)) {
            return 0;
        }
        
        if (owner.getInventory().hasMeleeWeapon()) {
            float r = CHARGE_RANGE;
            return Heuristics.randomizedDistanceScore(owner.dst2(target), r * r);
        }
        return 0;
    }

    public class ChargeAction extends AnimatedAction {
        public ChargeAction(Agent actor) {
            super(actor, Activity.Cast, Charge.this);
        }

        @Override
        public void apply(Level level) {
            owner.addEffect(new ChargeEffect(owner));
            owner.announce(GenericDialogue.onCharge(owner, owner.getTarget()));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.BUFF, owner.getPosition());
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class ChargeEffect extends BasicEffect {
        public ChargeEffect(Agent agent) {
            super(agent);
        }

        @Override
        protected void doApply() {
            target.toggleOn(Charge.class);
            target.changeMaxAcceleration(A_DELTA);
            target.changeMaxVelocity(V_DELTA);
        }

        @Override
        public void dispel() {
            target.changeMaxAcceleration(-A_DELTA);
            target.changeMaxVelocity(-V_DELTA);
            target.toggleOff(Charge.class);
        }

        @Override
        public boolean isFinished() {
            return getStateTime() > CHARGE_DURATION;
        }

        @Override
        protected void update(float delta) {
        }
    }
}
