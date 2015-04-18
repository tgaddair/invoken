package com.eldritch.invoken.location;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Settings;

public class Bullet implements AgentHandler {
    private final Body body;
    private AgentHandler delegate;
    
    public Bullet(World world) {
        this.body = createBody(world);
    }
    
    public void setup(AgentHandler handler, Vector2 position, Vector2 velocity) {
        delegate = handler;
        body.setTransform(position, 0);
        body.setLinearVelocity(velocity);
        body.setActive(true);
    }
    
    public void setActive(boolean active) {
        body.setActive(active);
    }
    
    public Vector2 getPosition() {
        return body.getPosition();
    }
    
    public Vector2 getVelocity() {
        return body.getLinearVelocity();
    }
    
    public void setVelocity(Vector2 velocity) {
        body.setLinearVelocity(velocity);
    }

    private Body createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.bullet = true;  // move fast, so prevent speeding through walls
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_PHYSICAL;
        filter.maskBits = Settings.BIT_SHOOTABLE;
        fixture.setFilterData(filter);
        
        circle.dispose();
        
        return body;
    }

    @Override
    public boolean handle(Agent agent) {
        return delegate.handle(agent);
    }

    @Override
    public boolean handle() {
        return delegate.handle();
    }
}