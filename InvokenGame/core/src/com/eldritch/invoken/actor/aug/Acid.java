package com.eldritch.invoken.actor.aug;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Acid extends ProjectileAugmentation {
    private static final int DAMAGE_SCALE = 10;
    private static final int BASE_COST = 10;
    private static final float SPREAD_DEGREES = 5f;
    private static final float PELLET_SCALE = 0.35f;

    private static class Holder {
        private static final Acid INSTANCE = new Acid();
    }

    public static Acid getInstance() {
        return Holder.INSTANCE;
    }

    private Acid() {
        super(Optional.<String>absent());
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
    public float quality(Agent owner, Agent target, Location location) {
        if (!target.isAlive()) {
            return 0;
        }
        
        float idealDst = 3f;
        return Heuristics.randomizedDistanceScore(owner.dst2(target), idealDst * idealDst);
    }

    public class AcidAction extends AnimatedAction {
        private final Vector2 target;

        public AcidAction(Agent actor, Vector2 target) {
            super(actor, Activity.Swipe, Acid.this);
            this.target = target;
        }

        @Override
        public void apply(Location location) {
            for (HandledProjectile bullet : getProjectiles(owner)) {
                location.addEntity(bullet);
            }
        }

        @Override
        public Vector2 getPosition() {
            return target;
        }
    }

    public static class AcidPellet extends HandledBullet {
        private static final float V_MAX = 12f;
        
        private static final TextureRegion[] regions = GameScreen.getRegions(
                "sprite/effects/drain-attack.png", 32, 32)[0];
        private final Animation animation;
        
        public AcidPellet(Agent owner, float theta, float scale) {
            super(owner, regions[0], V_MAX, Damage.from(owner, DamageType.TOXIC, getBaseDamage(owner)));
            rotate(theta);
            animation = new Animation(0.1f, regions);
            animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        @Override
        protected void apply(Agent owner, Agent target) {
            target.addEffect(new Stunned(owner, target, 0.2f));
            target.addEffect(new Bleed(target, getDamage(), velocity.cpy().nor().scl(50)));
            InvokenGame.SOUND_MANAGER.playAtPoint(SoundEffect.HIT, target.getPosition());
        }

        @Override
        protected TextureRegion getTexture(float stateTime) {
            return animation.getKeyFrame(stateTime);
        }
        
        private static int getBaseDamage(Agent owner) {
            return DAMAGE_SCALE;
        }
    }
    
    public List<HandledProjectile> getProjectiles(Agent owner) {
        ImmutableList.Builder<HandledProjectile> builder = ImmutableList.builder();
        for (int i = -1; i <= 1; i++) {
            float theta = (float) (SPREAD_DEGREES * i * Math.random());
            builder.add(new AcidPellet(owner, theta, PELLET_SCALE));
        }
        return builder.build();
    }
}
