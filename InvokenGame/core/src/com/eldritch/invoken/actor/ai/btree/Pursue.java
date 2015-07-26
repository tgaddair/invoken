package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Pursue extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        act(entity);
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
    
    public static void act(Npc entity) {
        entity.getPursue().setTarget(entity.getLastSeen().getNavpoint());
        entity.setBehavior(SteeringMode.Pursue);
        entity.setTask(Pursue.class.getSimpleName());
    }
}
