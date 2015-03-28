package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.eldritch.invoken.actor.type.Npc;

public class Tasks {
    @SafeVarargs
    public static Sequence<Npc> sequence(Task<Npc>... tasks) {
        Sequence<Npc> sequence = new Sequence<Npc>();
        for (Task<Npc> task : tasks) {
            sequence.addChild(task);
        }
        return sequence;
    }
    
    @SafeVarargs
    public static Selector<Npc> selector(Task<Npc>... tasks) {
        Selector<Npc> selector = new Selector<Npc>();
        for (Task<Npc> task : tasks) {
            selector.addChild(task);
        }
        return selector;
    }
    
    private Tasks() {}
}
