package com.eldritch.invoken.actor.ai.btree;

import com.badlogic.gdx.ai.btree.branch.Selector;
import com.eldritch.invoken.actor.type.Npc;

public class Patrol extends Selector<Npc> {
    public Patrol() {
//        addChild(new Follow());
        addChild(new Wander());
        addChild(new Idle());
    }
}
