package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;

/**
 * Like Paralyzed, but without a visible effect to render.
 *
 */
public class Stunned extends Paralyzed {
    public Stunned(Agent agent, Agent target, float duration) {
        super(agent, target, duration);
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
    }
}
