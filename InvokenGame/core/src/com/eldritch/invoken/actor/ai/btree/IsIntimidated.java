package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;

public class IsIntimidated extends BooleanTask {
    @Override
    protected boolean check(Npc npc) {
        // either we've been facing down our enemy for too long or we're really close
        if (npc.getInventory().hasRangedWeapon()) {
            return npc.getIntimidation().isExpended() || isInDanger(npc);
        } else if (npc.getInventory().hasMeleeWeapon()) {
            // melee attackers are not intimidated
            return false;
        } else {
            // unarmed attackers are always intimidated
            return true;
        }
    }
    
    private boolean isInDanger(Npc npc) {
        if (!npc.hasTarget() || !npc.hasLineOfSight(npc.getTarget())) {
            return false;
        }
        return isTooClose(npc) || isTargeted(npc);
    }

    private boolean isTooClose(Npc npc) {
        return npc.dst2(npc.getTarget()) < 5;
    }

    private boolean isTargeted(Npc npc) {
        return npc.getTarget().isAimingAt(npc);
    }
}
