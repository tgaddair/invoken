package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class Detonation implements TemporaryEntity {
    private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
            "sprite/effects/explosion.png", 256, 256);
    
    private final Animation explosion = new Animation(0.1f, explosionRegions);
    final Damage damage;
    private final Vector2 position;
    private final float radius;
    
    private boolean active = false;
    private float elapsed = 0;
    private boolean canceled = false;
    
    public Detonation(Damage damage, Vector2 position, float radius) {
        this.damage = damage;
        this.position = position;
        this.radius = radius;
    }
    
    public void detonate() {
        if (canceled) {
            // too late
            return;
        }
        
        Level level = damage.getSource().getLocation();
        for (Agent neighbor : level.getActiveEntities()) {
            apply(neighbor);
        }
        active = true;
    }
    
    public void cancel() {
        canceled = true;
    }

    private void apply(Agent agent) {
        if (agent.inRange(position, radius)) {
            Vector2 direction = agent.getPosition().cpy().sub(position).nor();
            agent.applyForce(direction.scl(500));
            agent.addEffect(new Stunned(damage.getSource(), agent, 0.2f));
            agent.addEffect(new Bleed(agent, damage));
        }
    }
    
    @Override
    public void update(float delta, Level level) {
        elapsed += delta;
        if (!active) {
            detonate();
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
    public float getZ() {
        return 0;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void dispose() {
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
}