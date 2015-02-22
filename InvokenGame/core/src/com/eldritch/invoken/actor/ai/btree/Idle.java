package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Idle extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        entity.setBehavior(SteeringMode.Default);
        entity.getFatigue().use(-Npc.STEP);
        entity.setTask(getClass().getSimpleName());
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
