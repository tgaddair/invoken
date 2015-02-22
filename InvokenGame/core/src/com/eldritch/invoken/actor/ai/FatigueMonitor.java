package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Npc;

public class FatigueMonitor extends FuzzyMonitor {
    private static final float TIRED = 10; // seconds of activity

    public FatigueMonitor(final Npc npc) {
        super(new PenaltyFunction() {
            @Override
            public float getPenalty() {
                return .001f * (1f - npc.getInfo().getEndurance());
            }
        });
    }

    public boolean isTired() {
        return isExpended();
    }

    @Override
    protected float getLimit() {
        return (float) Math.random() * TIRED;
    }
}
