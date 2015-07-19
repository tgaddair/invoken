package com.eldritch.invoken.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.invoken.box2d.AgentHandler.DamagingAgentHandler;
import com.eldritch.invoken.util.Damager;
import com.eldritch.invoken.util.Settings;

public class AreaOfEffect extends DamagingAgentHandler {
    private final Body body;
    
    public AreaOfEffect(World world) {
        this.body = createBody(world);
    }
    
    public void setup(AoeHandler delegate) {
        for (Fixture fixture : body.getFixtureList()) {
            fixture.getShape().setRadius(delegate.getRadius());
        }
        
        body.setTransform(delegate.getCenter(), 0);
        body.setActive(true);
        
        setup(delegate, delegate.getDamage());
    }
    
    @Override
    public Body getBody() {
        return body;
    }
    
    private Body createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);
        body.setUserData(this);
        createFixture(body, 1);
        return body;
    }
    
    private Fixture createFixture(Body body, float radius) {
        CircleShape circle = new CircleShape();
        circle.setRadius(1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        Filter filter = fixture.getFilterData();
        filter.categoryBits = Settings.BIT_BULLET;
        filter.maskBits = Settings.BIT_SHOOTABLE;
        fixture.setFilterData(filter);

        circle.dispose();
        return fixture;
    }
    
    public interface AoeHandler extends AgentHandler, Damager {
        Vector2 getCenter();
        
        float getRadius();
    }
}