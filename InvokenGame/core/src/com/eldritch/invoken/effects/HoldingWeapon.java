package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.eldritch.invoken.actor.type.Agent;

public class HoldingWeapon extends BasicEffect {
    public HoldingWeapon(Agent agent) {
        super(agent);
    }

    @Override
    public void doApply() {
    }

    @Override
    public void dispel() {
    }

    @Override
    public boolean isFinished() {
        return !target.isToggled(HoldingWeapon.class);
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        // render weapon
        target.getInventory().getRangedWeapon().render(target, renderer);
    }
}
