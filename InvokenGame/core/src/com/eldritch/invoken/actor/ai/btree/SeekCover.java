package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class SeekCover extends LeafTask<Npc> {
    @Override
    public void run(Npc npc) {
        npc.getHide().setTarget(npc.getLastSeen());
        npc.getEvade().setTarget(npc.getLastSeen());
        npc.setBehavior(SteeringMode.Evade);
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
