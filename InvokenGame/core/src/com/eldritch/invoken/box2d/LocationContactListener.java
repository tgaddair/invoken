package com.eldritch.invoken.box2d;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
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

        checkHandleEnd(fa, fb);
        checkHandleEnd(fb, fa);
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
    
    private void checkHandleEnd(Fixture fa, Fixture fb) {
        Object handler = fa.getUserData();
        Object handled = fb.getUserData();
        if (handler == null) {
            // cannot handle without a handler
            return;
        }

        if (handler instanceof AgentHandler) {
            AgentHandler agentHandler = (AgentHandler) handler;
            handleEnd(agentHandler, handled);
        } else if (handler instanceof Agent) {
            Agent agent = (Agent) handler;
            if (agent.hasCollisionDelegate()) {
                // delegate collisions
                handleEnd(agent.getCollisionDelegate(), handled);
            }
        }
    }
    
    private void handleEnd(AgentHandler agentHandler, Object handled) {
        if (handled != null && handled instanceof Agent) {
            Agent agent = (Agent) handled;
            if (agent.isAlive()) {
                // only collide with living agents
                agentHandler.handleEnd(agent);
            }
        } else {
            // generic handler
            agentHandler.handleEnd(handled);
        }
    }
}
