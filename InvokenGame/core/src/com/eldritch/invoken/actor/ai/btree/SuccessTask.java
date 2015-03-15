package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;

public abstract class SuccessTask extends LeafTask<Npc> {
    @Override
    public void run(Npc object) {
        doFor(object);
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
    
    protected abstract void doFor(Npc npc);
}
