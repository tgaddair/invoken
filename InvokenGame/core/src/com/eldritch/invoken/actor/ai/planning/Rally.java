package com.eldritch.invoken.actor.ai.planning;

import com.eldritch.invoken.actor.ai.btree.Pursue;
import com.eldritch.invoken.actor.ai.planning.Desire.AbstractDesire;
import com.eldritch.invoken.actor.type.BasicLocatable;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.location.NaturalVector2;

/**
 * Rallies agents to a choke point in a hallway.  Even better if it's on the critical path.
 */
public class Rally extends AbstractDesire {
    private static final float MAX_MDST = 15f;
    
    private final NaturalVector2 target;
    private final Locatable destination;

    public Rally(Npc owner) {
        super(owner);
        
        // find the nearest, or best, rally point
        NaturalVector2 current = owner.getCellPosition();
        NaturalVector2 bestPoint = null;
        int mdst = Integer.MAX_VALUE;
        for (NaturalVector2 point : owner.getLocation().getRallyPoints()) {
            if (point.mdst(current) < mdst) {
                bestPoint = point;
                mdst = point.mdst(current);
            }
        }
        
        if (bestPoint == null) {
            // fall back to current
            bestPoint = current;
        }

        target = bestPoint;
        destination = new BasicLocatable(bestPoint);
    }

    @Override
    public float getValue() {
        return Math.min(owner.getCellPosition().mdst(target) / MAX_MDST, 1f);
    }

    @Override
    public boolean act() {
        owner.getLastSeen().setPosition(destination);
        Pursue.act(owner);
        return true;
    }
}
