package com.eldritch.invoken.activators;

import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.type.InanimateEntity.InanimateEntityListener;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public abstract class CollisionActivator extends BasicActivator implements InanimateEntityListener {
	public CollisionActivator(NaturalVector2 position) {
	    super(position);
	}
	
	@Override
	public void update(float delta, Level level) {
	}
	
	@Override
    public void activate(Agent agent, Level level) {
    }
	
	@Override
    public void register(List<InanimateEntity> entities) {
	    for (InanimateEntity entity : entities) {
	        entity.addListener(this);
	    }
    }
	
	@Override
	public void onBodyCreation(Body body) {
	    for (Fixture fixture : body.getFixtureList()) {
	        Filter filter = fixture.getFilterData();
	        filter.categoryBits = Settings.BIT_OBSTACLE;
	        fixture.setFilterData(filter);
	        fixture.setUserData(getCollisionHandler());
	    }
	}
	
	protected abstract AgentHandler getCollisionHandler();
}
