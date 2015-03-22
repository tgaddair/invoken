package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Npc.SteeringMode;

public class Idle extends SuccessTask {
    @Override
    public void doFor(Npc entity) {
        entity.setBehavior(SteeringMode.Default);
        entity.getFatigue().use(-Npc.STEP);
        entity.setTask(getClass().getSimpleName());
    }
}
