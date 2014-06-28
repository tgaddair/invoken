package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;

public class SeekBehavior extends AbstractSteeringBehavior {
    public SeekBehavior(Npc agent) {
        super(agent);
    }
    
    @Override
    public Vector2 getForce(Location location) {
        Npc agent = getNpc();
        Vector2 position = agent.getPosition();
        Vector2 target = agent.getTarget().getPosition().cpy();
        Vector2 desired = target.sub(position).nor().scl(agent.getMaxVelocity());
        return desired.sub(agent.getVelocity());
    }
}
