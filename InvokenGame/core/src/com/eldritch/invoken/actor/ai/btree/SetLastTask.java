package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;

public class SetLastTask extends SuccessTask {
    private final String taskName;
    
    public SetLastTask(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void doFor(Npc entity) {
        entity.setTask(taskName);
    }
}
