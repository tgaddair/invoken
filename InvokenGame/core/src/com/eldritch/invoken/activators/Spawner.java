package com.eldritch.invoken.activators;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.DamageHandler;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public class Spawner extends CollisionActivator implements Damageable {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/spawner").split(32, 32));
    
    private final SpawnerHandler handler;
    
    private Level level = null;
    private HealthBar healthBar = null;

    public Spawner(NaturalVector2 position) {
        super(position);
        handler = new SpawnerHandler();
    }
    
    @Override
    public void register(Level level) {
        this.level = level;
        this.healthBar = level.createHealthBar();
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = handler.getHealth() > 0 ? regions[0] : regions[1];
        Vector2 position = getPosition();

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, frame.getRegionWidth() * Settings.SCALE,
                frame.getRegionHeight() * Settings.SCALE);
        batch.end();
        
        if (handler.isDamaged() && isAlive()) {
            // update and render health
            healthBar.update(this);
            healthBar.draw(level.getCamera());
        }
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        return handler;
    }
    
    private void destroy() {
    }

    private class SpawnerHandler extends DamageHandler {
        private static final float BASE_HEALTH = 100f;
        private float health = BASE_HEALTH;
        
        public boolean isDamaged() {
            return getHealth() < getBaseHealth();
        }

        public float getBaseHealth() {
            return BASE_HEALTH;
        }

        public float getHealth() {
            return health;
        }

        @Override
        public boolean handle(Damager damager) {
            System.out.println("damage!");
            Damage damage = damager.getDamage();
            health -= damage.getMagnitude();
            System.out.println("health: " + health);
            if (health <= 0) {
                destroy();
            }
            return true;
        }
    }

    @Override
    public float getBaseHealth() {
        return handler.getBaseHealth();
    }

    @Override
    public float getHealth() {
        return handler.getHealth();
    }

    @Override
    public boolean isAlive() {
        return handler.getHealth() > 0;
    }

    @Override
    public void setHealthIndicator(Vector3 worldCoords) {
        Vector2 position = getPosition();
        worldCoords.set(position.x, position.y + 1, 0);
    }
}
