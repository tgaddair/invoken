package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.effects.Bleed;
import com.eldritch.invoken.effects.Stunned;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class ProximityMine extends ClickActivator implements ProximityActivator, TemporaryEntity {
    private static final Texture texture = GameScreen.getTexture(
            "sprite/activators/proximity-mine.png");
    
    private static final float RADIUS = 2f;
    
    private final ProximityCache proximityCache = new ProximityCache(3);
    private final Vector2 center;
    private final Detonation detonation;

    public ProximityMine(NaturalVector2 position, Damage damage) {
        super(position);
        center = new Vector2(position.x + 0.5f, position.y + 0.5f);
        detonation = new Detonation(damage, center, RADIUS);
    }
    
    @Override
    public void update(float delta, Location location) {
        if (detonation.isActive()) {
            detonation.update(delta);
        } else {
            for (Agent agent : location.getActiveEntities()) {
                if (inProximity(agent)) {
                    detonation.detonate();
                    break;
                }
            }
        }
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (detonation.isActive()) {
            detonation.render(renderer);
        } else {
            Vector2 position = getPosition();
            Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(texture, position.x, position.y, getWidth(), getHeight());
            batch.end();
        }
    }

    @Override
    public void activate(Agent agent, Location location) {
        detonation.detonate();
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }

    @Override
    public boolean isFinished() {
        return detonation.isFinished();
    }

    @Override
    public void dispose() {
    }

    private static class Detonation {
        private static final TextureRegion[] explosionRegions = GameScreen.getMergedRegion(
                "sprite/effects/explosion.png", 256, 256);
        
        private final Animation explosion = new Animation(0.1f, explosionRegions);
        private final Damage damage;
        private final Vector2 position;
        private final float radius;
        
        private boolean active = false;
        private float elapsed = 0;
        
        public Detonation(Damage damage, Vector2 position, float radius) {
            this.damage = damage;
            this.position = position;
            this.radius = radius;
        }
        
        public void detonate() {
            Location location = damage.getSource().getLocation();
            for (Agent neighbor : location.getActiveEntities()) {
                apply(neighbor);
            }
            active = true;
        }

        private void apply(Agent agent) {
            if (agent.inRange(position, radius)) {
                Vector2 direction = agent.getPosition().cpy().sub(position).nor();
                agent.applyForce(direction.scl(500));
                agent.addEffect(new Stunned(damage.getSource(), agent, 0.2f));
                agent.addEffect(new Bleed(agent, damage));
            }
        }
        
        public void update(float delta) {
            elapsed += delta;
        }

        public void render(OrthogonalTiledMapRenderer renderer) {
            // render the explosion
            float width = radius * 2;
            float height = radius * 2;
            Batch batch = renderer.getBatch();
            batch.begin();
            batch.draw(explosion.getKeyFrame(elapsed), position.x - width / 2, position.y
                    - height / 2, width, height);
            batch.end();
        }
        
        public boolean isActive() {
            return active;
        }
        
        public boolean isFinished() {
            return explosion.isAnimationFinished(elapsed);
        }
    }
}
