package com.eldritch.invoken.actor.aug;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.HandledBullet;
import com.eldritch.invoken.actor.type.HandledProjectile;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.location.Location;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Heuristics;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class Acid extends ProjectileAugmentation {
    private static final int DAMAGE_SCALE = 10;
    private static final int BASE_COST = 20;
    private static final float SPREAD_DEGREES = 5f;
    private static final float PELLET_SCALE = 0.35f;

    private static class Holder {
        private static final Acid INSTANCE = new Acid();
    }

    public static Acid getInstance() {
        return Holder.INSTANCE;
    }

    private Acid() {
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
        private static final float V_MAX = 10f;

        private static final TextureRegion PELLET_TEXTURE = new TextureRegion(
                GameScreen.getTexture("sprite/effects/toxic-projectile.png"));

        public AcidPellet(Agent owner, float theta, float scale) {
            super(owner, PELLET_TEXTURE, V_MAX, Damage.from(owner, DamageType.TOXIC,
                    getBaseDamage(owner)));
            rotate(theta);
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
            getOwner().getLocation().addEntity(new Splash(getPosition().cpy(), 3));
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
    
    private static class Splash implements TemporaryEntity {
        private static final TextureRegion SPLASH_REGION = new TextureRegion(
                GameScreen.getTexture("sprite/effects/toxic-splash.png"));
        
        private final Vector2 position;
        private final float duration;
        private float elapsed = 0;
        
        public Splash(Vector2 position, float duration) {
            this.position = position;
            this.duration = duration;
        }
        
        @Override
        public void update(float delta, Location location) {
            elapsed += delta;
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            final float width = SPLASH_REGION.getRegionWidth() * Settings.SCALE;
            final float height = SPLASH_REGION.getRegionHeight() * Settings.SCALE;
            
            Batch batch = renderer.getBatch();
            batch.begin();
            
            float alpha = MathUtils.lerp(1f, 0f, elapsed / duration);
            batch.setColor(1, 1, 1, alpha);
            batch.draw(SPLASH_REGION, position.x, position.y, width, height);
            
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        @Override
        public float getZ() {
            return Float.POSITIVE_INFINITY;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public boolean isFinished() {
            return elapsed > duration;
        }

        @Override
        public void dispose() {
        }
    }
}