package com.eldritch.invoken.activators;

import java.util.List;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.BasicLocatable;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;

public abstract class BasicActivator extends BasicLocatable implements Activator {
    public BasicActivator(NaturalVector2 position) {
        super(position);
    }
    
    public BasicActivator(float x, float y) {
        super(new Vector2(x, y));
    }
    
    @Override
    public boolean click(Agent agent, Level level, float x, float y) {
        return false;
    }
    
    @Override
    public void register(Level level) {
    }
    
    @Override
    public void register(List<InanimateEntity> entities) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    }
    
    @Override
    public float getZ() {
        return position.y;
    }
}
