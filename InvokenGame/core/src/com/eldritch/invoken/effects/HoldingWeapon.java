package com.eldritch.invoken.effects;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.items.RangedWeapon;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.Location;

public class HoldingWeapon extends BasicEffect {
    public HoldingWeapon(Agent agent) {
        super(agent);
    }

    @Override
    public void doApply() {
        target.getLocation().addEntity(new WeaponSentry());
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
    
    public class WeaponSentry implements TemporaryEntity {
        private final Vector2 position = new Vector2();
        private final Vector2 direction = new Vector2();
        
        @Override
        public void update(float delta, Location location) {
            Vector2 origin = target.getRenderPosition();
            direction.set(target.getFocusPoint()).sub(origin).nor();
            position.set(origin.x + direction.x, origin.y + direction.y);
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            RangedWeapon weapon = target.getInventory().getRangedWeapon();
            weapon.render(position, direction, renderer);
        }

        @Override
        public float getZ() {
            return position.y;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public boolean isFinished() {
            return HoldingWeapon.this.isFinished();
        }
    }
}
