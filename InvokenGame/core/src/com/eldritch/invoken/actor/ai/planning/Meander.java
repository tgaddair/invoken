package com.eldritch.invoken.actor.ai.planning;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.eldritch.invoken.actor.ai.btree.BooleanTask;
import com.eldritch.invoken.actor.ai.btree.IsTired;
import com.eldritch.invoken.actor.ai.btree.LowerWeapon;
import com.eldritch.invoken.actor.ai.btree.SuccessTask;
import com.eldritch.invoken.actor.ai.btree.Wander;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.Npc;

public class Meander extends AbstractDesire {
    private final BehaviorTree<Npc> tree;
    private boolean active = false;
    
    public Meander(Npc owner) {
        super(owner);
        tree = new BehaviorTree<Npc>(createBehavior(), owner);
    }

    @Override
    public boolean act() {
        // active will be flipped to true if all the preconditions on the tree are satisfied
        active = false;
        tree.step();
        return active;
    }

    @Override
    public float getValue() {
        return 1.0f - owner.getFatigue().getPercent();
    }
    
    private static class CanWander extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return !npc.inDialogue();
        }
    }
    
    private class SetActive extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Meander.this.active = true;
        }
    }

    private Task<Npc> createBehavior() {
        Sequence<Npc> wanderSequence = new Sequence<>();
        wanderSequence.addChild(new CanWander());
        wanderSequence.addChild(new Invert<>(new IsTired()));
        wanderSequence.addChild(new LowerWeapon());
        wanderSequence.addChild(new SetActive());
        wanderSequence.addChild(new Wander());
        return wanderSequence;
    }
}
