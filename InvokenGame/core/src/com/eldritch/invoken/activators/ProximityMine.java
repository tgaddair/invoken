package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.effects.Detonation;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;

public class ProximityMine extends ClickActivator implements TemporaryEntity {
    private static final Texture texture = GameScreen.getTexture(
            "sprite/activators/proximity-mine.png");
    
    private static final float RADIUS = 2f;
    
    private final Vector2 center;
    private final Detonation detonation;

    public ProximityMine(Vector2 position, Damage damage) {
        super(position.x - 0.5f, position.y - 0.5f, 1, 1);
        center = new Vector2(position.x, position.y);
        detonation = new Detonation(damage, center, RADIUS);
    }
    
    @Override
    public void postUpdate(float delta, Level level) {
        if (detonation.isActive()) {
            detonation.update(delta, level);
        } else {
            Agent source = detonation.getSource();
            for (Agent agent : level.getActiveEntities()) {
                if (agent == source || agent.isFollowing(source)) {
                    // can't trip our own mine
                    // also prevent it for followers, otherwise placing them could be cumbersome
                    continue;
                }
                
                if (hasProximity(agent)) {
                    detonation.detonate();
                    break;
                }
            }
        }
    }
    
    @Override
    public void preRender(float delta, OrthogonalTiledMapRenderer renderer) {
        if (detonation.isActive()) {
            detonation.render(delta, renderer);
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
    protected boolean canActivate(Agent agent) {
        return !detonation.isActive() && agent == detonation.getSource();
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
        Agent source = detonation.getSource();
        source.getLocation().removeActivator(this);
    }

    @Override
    protected void postRegister(Level level) {
    }
}
