package com.eldritch.invoken.actor.ai.btree;

import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.location.NaturalVector2;

public class IsTrapped extends BooleanTask {
    @Override
    protected boolean check(Npc npc) {
        return npc.getInventory().hasMeleeWeapon() && isTrapped(npc);
    }

    public static boolean isTrapped(Agent agent) {
        // check our surrounding cells
        int obstacles = 0;
        NaturalVector2 current = agent.getNaturalPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                // 3 obstacles means we're trapped
                if (agent.getLocation().isObstacle(current.x + dx, current.y + dy)) {
                    obstacles++;
                }
            }
        }

        return obstacles >= 3;
    }
}
