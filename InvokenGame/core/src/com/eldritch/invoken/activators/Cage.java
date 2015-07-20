package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.box2d.AgentHandler;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class Cage extends CollisionActivator implements Damageable {
    private static final TextureRegion frame = GameScreen.ATLAS.findRegion("activators/cage");

    private final CageHandler handler;
    private HealthBar healthBar;
    private Optional<Agent> spawned = Optional.absent();

    public Cage(NaturalVector2 position) {
        super(position);
        handler = new CageHandler();
    }

    @Override
    public void update(float delta, Level level) {
        if (isAlive()) {
            if (spawned.isPresent() && !spawned.get().isAlive()) {
                spawned = Optional.absent();
            }
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = getPosition();
        float w = frame.getRegionWidth() * Settings.SCALE;
        float h = frame.getRegionHeight() * Settings.SCALE;

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x, position.y, w, h);
        batch.end();

        if (handler.isDamaged() && isAlive()) {
            // update and render health
            healthBar.update(this);
            healthBar.draw(getLevel().getCamera());
        }
    }

    @Override
    public void register(Level level) {
        super.register(level);
        this.healthBar = level.createHealthBar();
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        // NOTE: in order for this to be called, the TMX must have a TRANSIENT collider in the
        // collision layer and a "statics" or "dynamics" layer
        Vector2 center = getPosition().cpy().add(0.5f, 0.5f);
        entity.getBody().setTransform(center, 0);
        return handler.with(entity);
    }

    private void destroy() {
        for (InanimateEntity entity : handler.entities) {
            entity.finish();
        }
    }

    private class CageHandler extends DamageHandler {
        private static final float BASE_HEALTH = 25f;
        private float health = BASE_HEALTH;

        private final List<InanimateEntity> entities = new ArrayList<>();

        public CageHandler with(InanimateEntity entity) {
            entities.add(entity);
            return this;
        }

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
            // only vulnerable to thermal
            Damage damage = damager.getDamage();

            // do full damage from theraml, plus a significantly scaled down version of the full
            // magnitude, meaning thermal does extra damage, but all else is negligible
            health -= damage.getDamageOf(DamageType.THERMAL);
            health -= 0.1f * damage.getMagnitude();

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
