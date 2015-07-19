package com.eldritch.invoken.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.box2d.DamageHandler;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public abstract class Shield extends BasicEffect {
    private static final float MAX_DAMAGE = 100f;
    private static final float V_PENALTY = 1;
    private static final float ENERGY_COST = 20f;

    private final Augmentation aug;
    private final Map<Activity, Map<Direction, Animation>> animations = AnimationUtils
            .getHumanAnimations("sprite/effects/shield.png");
    private final Color color = new Color(Color.WHITE);
    // private final ProjectileHandler handler = new ShieldProjectileHandler();

    private float strength = MAX_DAMAGE;

    public Shield(Agent actor, Augmentation aug) {
        super(actor);
        this.aug = aug;
    }

    @Override
    public void doApply() {
        // target.addProjectileHandler(handler);
        target.getInfo().changeMaxEnergy(-ENERGY_COST);
        target.addVelocityPenalty(V_PENALTY); // shielding slows down the caster
        createHandlers(target);
    }

    @Override
    public void dispel() {
        // target.removeProjectileHandler(handler);
        target.getInfo().getAugmentations().removeSelfAugmentation(aug);
        target.getInfo().changeMaxEnergy(ENERGY_COST);
        target.addVelocityPenalty(-V_PENALTY);
        target.toggleOff(Shield.class);
        destroyHandlers();
    }

    @Override
    public boolean isFinished() {
        return !getTarget().isToggled(Shield.class) || strength <= 0;
    }

    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        Vector2 position = target.getRenderPosition();
        float width = target.getWidth();
        float height = target.getHeight();

        Animation animation = animations.get(target.getLastActivity()).get(target.getDirection());
        TextureRegion frame = animation.getKeyFrame(target.getLastStateTime());

        Batch batch = renderer.getBatch();
        batch.begin();
        batch.setColor(color);
        batch.draw(frame, // frame
                position.x - width / 2, position.y - height / 2, // position
                width / 2, height / 2, // origin
                width, height, // size
                1f, 1f, // scale
                0); // direction
        batch.setColor(Color.WHITE);
        batch.end();
    }

    protected void damage(float magnitude) {
        strength = Math.max(strength - magnitude, 0);
        float fraction = strength / MAX_DAMAGE;
        color.set(color.r, color.g * fraction, color.b * fraction,
                MathUtils.lerp(0.9f, 1f, fraction));
    }

    protected abstract void createHandlers(Agent owner);

    protected abstract void destroyHandlers();

    public static class FixedShield extends Shield {
        private final Map<Direction, Body> bodies = new HashMap<>();
        private final List<Joint> joints = new ArrayList<>();
        private Direction lastDirection;

        public FixedShield(Agent actor, Augmentation aug) {
            super(actor, aug);
        }

        @Override
        protected void update(float delta) {
            Direction direction = target.getDirection();
            if (direction != lastDirection) {
                lastDirection = direction;
                for (Entry<Direction, Body> entry : bodies.entrySet()) {
                    short categoryBits = entry.getKey() == direction ? Settings.BIT_SHIELD
                            : Settings.BIT_NOTHING;
                    for (Fixture fixture : entry.getValue().getFixtureList()) {
                        Filter filter = fixture.getFilterData();
                        filter.categoryBits = categoryBits;
                        fixture.setFilterData(filter);
                    }
                }
            }
        }

        @Override
        protected void createHandlers(Agent owner) {
            float r = owner.getBodyRadius();
            bodies.put(Direction.Right, createBody(owner, Direction.Right, new Vector2(r, 0)));
            bodies.put(Direction.Up, createBody(owner, Direction.Up, new Vector2(0, r)));
            bodies.put(Direction.Left, createBody(owner, Direction.Left, new Vector2(-r, 0)));
            bodies.put(Direction.Down, createBody(owner, Direction.Down, new Vector2(0, -r)));
        }

        @Override
        protected void destroyHandlers() {
            unweld();
            for (Body body : bodies.values()) {
                target.getLocation().getWorld().destroyBody(body);
            }
        }

        private Body createBody(Agent owner, Direction direction, Vector2 position) {
            float radius = owner.getBodyRadius();
            CircleShape shape = new CircleShape();
            shape.setPosition(position);
            shape.setRadius(radius);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 0.5f;
            fixtureDef.friction = 0.5f;
            fixtureDef.restitution = 0.1f;
            fixtureDef.filter.groupIndex = 0;

            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(owner.getBody().getPosition());
            bodyDef.type = BodyType.DynamicBody;
            Body body = owner.getLocation().getWorld().createBody(bodyDef);

            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(new FixedShieldHandler(owner, direction));

            // collision filters
            Filter filter = fixture.getFilterData();
            filter.categoryBits = Settings.BIT_SHIELD; // hits nothing
            filter.maskBits = Settings.BIT_BULLET; // hit by bullets
            fixture.setFilterData(filter);

            // weld the new body to the agent body
            weld(owner.getBody(), body);

            shape.dispose();
            return body;
        }

        private void weld(Body bodyA, Body bodyB) {
            WeldJointDef def = new WeldJointDef();

            def.collideConnected = false;
            Vector2 worldCoordsAnchorPoint = bodyA.getWorldPoint(new Vector2(0.0f, 0.0f));

            def.bodyA = bodyA;
            def.bodyB = bodyB;

            def.localAnchorA.set(def.bodyA.getLocalPoint(worldCoordsAnchorPoint));
            def.referenceAngle = def.bodyB.getAngle() - def.bodyA.getAngle();

            def.initialize(def.bodyA, def.bodyB, worldCoordsAnchorPoint);

            joints.add(target.getLocation().getWorld().createJoint(def));
        }

        private void unweld() {
            for (Joint joint : joints) {
                target.getLocation().getWorld().destroyJoint(joint);
            }
        }

        private class FixedShieldHandler extends DamageHandler {
            public FixedShieldHandler(Agent owner, Direction direction) {
            }

            @Override
            public boolean handle(Damager damager) {
                damage(damager.getDamage().getMagnitude());
                return true;
            }
        }
    }

    public static class RotatingShield extends Shield {
        private Body body;

        public RotatingShield(Agent actor, Augmentation aug) {
            super(actor, aug);
        }

        @Override
        protected void update(float delta) {
            body.setTransform(target.getWeaponSentry().getPosition(), 0);
        }

        @Override
        protected void createHandlers(Agent owner) {
            body = createBody(target);
        }

        @Override
        protected void destroyHandlers() {
            target.getLocation().getWorld().destroyBody(body);
        }

        private Body createBody(Agent owner) {
            float radius = owner.getBodyRadius();
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(0, 0));
            shape.setRadius(radius);

            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(owner.getBody().getPosition());
            bodyDef.type = BodyType.DynamicBody;
            Body body = owner.getLocation().getWorld().createBody(bodyDef);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.isSensor = true;
            fixtureDef.filter.groupIndex = 0;

            Fixture fixture = body.createFixture(fixtureDef);
            // fixture.setUserData(this); // allow callbacks to owning Agent

            // collision filters
            Filter filter = fixture.getFilterData();
            filter.categoryBits = Settings.BIT_SHIELD; // hits nothing
            filter.maskBits = Settings.BIT_BULLET; // hit by bullets
            fixture.setFilterData(filter);

            shape.dispose();
            return body;
        }
    }
}
