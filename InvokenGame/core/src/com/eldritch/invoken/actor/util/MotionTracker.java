package com.eldritch.invoken.actor.util;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager;

public class MotionTracker {
    private static final float FOOTSTEP_DELAY_SECS = Settings.FRAME_DURATION * 4;

    private final SoundManager soundManager = InvokenGame.SOUND_MANAGER;
    private final Agent agent;

    private MotionState state = MotionState.Moving;
    private float nextFootstep = 0;
    private int stepSequence = 0;

    public MotionTracker(Agent agent) {
        this.agent = agent;
    }

    public void update(float delta) {
        Vector2 velocity = agent.getVelocity();

        // clamp the velocity to 0 if it's < 1, and set the state to standing
        if ((Math.abs(velocity.x) < .01 && Math.abs(velocity.y) < .01) || agent.isParalyzed()) {
            state = MotionState.Standing;
        } else if (Math.abs(velocity.x) > .1 || Math.abs(velocity.y) > .1) {
            // only update direction if we are going pretty fast
            Agent target = agent.getTarget();
            if (target == null || target == agent) {
                // update the current animation based on the maximal velocity
                // component
                if (!agent.actionInProgress()) {
                    // don't update the direction if we're currently performing an action
                    agent.setDirection(agent.getDominantDirection(velocity.x, velocity.y));
                }
            }
            state = MotionState.Moving;
        }

        // react to state
        if (isStanding()) {
            nextFootstep = 0;
        } else {
            nextFootstep -= delta;
            if (nextFootstep < 0) {
                nextFootstep = FOOTSTEP_DELAY_SECS;
                stepSequence = soundManager.playAtPoint(agent.getWalkingSound(), stepSequence,
                        agent.getPosition(), 0.25f);
            }
        }
    }

    public boolean isStanding() {
        return state == MotionState.Standing;
    }

    private enum MotionState {
        Standing, Moving
    }
}
