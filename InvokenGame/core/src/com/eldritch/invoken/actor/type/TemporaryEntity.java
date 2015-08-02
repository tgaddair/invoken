package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.Entity;
import com.eldritch.invoken.location.Level;

public interface TemporaryEntity extends Entity {
    boolean isFinished();
    
    void dispose();
    
    public static class DefaultTemporaryEntity implements TemporaryEntity {
        private final Vector2 position = new Vector2();
        
        public DefaultTemporaryEntity(Vector2 position) {
            this.position.set(position);
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        }

        @Override
        public void renderOverlay(float delta, OrthogonalTiledMapRenderer renderer) {
        }

        @Override
        public float getZ() {
            return position.y;
        }

        @Override
        public boolean inOverlay() {
            return false;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        @Override
        public void update(float delta, Level level) {
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public void dispose() {
        }
    }
}
