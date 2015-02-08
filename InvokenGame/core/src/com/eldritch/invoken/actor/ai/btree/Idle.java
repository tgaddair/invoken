package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.ai.StateValidator.TimedValidator;
import com.eldritch.invoken.actor.type.Npc;

public class Idle extends LeafTask<Npc> {
    @Override
    public void start(Npc entity) {
        entity.getStateMachine().setValidator(new TimedValidator((float) Math.random() * 10));
        entity.getWander().setEnabled(false);
    }

    @Override
    public void run(Npc entity) {
        if (entity.isFollowing()) {
            fail();
        } else if (!entity.inDialogue() && !entity.getStateMachine().getValidator().isValid()) {
            // move to wander
            success();
        }
        running();
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

}
