package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class FleeBehavior extends AbstractSteeringBehavior {
    public FleeBehavior(Npc agent) {
        super(agent);
    }
    
    @Override
    public Vector2 getForce(Vector2 target, Location location) {
        Npc agent = getNpc();
        Vector2 position = agent.getPosition().cpy();
        if (agent.getTarget() != null) {
        	target = agent.getTarget().getPosition();
        } else {
        	target = agent.getPosition();
        }
    	Vector2 desired = position.sub(target).nor().scl(agent.getMaxLinearSpeed());
        return desired.sub(agent.getVelocity());
    }
}
