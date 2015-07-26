package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class SeekCover extends LeafTask<Npc> {
    @Override
    public void run(Npc npc) {
        npc.getHide().setTarget(npc.getLastSeen().getNavpoint());
        npc.getEvade().setTarget(npc.getLastSeen().getNavpoint());
        npc.setBehavior(SteeringMode.Evade);
        npc.setTask(getClass().getSimpleName());
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
