package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Npc;

public class IsIntimidated extends BooleanTask {
    @Override
    protected boolean check(Npc npc) {
        // either we've been facing down our enemy for too long or we're really close
        if (npc.getInventory().hasRangedWeapon()) {
            return npc.getIntimidation().isExpended() || isTooClose(npc);
        } else if (npc.getInventory().hasMeleeWeapon()) {
            // melee attackers are not intimidated
            return false;
        } else {
            // unarmed attackers are always intimidated
            return true;
        }
    }

    private boolean isTooClose(Npc npc) {
        return npc.hasTarget() && npc.hasLineOfSight(npc.getTarget())
                && npc.getTarget().isAimingAt(npc) && npc.dst2(npc.getTarget()) < 10;
    }
}
