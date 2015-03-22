package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class SquadTactics extends Sequence<Npc> {
    private static final float SQUAD_COHESION = 2f;
    
    /**
     * Only applies to NPCs that are part of a squad. First we check that this condition is
     * satisfied, then we ensure that NPC either maintains sufficient proximity to the squad
     * leader.
     */
    public SquadTactics() {
        addChild(new HasSquad());
        addChild(new StrayedFromLeader());
        addChild(new SeekLeader());
        addChild(new Pursue());
    }
    
    private static class HasSquad extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return npc.hasSquad();
        }
    }
    
    private static class StrayedFromLeader extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            Agent leader = npc.getSquad().getLeader();
            if (leader == npc) {
                // cannot stray from oneself
                return false;
            }
            
            float r = SQUAD_COHESION;
            return npc.dst2(leader) > r * r;
        }
    }
    
    private static class SeekLeader extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Agent leader = npc.getSquad().getLeader();
            npc.setTarget(leader);
        }
    }
}
