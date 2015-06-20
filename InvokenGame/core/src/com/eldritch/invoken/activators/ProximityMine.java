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
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class ProximityMine extends ClickActivator implements ProximityActivator, TemporaryEntity {
    private static final Texture texture = GameScreen.getTexture(
            "sprite/activators/proximity-mine.png");
    
    private static final float RADIUS = 2f;
    
    private final ProximityCache proximityCache = new ProximityCache(3);
    private final Vector2 center;
    private final Detonation detonation;

    public ProximityMine(Vector2 position, Damage damage) {
        super(position.x - 0.5f, position.y - 0.5f, 1, 1);
        center = new Vector2(position.x, position.y);
        detonation = new Detonation(damage, center, RADIUS);
    }
    
    @Override
    public void update(float delta, Level level) {
        if (detonation.isActive()) {
            detonation.update(delta);
        } else {
            Agent source = detonation.damage.getSource();
            for (Agent agent : level.getActiveEntities()) {
                if (agent == source || agent.isFollowing(source)) {
                    // can't trip our own mine
                    // also prevent it for followers, otherwise placing them could be cumbersome
                    continue;
                }
                
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
    public void activate(Agent agent, Level level) {
        detonation.cancel();
    }
    
    @Override
    protected boolean canActivate(Agent agent, float x, float y) {
        return !detonation.isActive() && agent == detonation.damage.getSource();
    }

    @Override
    public boolean inProximity(Agent agent) {
        return proximityCache.inProximity(center, agent);
    }
    
    @Override
    public float getZ() {
        // always below
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public boolean isFinished() {
        return detonation.isFinished();
    }

    @Override
    public void dispose() {
        Agent source = detonation.damage.getSource();
        source.getLocation().removeActivator(this);
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
            return canceled || explosion.isAnimationFinished(elapsed);
        }
    }
}
