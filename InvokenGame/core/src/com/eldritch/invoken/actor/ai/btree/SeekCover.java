package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class SeekCover extends AbstractCombatTask {
    @Override
    public void run(Npc npc) {
        npc.getHide().setTarget(npc.getLastSeen());
        npc.getEvade().setTarget(npc.getLastSeen());
        npc.setBehavior(SteeringMode.Evade);
//        npc.getEvade().setEnabled(npc.hasLineOfSight(npc.getLocation().getPlayer()));
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
