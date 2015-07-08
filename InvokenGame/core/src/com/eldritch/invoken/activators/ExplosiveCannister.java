package com.eldritch.invoken.activators;

import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.location.Bullet;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Settings;

public class ExplosiveCannister extends CollisionActivator {
	public ExplosiveCannister(NaturalVector2 position) {
	    super(position);
	}
	
    @Override
    protected AgentHandler getCollisionHandler() {
        return new BulletHandler();
    }
	
	private class BulletHandler implements AgentHandler {
        @Override
        public boolean handle(Agent agent) {
            return false;
        }

        @Override
        public boolean handle(Object userData) {
            if (userData instanceof Bullet) {
                Bullet bullet = (Bullet) userData;
                System.out.println("bullet!");
                return true;
            }
            return false;
        }

        @Override
        public short getCollisionMask() {
            return Settings.BIT_ANYTHING;
        }
    }
}
