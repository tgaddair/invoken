package com.eldritch.invoken.actor.aug;

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
import com.eldritch.invoken.actor.aug.Augmentation.SelfAugmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.TemporaryEntity.DefaultTemporaryEntity;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.box2d.OneWayWall;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public class Barrier extends SelfAugmentation {
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
        return 20;
    }

    @Override
    public float quality(Agent owner, Agent target, Level level) {
        if (owner.getInventory().hasRangedWeapon() && owner.inFieldOfView(target)
                && owner.dst2(target) > 3 * 3) {
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
            level.addEntity(new BarrierEntity(position, direction, theta, level));
        }

        @Override
        public Vector2 getPosition() {
            return owner.getPosition();
        }
    }

    private static class BarrierEntity extends DefaultTemporaryEntity {
        private static final TextureRegion REGION = new TextureRegion(
                GameScreen.getTexture("sprite/effects/barrier-stationary.png"));
        
        private static final float width = 1;
        private static final float height = 2;

        private final Handler handler;
        private final World world;
        private final Body body;
        private final float thetaDegrees;
        private boolean finished = false;

        public BarrierEntity(Vector2 position, Vector2 direction, float theta, Level level) {
            super(position);
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
            batch.draw(REGION.getTexture(),  // texture
                    position.x - hx, position.y - hy,  // position
                    hx, hy,  // origin
                    width, height,  // size
                    1f, 1f,  // scale
                    thetaDegrees,  // rotation
                    0, 0,  // texture position
                    REGION.getRegionWidth(), REGION.getRegionHeight(),  // texture size
                    false, false);  // flip
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
            world.destroyBody(body);
        }
        
        private void destroyBy(Agent agent) {
            finished = true;
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

    private static Body createBox(float x0, float y0, float width, float height, float theta, DamageHandler handler, World world) {
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
