package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.effects.SummonEnergy;
import com.eldritch.invoken.gfx.AnimatedEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class Discharge extends ProjectileAugmentation {
    private static final TextureRegion BOLT_TEXTURE = new TextureRegion(
            GameScreen.getTexture("sprite/effects/bolt.png"));

    private static final float BOLT_SIZE = 2.5f;
    private static final int DAMAGE_SCALE = 35;
    private static final int BASE_COST = 25;
    private static final float BULLET_VELOCITY = 15;

    private static class Holder {
        private static final Discharge INSTANCE = new Discharge();
    }

    public static Discharge getInstance() {
        return Holder.INSTANCE;
    }

    private Discharge() {
        super("discharge");
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new DischargeAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive() || target == owner) {
            return 0;
        }

        float penalty = 1f;
        final float maxDst = 2f;
        for (Agent neighbor : owner.getVisibleNeighbors()) {
            if (owner.isAlly(neighbor) && owner.dst2(neighbor) < maxDst * maxDst) {
                penalty *= 0.25f;
            }
        }

        final float idealDst = 1f;
        float score = Heuristics.randomizedDistanceScore(owner.dst2(target), idealDst * idealDst);
        return score * penalty;
    }

    public class DischargeAction extends AnimatedAction {
        private final Vector2 target;

        public DischargeAction(Agent owner, Vector2 target) {
            super(owner, Activity.Swipe, Discharge.this, new SummonEnergy(owner));
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            for (int i = 0; i < 8; i++) {
                float theta = 45f * i;
                DischargeBolt bullet = new DischargeBolt(owner, theta);
                level.addEntity(bullet);
            }
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class DischargeBolt extends HandledBullet {
        public DischargeBolt(Agent owner, float theta) {
            super(owner, BOLT_TEXTURE, BOLT_SIZE, fixedSentryDirection(owner),
                    BULLET_VELOCITY * 0.5f, Damage.from(owner, DamageType.RADIOACTIVE,
                            getBaseDamage(owner)));
            rotate(theta);
        }

        @Override
        protected void apply(Agent owner, Agent target, Vector2 contact) {
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(target, getDamage(), contact, velocity.cpy().nor().scl(50)));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.HIT, target.getPosition());
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return BOLT_TEXTURE;
        }

        @Override
        protected void onFinish() {
            getOwner().getLocation().addEntity(AnimatedEntity.createDisintegrate(getPosition()));
        }

        private static int getBaseDamage(Agent owner) {
            return (int) (DAMAGE_SCALE * owner.getInfo().getExecuteModifier());
        }
    }

}
