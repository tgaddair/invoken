package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.type.Npc;

public class IntimidationMonitor extends FuzzyMonitor {
    // base amount of intimidation (seconds sighting enemy) past which the NPC should flee
    private static final float INTIMIDATED = 3;

    public IntimidationMonitor(final Npc npc) {
        super(INTIMIDATED, new PenaltyFunction() {
            @Override
            public float getPenalty() {
                // TODO: incorporate health, energy, and relative levels
                return 0;
            }
        });
    }

    public boolean isTired() {
        return isExpended();
    }
}
