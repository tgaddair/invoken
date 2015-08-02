package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.box2d.AreaOfEffect;
import com.eldritch.invoken.box2d.AreaOfEffect.AoeHandler;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Detonation implements TemporaryEntity {
    private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
            "sprite/effects/explosion.png", 256, 256);
    
    private final Animation explosion;
    final Damage damage;
    private final Vector2 position;
    private final float radius;
    
    private Level level = null;
    private AreaOfEffect aoe = null;
    private boolean active = false;
    private float elapsed = 0;
    private boolean canceled = false;
    
    public Detonation(Damage damage, Vector2 position, float radius) {
        this(damage, position, radius, explosionRegions);
    }
    
    public Detonation(Damage damage, Vector2 position, float radius, TextureRegion[] regions) {
        this.damage = damage;
        this.position = position;
        this.radius = radius;
        this.explosion = new Animation(0.1f, regions);
    }
    
    public void detonate() {
        if (canceled) {
            // too late
            return;
        }
        
        Level level = damage.getSource().getLocation();
        apply(level);
        active = true;
    }
    
    public void cancel() {
        canceled = true;
    }

    private void apply(Level level) {
        this.level = level;
        this.aoe = new AreaOfEffect(level.getWorld());
        aoe.setup(new DetonationHandler(position, damage, radius));
    }
    
    @Override
    public void update(float delta, Level level) {
        elapsed += delta;
        if (!active) {
            detonate();
        } else {
            if (getProgress() > 0.5) {
                aoe.getBody().setActive(false);
            }
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // render the explosion
        float width = radius * 2;
        float height = radius * 2;
        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(explosion.getKeyFrame(elapsed), position.x - width / 2, position.y
                - height / 2, width, height);
        batch.end();
    }
    
    @Override
    public void renderOverlay(float delta, OrthogonalTiledMapRenderer renderer) {
    }
    
    public float getProgress() {
        return elapsed / explosion.getAnimationDuration();
    }
    
    @Override
    public float getZ() {
        return 0;
    }
    
    @Override
    public boolean inOverlay() {
        return false;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void dispose() {
        level.getWorld().destroyBody(aoe.getBody());
    }
    
    public boolean isActive() {
        return active;
    }
    
    public boolean isFinished() {
        return canceled || explosion.isAnimationFinished(elapsed);
    }
    
    public Agent getSource() {
        return damage.getSource();
    }
    
    public static class DetonationHandler extends DefaultAgentHandler implements AoeHandler {
        private final Vector2 center;
        private final Damage damage;
        private final float radius;
        
        public DetonationHandler(Vector2 center, Damage damage, float radius) {
            this.center = center;
            this.damage = damage;
            this.radius = radius;
        }
        
        @Override
        public boolean handle(Agent agent) {
            Vector2 direction = agent.getPosition().cpy().sub(center).nor();
            agent.applyForce(direction.scl(500));
            agent.addEffect(new Stunned(damage.getSource(), agent, 0.2f));
            agent.addEffect(new Bleed(agent, damage, center));
            return false;
        }
        
        @Override
        public boolean handle(Object userData) {
            if (userData instanceof DamageHandler) {
                DamageHandler handler = (DamageHandler) userData;
                handler.handle(this);
                return true;
            }
            return false;
        }
        
        @Override
        public Damage getDamage() {
            return damage;
        }
        
        @Override
        public Vector2 getDirection() {
            return Vector2.Zero;
        }
        
        @Override
        public Vector2 getCenter() {
            return center;
        }
        
        @Override
        public float getRadius() {
            return radius;
        }
    }
}