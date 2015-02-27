package com.eldritch.invoken.encounter;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.eldritch.invoken.actor.AgentHandler;
import com.eldritch.invoken.actor.type.Agent;

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
        if (handler == null || handled == null) {
            return;
        }

        if (handler instanceof AgentHandler) {
            System.out.println("begin collision");
            AgentHandler agentHandler = (AgentHandler) handler;
            if (handled instanceof Agent) {
                Agent agent = (Agent) handled;
                agentHandler.handle(agent);
            } else {
                // generic handler
                agentHandler.handle();
            }
        }
    }
}
