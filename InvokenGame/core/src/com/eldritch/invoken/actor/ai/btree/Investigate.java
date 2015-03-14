package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Investigate extends Sequence<Npc> {
    public Investigate() {
        addChild(new IsSuspicious());
        addChild(new Pursue());
    }
    
    private static class IsSuspicious extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.isSuspicious();
        }
    }
}
