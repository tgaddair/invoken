package com.eldritch.invoken.actor.ai.planning;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.ai.btree.BooleanTask;
import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.ai.btree.SuccessTask;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Follow extends AbstractDesire {
    private static final float MIN_DST2 = 5 * 5;
    
    private final BehaviorTree<Npc> tree;
    private boolean active = false;

    public Follow(Npc owner) {
        super(owner);
        tree = new BehaviorTree<Npc>(createBehavior(), owner);
    }
    
    @Override
    public boolean act() {
        // active will be flipped to true if all the preconditions on the tree
        // are satisfied
        active = false;
        tree.step();
        return active;
    }

    @Override
    public float getValue() {
        if (owner.hasSquad() && owner.getSquad().getLeader() != owner && canFollow(owner)) {
            return 1f;
        }
        return 0f;
    }

    private Task<Npc> createBehavior() {
        Sequence<Npc> sequence = new Sequence<>();
        sequence.addChild(new CanFollow());
        sequence.addChild(new SetActive());
        sequence.addChild(new FollowTask());
        sequence.addChild(new Pursue());
        return sequence;
    }
    
    private static class CanFollow extends BooleanTask {
        @Override
        protected boolean check(Npc npc) {
            return canFollow(npc);
        }
    }
    
    private class SetActive extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Follow.this.active = true;
        }
    }

    private static class FollowTask extends SuccessTask {
        @Override
        protected void doFor(Npc npc) {
            Agent followed = npc.getSquad().getLeader();
            npc.setTarget(followed);
        }
    }
    
    private static boolean canFollow(Npc npc) {
        Agent followed = npc.getSquad().getLeader();
        return npc.dst2(followed) > MIN_DST2;
    }
}
