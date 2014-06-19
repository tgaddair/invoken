package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;

public abstract class BasicEffect implements Effect {
    protected final Agent target;
    private boolean applied = false;
    private float stateTime = 0;
    
    public BasicEffect(Agent target) {
        this.target = target;
    }
    
    @Override
    public void apply(float delta) {
        if (!applied) {
            doApply();
            applied = true;
        } else {
            update(delta);
        }
        stateTime += delta;
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    }
    
    protected Agent getTarget() {
        return target;
    }
    
    protected float getStateTime() {
        return stateTime;
    }
    
    protected boolean isApplied() {
        return applied;
    }
    
    protected abstract void doApply();
    
    protected abstract void update(float delta);
}
