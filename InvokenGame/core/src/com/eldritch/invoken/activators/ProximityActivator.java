package com.eldritch.invoken.activators;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.box2d.AgentHandler.DefaultAgentHandler;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public abstract class ProximityActivator extends BasicActivator {
    private final Vector2 center = new Vector2();
    private final Vector2 offset = new Vector2();
    private final float radius;
    private final Optional<Indicator> indicator;

    private final Set<Agent> proximityAgents = new HashSet<>();
    private final Set<Agent> lastProximityAgents = new HashSet<>();
    private final Set<Agent> triggerAgents = new HashSet<>();

    private Body sensor;
    private boolean proximityActive = false;

    public ProximityActivator(NaturalVector2 position, ProximityParams params) {
        this(position.x, position.y, params);
    }

    public ProximityActivator(float x, float y, ProximityParams params) {
        super(x, y);
        this.center.set(params.center);
        this.offset.set(params.offset);
        this.radius = params.radius;
        this.indicator = params.indicator;
    }

    @Override
    public final void update(float delta, Level level) {
        preUpdate(delta, level);
        
        // only change the state of the door if it differs from the current
        // state must click to unlock
        boolean hasProximity = !proximityAgents.isEmpty();
        if (shouldActivate(hasProximity)) {
            lastProximityAgents.removeAll(proximityAgents);
            triggerAgents.clear();
            triggerAgents.addAll(lastProximityAgents);

            if (onProximityChange(hasProximity, level)) {
                proximityActive = hasProximity;
            }
        }

        lastProximityAgents.clear();
        lastProximityAgents.addAll(proximityAgents);
        
        // delegate to subclasses
        postUpdate(delta, level);
    }
    
    protected void preUpdate(float delta, Level level) {
    }
    
    protected void postUpdate(float delta, Level level) {
    }
    
    @Override
    public void render(float delta, OrthogonalTiledMapRenderer renderer) {
        if (indicator.isPresent()) {
            indicator.get().render(delta, renderer, this);
        }
    }

    @Override
    public void activate(Agent agent, Level level) {
    }

    protected boolean shouldActivate(boolean hasProximity) {
        return hasProximity != proximityActive;
    }

    public boolean hasProximity(Agent agent) {
        return proximityAgents.contains(agent);
    }
    
    protected Iterable<Agent> getProximityAgents() {
        return proximityAgents;
    }

    protected Iterable<Agent> getTriggerAgents() {
        return triggerAgents;
    }

    protected abstract boolean onProximityChange(boolean hasProximity, Level level);

    @Override
    public Vector2 getPosition() {
        return center;
    }

    public Vector2 getRenderPosition() {
        return position;
    }

    public Vector2 getCenter() {
        return center;
    }

    protected Body getSensor() {
        return sensor;
    }

    @Override
    public final void register(Level level) {
        sensor = createSensor(level, offset);
        if (indicator.isPresent()) {
            indicator.get().register(level);
        }
        postRegister(level);
    }

    protected abstract void postRegister(Level level);

    private Body createSensor(Level level, Vector2 offset) {
        CircleShape shape = new CircleShape();
        shape.setPosition(offset);
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.groupIndex = 0;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(center);
        bodyDef.type = BodyType.StaticBody;
        Body body = level.getWorld().createBody(bodyDef);

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(new ProximityHandler());

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_INVISIBLE; // does not interrupt targeting
        filter.maskBits = Settings.BIT_AGENTS; // hit by agents
        fixture.setFilterData(filter);

        shape.dispose();
        return body;
    }

    private class ProximityHandler extends DefaultAgentHandler {
        @Override
        public boolean handle(Agent agent) {
            proximityAgents.add(agent);
            return true;
        }

        @Override
        public boolean handleEnd(Agent agent) {
            proximityAgents.remove(agent);
            return true;
        }
    }

    protected static class ProximityParams {
        private final Vector2 center;
        private final Vector2 offset;
        private final float radius;
        
        private Optional<Indicator> indicator = Optional.absent();

        private ProximityParams(Vector2 center, Vector2 offset, float radius) {
            this.center = center;
            this.offset = offset;
            this.radius = radius;
        }
        
        public ProximityParams withIndicator(Indicator indicator) {
            this.indicator = Optional.of(indicator);
            return this;
        }

        public static ProximityParams of(float x, float y, float width, float height) {
            return of(new Vector2(x + width / 2, y + height / 2));
        }

        public static ProximityParams of(Vector2 center) {
            return of(center, Vector2.Zero, 1.5f);
        }

        public static ProximityParams of(Vector2 center, Vector2 offset, float radius) {
            return new ProximityParams(center, offset, radius);
        }
    }

    public static class Indicator {
        private static final float W = 0.5f;
        private static final float H = 0.5f;

        private final Texture texture;
        private final Vector2 renderOffset = new Vector2();
        
        private Level level;

        public Indicator(Texture texture) {
            this(texture, Vector2.Zero);
        }

        public Indicator(Texture texture, Vector2 renderOffset) {
            this.texture = texture;
            this.renderOffset.set(renderOffset);
        }
        
        public void register(Level level) {
            this.level = level;
        }
        
        public float getX(ProximityActivator owner) {
            return owner.getRenderPosition().x + renderOffset.x - W / 2f;
        }
        
        public float getY(ProximityActivator owner) {
            return owner.getRenderPosition().y + renderOffset.y;
        }
        
        public float getWidth() {
            return W;
        }
        
        public float getHeight() {
            return H;
        }
        
        public Level getLevel() {
            return level;
        }

        public void render(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
            if (isActive(level, owner)) {
                preRender(delta, renderer, owner);
                
                float x = getX(owner);
                float y = getY(owner);

                Batch batch = renderer.getBatch();
                batch.begin();
                batch.draw(texture, x, y, W, H);
                batch.end();
                batch.setColor(Color.WHITE);
                
                postRender(delta, renderer, owner);
            }
        }
        
        protected void preRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
        }
        
        protected void postRender(float delta, OrthogonalTiledMapRenderer renderer,
                ProximityActivator owner) {
        }
        
        protected boolean isActive(Level level, ProximityActivator owner) {
            return owner.hasProximity(level.getPlayer());
        }
    }
}
