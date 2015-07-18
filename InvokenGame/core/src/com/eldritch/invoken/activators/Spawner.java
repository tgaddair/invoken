package com.eldritch.invoken.activators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.DamageHandler;
import com.eldritch.invoken.actor.aug.Burrow;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.util.Damageable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class Spawner extends CollisionActivator implements Damageable {
    private static final TextureRegion[] regions = GameScreen.getMergedRegion(GameScreen.ATLAS
            .findRegion("activators/spawner").split(32, 32));

    private final SpawnerHandler handler;
    private HealthBar healthBar;
    private Optional<Agent> spawned = Optional.absent();

    public Spawner(NaturalVector2 position) {
        super(position);
        handler = new SpawnerHandler();
    }

    @Override
    public void update(float delta, Level level) {
        if (isAlive()) {
            if (spawned.isPresent() && !spawned.get().isAlive()) {
                spawned = Optional.absent();
                spawn(level);
            }
        }
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        TextureRegion frame = isAlive() ? regions[0] : regions[1];
        Vector2 position = getPosition();
        float w = frame.getRegionWidth() * Settings.SCALE;
        float h = frame.getRegionHeight() * Settings.SCALE;

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.draw(frame, position.x - w / 2, position.y - h / 2, w, h);
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
        spawn(level);
    }

    private void spawn(Level level) {
        for (NaturalVector2 point : getRandomPoints()) {
            if (level.isGround(point)) {
                System.out.println("spawn!!! " + point);
                spawn(point, level);
                return;
            }
        }
    }

    private List<NaturalVector2> getRandomPoints() {
        List<NaturalVector2> points = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                points.add(NaturalVector2.of((int) position.x + dx, (int) position.y + dy));
            }
        }
        Collections.shuffle(points);
        return points;
    }

    private void spawn(NaturalVector2 point, Level level) {
        String id = Constants.CRAWLER;
        Agent agent = Npc.create(InvokenGame.ACTOR_READER.readAsset(id), point.x, point.y, level);
        spawned = Optional.of(agent);
        
        level.addAgent(agent);
        Burrow.addDust(agent);
    }

    @Override
    protected AgentHandler getCollisionHandler(InanimateEntity entity) {
        // NOTE: in order for this to be called, the TMX must have a TRANSIENT collider in the
        // collision layer and a "statics" or "dynamics" layer
        getPosition().set(entity.getPhysicalPosition());
        return handler.with(entity);
    }

    private void destroy() {
        for (InanimateEntity entity : handler.entities) {
            entity.finish();
        }
    }

    private class SpawnerHandler extends DamageHandler {
        private static final float BASE_HEALTH = 25f;
        private float health = BASE_HEALTH;

        private final List<InanimateEntity> entities = new ArrayList<>();

        public SpawnerHandler with(InanimateEntity entity) {
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
            health -= damage.getMagnitude();
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
