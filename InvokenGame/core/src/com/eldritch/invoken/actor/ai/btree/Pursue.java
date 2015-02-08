package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Pursue extends AbstractCombatTask {
    @Override
    public void start(Npc entity) {
        entity.getSeek().setTarget(entity.getLastSeen());
        entity.getSeek().setEnabled(true);
    }
    
    @Override
    public void run(Npc entity) {
        Vector2 lastSeen = entity.getLastSeen().getPosition();
        
        // update target enemy
        fillTargets(entity);
        Agent target = selectBestTarget(entity);
        
        // update our target
        entity.setTarget(target);
        if (target != null && target.isAlive()) {
            // found target
            success();
        } else if (entity.getPosition().dst2(lastSeen) < 1) {
            // found last seen, but no target to be found
            fail();
        }
        
        // still pursuing
        running();
    }
    
    @Override
    public void end(Npc entity) {
        entity.getSeek().setEnabled(false);
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return task;
    }
}
