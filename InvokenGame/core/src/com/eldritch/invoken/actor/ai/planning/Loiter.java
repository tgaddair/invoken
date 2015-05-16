package com.eldritch.invoken.actor.ai.planning;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.eldritch.invoken.actor.ai.btree.Idle;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.Npc;

public class Loiter extends AbstractDesire {
    private final BehaviorTree<Npc> tree;
    
    public Loiter(Npc owner) {
        super(owner);
        tree = new BehaviorTree<Npc>(new Idle(), owner);
    }

    @Override
    public boolean act() {
        tree.step();
        return owner.getFatigue().isTired();
    }

    @Override
    public float getValue() {
        return owner.getFatigue().getPercent();
    }
}
