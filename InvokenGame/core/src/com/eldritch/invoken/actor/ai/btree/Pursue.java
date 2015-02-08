package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Pursue extends AbstractCombatTask {
    @Override
    public void run(Npc entity) {
        entity.getSeek().setTarget(entity.getLastSeen());
        entity.setBehavior(SteeringMode.Pursue);
        
//        if (entity.getPosition().dst2(lastSeen) < 1)
        
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
