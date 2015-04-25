package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Wander extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        entity.setTarget(null);
        entity.setBehavior(SteeringMode.Wander);
        entity.getFatigue().use(Npc.STEP);
        entity.setTask(getClass().getSimpleName());
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
