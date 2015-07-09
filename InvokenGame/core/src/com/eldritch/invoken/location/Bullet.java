package com.eldritch.invoken.location;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.eldritch.invoken.actor.AgentHandler.ProjectileAgentHandler;
import com.eldritch.invoken.util.Settings;

public class Bullet extends ProjectileAgentHandler {
    private final Body body;

    public Bullet(World world) {
        this.body = createBody(world);
    }
    
    @Override
    public Body getBody() {
        return body;
    }
    
    private Body createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.bullet = true; // move fast, so prevent speeding through walls
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
        filter.categoryBits = Settings.BIT_BULLET;
        filter.maskBits = Settings.BIT_SHOOTABLE;
        fixture.setFilterData(filter);

        circle.dispose();

        return body;
    }
}
