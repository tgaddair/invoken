package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Npc;

public class IntimidationMonitor extends FuzzyMonitor {
    // base amount of intimidation (seconds sighting enemy) past which the NPC should flee
    private static final float INTIMIDATED = 2;

    public IntimidationMonitor(final Npc npc) {
        super(new PenaltyFunction() {
            @Override
            public float getPenalty() {
                // TODO: incorporate health, energy, and relative levels
                return 0;
            }
            
            @Override
            public float getLimit() {
                return ((float) Math.random() * INTIMIDATED) + 1;
            }
        });
    }
}
