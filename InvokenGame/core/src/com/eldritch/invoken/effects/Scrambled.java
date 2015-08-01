package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.screens.GameScreen;

public class Scrambled extends AnimatedEffect {
    private static final TextureRegion[] REGIONS = GameScreen.getMergedRegion(
            "sprite/effects/confused.png", 32, 32);

    private final float duration;
    
    public Scrambled(Agent agent, Agent target, float duration) {
        super(target, REGIONS, Animation.PlayMode.LOOP);
        this.duration = duration * getDurationScale(agent, target);
        System.out.println("duration: " + this.duration);
    }

    @Override
    public boolean isFinished() {
        return getStateTime() > duration;
    }

    @Override
    public void doApply() {
        getTarget().setConfused(true);
    }

    @Override
    public void dispel() {
        getTarget().setConfused(false);
    }

    protected float getDurationScale(Agent agent, Agent target) {
        return agent.getManipulationScale(target);
    }
}
