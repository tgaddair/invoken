package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class SeekCover extends AbstractCombatTask {
    @Override
    public void run(Npc npc) {
        // entity.getHide().setTarget(entity.getLastSeen());
        npc.getHide().setTarget(npc.getLocation().getPlayer());
        npc.getHide().setEnabled(true);
        npc.getEvade().setTarget(npc.getLocation().getPlayer());
        npc.getEvade().setEnabled(true);

        // update target enemy
        fillTargets(npc);
        Agent target = selectBestTarget(npc);

        // update our target
        npc.setTarget(target);
        if (target != null && target.isAlive()) {
            // entity.getHide().setTarget(target);
            // entity.getEvade().setTarget(target);
        } else {
            // entity.getHide().setTarget(entity.getLastSeen());
            // entity.getEvade().setTarget(entity.getLastSeen());
        }
        npc.getEvade().setEnabled(npc.hasLineOfSight(npc.getLocation().getPlayer()));
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
