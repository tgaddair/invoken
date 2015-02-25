package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.pathfinding.LocationGraphPath;
import com.eldritch.invoken.actor.pathfinding.PathManager;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class NavigatedSteerable extends BasicSteerable {
    private static final float MIN_DIST = 1f;
    private static final float WAIT_SECONDS = 1f;

    private final Npc npc;
    private final PathManager pathManager;
    private Agent target = null;

    // path state
    private LocationGraphPath path = null;
    private float pathAge = 0;
    private int pathIndex = 0;

    public NavigatedSteerable(Npc npc, Location location) {
        this.npc = npc;
        pathManager = location.getPathManager();
    }

    public void update(float delta) {
        if (path != null) {
            pathAge += delta;

            // if we're close to the current node, then move on to the next one
            if (npc.getPosition().dst2(path.getNodePosition(pathIndex)) < MIN_DIST) {
                if (path.getCount() < pathIndex + 1) {
                    // proceed to the next node
                    setPosition(path.getNodePosition(++pathIndex));
                } else {
                    // fallback to moving in a straight line
                    setPosition(target.getPosition());
                    resetPath();
                }
            }
        }
    }

    public void setPosition(Agent target) {
        if (target != this.target) {
            // invalidate the path
            resetPath();
            this.target = target;
        }

        if (npc.hasLineOfSight(target)) {
            // we don't need pathfinding if we have line of sight
            setPosition(target.getPosition());
        } else {
            // only update the path if the new position is sufficiently different from the last we
            // computed a path for, and a certain amount of time has elapsed
            if (path == null
                    || (target.getPosition().dst2(getPosition()) > MIN_DIST && pathAge > WAIT_SECONDS)) {
                resetPath();
                computePath(target.getNaturalPosition());

                if (path != null && path.getCount() > 0) {
                    // begin at the first node in the path
                    setPosition(path.getNodePosition(0));
                } else {
                    // pathfinding failed, so fallback to the default behavior
                    setPosition(target.getPosition());
                    resetPath();
                }
            }
        }
    }

    @Override
    public Vector2 getPosition() {
        return super.getPosition();
    }

    public Agent getTarget() {
        return target;
    }

    private void resetPath() {
        path = null;
        pathAge = 0;
        pathIndex = 0;
    }

    private void computePath(NaturalVector2 destination) {
        path = pathManager.getPath(npc.getNaturalPosition(), destination);
    }
}
