package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.Location;

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
    
    public class WeaponSentry implements TemporaryEntity {
        @Override
        public void update(float delta, Location location) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            // TODO Auto-generated method stub
        }

        @Override
        public float getZ() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Vector2 getPosition() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isFinished() {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
