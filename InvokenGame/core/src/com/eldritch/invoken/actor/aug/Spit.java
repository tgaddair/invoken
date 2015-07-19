package com.eldritch.invoken.actor.aug;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

public class Spit extends ProjectileAugmentation {
    private static final int DAMAGE_SCALE = 10;
    private static final int BASE_COST = 20;
    private static final float PELLET_SCALE = 0.35f;

    private static class Holder {
        private static final Spit INSTANCE = new Spit();
    }

    public static Spit getInstance() {
        return Holder.INSTANCE;
    }

    private Spit() {
        super(Optional.<String> absent());
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new AcidAction(owner, target);
    }

    @Override
    public int getCost(Agent owner) {
        return BASE_COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (!target.isAlive()) {
            return 0;
        }

        float idealDst = 3f;
        return Heuristics.randomizedDistanceScore(owner.dst2(target), idealDst * idealDst);
    }

    public class AcidAction extends AnimatedAction {
        private final Vector2 target;

        public AcidAction(Agent actor, Vector2 target) {
            super(actor, Activity.Cast, Spit.this);
            this.target = target;
        }

        @Override
        public void apply(Level level) {
            level.addEntity(new AcidPellet(owner, PELLET_SCALE));
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
        
        @Override
        protected float getHoldSeconds() {
            return 0.25f;
        }
    }

    public static class AcidPellet extends HandledBullet {
        private static final float V_MAX = 10f;

        private static final TextureRegion PELLET_TEXTURE = new TextureRegion(
                GameScreen.getTexture("sprite/effects/toxic-projectile.png"));

        public AcidPellet(Agent owner, float scale) {
            super(owner, PELLET_TEXTURE, V_MAX, Damage.from(owner, DamageType.TOXIC,
                    getBaseDamage(owner)));
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(target, getDamage(), velocity.cpy().nor().scl(50)));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.HIT, target.getPosition());
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return PELLET_TEXTURE;
        }

        private static int getBaseDamage(Agent owner) {
            return DAMAGE_SCALE;
        }
        
        @Override
        public short getCollisionMask() {
            return Settings.BIT_HIGH_SHOOTABLE;
        }
        
        @Override
        protected void onFinish() {
        }
    }
}
