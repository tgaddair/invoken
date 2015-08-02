package com.eldritch.invoken.actor.aug;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.type.TemporaryEntity.DefaultTemporaryEntity;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.box2d.OneWayWall;
import com.eldritch.invoken.effects.DamagedEnergy;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Condition;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public class Barrier extends SelfAugmentation {
    private static final int COST = 20;

    private static final String TOOLTIP = "Barrier\n\n"
            + "Absorbs up to 100 damage from incoming projectiles in the direction "
            + "the user is currently facing.  Sustained the shield reduces movement speed.\n\n"
            + "Mode: Sustained\n" + "Cost: 0";

    private static class Holder {
        private static final Barrier INSTANCE = new Barrier();
    }

    public static Barrier getInstance() {
        return Holder.INSTANCE;
    }

    private Barrier() {
        super("barrier");
    }

    @Override
    public Action getAction(Agent owner, Agent target) {
        return new BarrierAction(owner);
    }

    @Override
    public Action getAction(Agent owner, Vector2 target) {
        return new BarrierAction(owner);
    }

    @Override
    public boolean isValid(Agent owner, Agent target) {
        return true;
    }

    @Override
    public boolean isValid(Agent owner, Vector2 target) {
        return true;
    }

    @Override
    public int getCost(Agent owner) {
        return COST;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.getInventory().hasRangedWeapon() && owner.inFieldOfView(target)
                && owner.dst2(target) > 3 * 3 && target.isAimingAt(owner)) {
            return 5;
        }
        return 0;
    }

    @Override
    public String getTooltip() {
        return TOOLTIP;
    }

    public class BarrierAction extends AnimatedAction {
        public BarrierAction(Agent actor) {
            super(actor, Activity.Cast, Barrier.this);
        }

        @Override
        public void apply(Level level) {
            Vector2 direction = owner.getForwardVector();
            Vector2 position = owner.getPosition().cpy().add(direction);
            float theta = direction.angleRad();

            // having a barrier imposes an energy cost on the invocator
            final BarrierEntity barrier = new BarrierEntity(owner, position, direction, theta,
                    level);
            level.addEntity(barrier);
            level.addActivator(barrier);
            getOwner().addEffect(new DamagedEnergy(getOwner(), COST, new Condition() {
                private boolean finished = false;

                @Override
                public boolean satisfied() {
                    return barrier.isFinished() || finished;
                }

                @Override
                public void onReset(Level level) {
                    finished = true;
                }
            }));
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class BarrierEntity extends DefaultTemporaryEntity implements Activator {
        private static final TextureRegion REGION = new TextureRegion(
                GameScreen.getTexture("sprite/effects/barrier-stationary.png"));

        private static final float width = 0.5f;
        private static final float height = 2;

        private final Agent owner;
        private final Handler handler;
        private final World world;
        private final Body body;
        private final float thetaDegrees;
        private boolean finished = false;
        private boolean disposed = false;

        public BarrierEntity(Agent owner, Vector2 position, Vector2 direction, float theta,
                Level level) {
            super(position);
            this.owner = owner;
            this.handler = new Handler(direction);
            this.world = level.getWorld();
            this.body = createBox(position.x, position.y, width, height, theta, handler, world);
            this.thetaDegrees = theta * MathUtils.radiansToDegrees;
        }

        @Override
        public void update(float delta, Level level) {
            handler.heal(delta);
        }

        @Override
        public void render(float delta, OrthogonalTiledMapRenderer renderer) {
            Vector2 position = getPosition();
            Batch batch = renderer.getBatch();
            if (handler.isDamaged()) {
                Color c = batch.getColor();
                float a = handler.getHealth() / handler.getBaseHealth();
                batch.setColor(c.r, c.g * a, c.b * a, c.a);
            }

            batch.begin();
            float hx = width / 2;
            float hy = height / 2;
            batch.draw(REGION.getTexture(), // texture
                    position.x - hx, position.y - hy, // position
                    hx, hy, // origin
                    width, height, // size
                    1f, 1f, // scale
                    thetaDegrees, // rotation
                    0, 0, // texture position
                    REGION.getRegionWidth(), REGION.getRegionHeight(), // texture size
                    false, false); // flip
            batch.end();

            if (handler.isDamaged()) {
                batch.setColor(Color.WHITE);
            }
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void dispose() {
            if (!disposed) {
                // destroying a body more than once can cause an infinite loop
                world.destroyBody(body);
                disposed = true;
            }
        }

        private void destroyBy(Agent agent) {
            finished = true;
        }

        @Override
        public boolean click(Agent agent, Level level, float x, float y) {
            if (agent != owner) {
                return false;
            }

            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.testPoint(x, y)) {
                    activate(agent, level);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void activate(Agent agent, Level level) {
            if (agent == owner) {
                destroyBy(owner);
            }
        }

        @Override
        public void register(Level level) {
        }

        @Override
        public void register(List<InanimateEntity> entities) {
        }

        private class Handler extends DamageHandler implements OneWayWall {
            private static final float HEAL_RATE = 5f;
            private static final float BASE_HEALTH = 50f;

            private final OneWayWall wallDelegate;
            private float health = BASE_HEALTH;

            public Handler(Vector2 normal) {
                wallDelegate = new OneWayWallImpl(normal);
            }

            public void heal(float delta) {
                if (isDamaged()) {
                    health += delta * HEAL_RATE;
                    health = Math.min(health, getBaseHealth());
                }
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
                if (!hasContact(damager.getDirection())) {
                    return false;
                }

                health -= damage.getMagnitude();
                if (health <= 0) {
                    destroyBy(damage.getSource());
                }
                return true;
            }

            @Override
            public boolean hasContact(Vector2 direction) {
                return wallDelegate.hasContact(direction);
            }
        }
    }

    private static Body createBox(float x0, float y0, float width, float height, float theta,
            DamageHandler handler, World world) {
        PolygonShape box = new PolygonShape();
        float hx = width / 2;
        float hy = height / 2;
        Vector2 center = new Vector2(x0, y0);
        box.setAsBox(hx, hy, center, theta);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyType.StaticBody;
        groundBodyDef.position.set(0, 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.filter.groupIndex = 0;

        Body body = world.createBody(groundBodyDef);
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(handler);

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_SHIELD;
        filter.maskBits = Settings.BIT_ANYTHING;
        fixture.setFilterData(filter);

        box.dispose();

        return body;
    }
}
