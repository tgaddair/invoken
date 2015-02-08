package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Pursue extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        entity.getPursue().setTarget(entity.getLastSeen());
        entity.setBehavior(SteeringMode.Pursue);
        
//        if (entity.getPosition().dst2(lastSeen) < 1)
        
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
