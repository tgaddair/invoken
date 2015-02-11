package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;

public abstract class BooleanTask extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        if (check(entity)) {
            success();
        } else {
            fail();
        }
    }
    
    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
    
    protected abstract boolean check(Npc npc);
}
