package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;

public class IsIntimidated extends BooleanTask {
    @Override
    protected boolean check(Npc npc) {
        // either we've been facing down our enemy for too long or we're really close
        return npc.getIntimidation().isExpended() 
                || npc.getPosition().dst2(npc.getLastSeen().getPosition()) < 10;
    }
}
