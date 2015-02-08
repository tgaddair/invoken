package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.eldritch.invoken.actor.type.Npc;

public class Combat extends LeafTask<Npc> {
    private BehaviorTree<Npc> behaviorTree = new BehaviorTree<Npc>(createBehavior());
    
    @Override
    public void start(Npc npc) {
        behaviorTree.setObject(npc);
    }
    
    @Override
    public void run(Npc npc) {
        if (!npc.isCombatReady()) {
            fail();
        }
        
        behaviorTree.step();
        running();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

    private Task<Npc> createBehavior() {
        Selector<Npc> selector = new Selector<Npc>();
//        selector.addChild(new SeekCover());
        selector.addChild(new Attack());
        selector.addChild(new Pursue());
        return selector;
    }
}
