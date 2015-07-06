package com.eldritch.invoken.location;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.util.BulletHandler;

public class LocationContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) {
            return;
        }

        checkHandle(fa, fb);
        checkHandle(fb, fa);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) {
            return;
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private void checkHandle(Fixture fa, Fixture fb) {
        Object handler = fa.getUserData();
        Object handled = fb.getUserData();
        if (handler == null) {
            // cannot handle without a handler
            return;
        }

        if (handler instanceof BulletHandler) {
            System.out.println("bullet handler");
            BulletHandler bulletHandler = (BulletHandler) handler;
            handle(bulletHandler, handled);
        }
        if (handler instanceof AgentHandler) {
            AgentHandler agentHandler = (AgentHandler) handler;
            handle(agentHandler, handled);
        } else if (handler instanceof Agent) {
            Agent agent = (Agent) handler;
            if (agent.hasCollisionDelegate()) {
                // delegate collisions
                handle(agent.getCollisionDelegate(), handled);
            }
        }
    }
    
    private void handle(BulletHandler bulletHandler, Object handled) {
        if (handled != null && handled instanceof Bullet) {
            Bullet bullet = (Bullet) handled;
            bulletHandler.handle(bullet);
        }
    }
    
    private void handle(AgentHandler agentHandler, Object handled) {
        if (handled != null && handled instanceof Agent) {
            Agent agent = (Agent) handled;
            if (agent.isAlive()) {
                // only collide with living agents
                agentHandler.handle(agent);
            }
        } else {
            // generic handler
            agentHandler.handle(handled);
        }
    }
}
