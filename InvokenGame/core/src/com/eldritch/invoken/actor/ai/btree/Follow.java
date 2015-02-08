package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Follow extends LeafTask<Npc> {
    @Override
    public void start(Npc entity) {
        if (entity.isFollowing()) {
            entity.getArrive().setTarget(entity.getFollowed());
            entity.getArrive().setEnabled(true);
        }
    }

    @Override
    public void run(Npc entity) {
        if (!entity.isFollowing()) {
            // lost our target
            fail();
            return;
        }
        
        Agent followed = entity.getFollowed();
        if (followed != entity.getArrive().getTarget()) {
            // new target
            entity.getArrive().setTarget(followed);
        }
        running();
    }
    
    @Override
    public void end(Npc entity) {
        entity.getArrive().setEnabled(false);
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }

}
