package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Npc;

public class LowerWeapon extends LeafTask<Npc> {
    @Override
    public void run(Npc entity) {
        entity.setAiming(false);
        entity.getInfo().getAugmentations().unprepareAll();
        success();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
