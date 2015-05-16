package com.eldritch.invoken.actor.ai.planning;

import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.aug.Ping;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;

public class Hunt extends AbstractDesire {
    private static final float DURATION = 5f;
    
    private float elapsed = 0;
    
    public Hunt(Npc owner) {
        super(owner);
    }

    @Override
    public void activeUpdate(float delta) {
        elapsed += delta;
    }
    
    @Override
    public boolean act() {
        if (getPrey() == null || elapsed > DURATION) {
            if (!ping()) {
                // failed to ping, so we cannot proceed
                return false;
            }
            elapsed = 0;
        }

        Agent target = owner.getLastSeen().getTarget();
        if (target != null && owner.isEnemy(target)) {
            Pursue.act(owner);
            return true;
        }
        return false;
    }

    @Override
    public float getValue() {
        return 0.5f;
    }
    
    private Agent getPrey() {
        return owner.getLastSeen().getTarget();
    }

    private boolean ping() {
        PreparedAugmentations augs = owner.getInfo().getAugmentations();
        if (augs.isPrepared(Ping.getInstance())) {
            if (augs.use(Ping.getInstance())) {
                return true;
            }
        }
        return false;
    }
}
