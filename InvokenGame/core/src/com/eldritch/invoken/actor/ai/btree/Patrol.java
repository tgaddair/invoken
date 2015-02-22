package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.type.Npc;

public class Patrol extends Selector<Npc> {
    public Patrol() {
//        addChild(new Follow());
        
        Sequence<Npc> wanderSequence = new Sequence<Npc>();
        wanderSequence.addChild(new CanWander());
        wanderSequence.addChild(new Invert<Npc>(new IsTired()));
        wanderSequence.addChild(new LowerWeapon());
        wanderSequence.addChild(new Wander());
        
        addChild(wanderSequence);
        addChild(new Idle());
    }
    
    private static class CanWander extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.inDialogue();
        }
    }
}
