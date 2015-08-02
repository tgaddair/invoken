package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.box2d.AgentHandler;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.location.ConnectedRoom;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.proto.Effects.DamageType;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.google.common.base.Optional;

public class Hideout extends CollisionActivator implements Damageable {
    private static final float RELATION_DELTA = 50f;

    private final Vector2 center = new Vector2();
    private final CageHandler handler;
    private HealthBar healthBar;
    private Optional<ConnectedRoom> room = Optional.absent();
    private Optional<Agent> captive = Optional.absent();
    private boolean pendingEnable = false;

    public Hideout(NaturalVector2 position) {
        super(position);
        center.set(getPosition()).add(0.5f, 0.5f);
        handler = new CageHandler();
    }

    @Override
    public void update(float delta, Level level) {
        if (pendingEnable) {
            if (captive.isPresent()) {
                captive.get().setEnabled(true);
            }
            pendingEnable = false;
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (isAlive()) {
            if (handler.isDamaged()) {
                // update and render health
                healthBar.update(this);
                healthBar.draw(getLevel().getCamera());
            }
        }
    }

    @Override
    public void register(Level level) {
        super.register(level);
        this.healthBar = level.createHealthBar();
        room = level.getRoomFor(NaturalVector2.of(center));
    }

    private void hide(Agent agent) {
        agent.setEnabled(false);
        captive = Optional.of(agent);
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        // NOTE: in order for this to be called, the TMX must have a TRANSIENT collider in the
        // collision layer and a "statics" or "dynamics" layer
        entity.getBody().setTransform(center, 0);
        return handler.with(entity);
    }

    private void destroyBy(Agent source) {
        // destroy physics
        for (InanimateEntity entity : handler.entities) {
            entity.finish();
        }

        // note that we can't actually change the enabled state of the captive here, because we
        // are in a Box2D update step, so we defer to the next update cycle
        // update the captive
        pendingEnable = true;
        if (captive.isPresent()) {
            // modify relations
            captive.get().changeRelation(source, RELATION_DELTA);
            if (room.isPresent()) {
                for (Agent resident : room.get().getResidents()) {
                    captive.get().changeRelation(resident, -RELATION_DELTA);
                }
            }
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
            Damage damage = damager.getDamage();
            health -= damage.getDamageOf(DamageType.PHYSICAL)
                    + damage.getDamageOf(DamageType.THERMAL);
            if (health <= 0) {
                destroyBy(damage.getSource());
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
