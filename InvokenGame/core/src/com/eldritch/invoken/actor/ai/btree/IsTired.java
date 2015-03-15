package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;

public class IsTired extends BooleanTask {
    @Override
    protected boolean check(Npc npc) {
        // alerted NPCs are not tired
        return !npc.getThreat().isAlerted() && npc.getFatigue().isTired();
    }
}
