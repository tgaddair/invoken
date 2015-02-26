package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.pathfinding.LocationGraphPath;
import com.eldritch.invoken.actor.pathfinding.LocationNode;
import com.eldritch.invoken.actor.pathfinding.PathManager;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class NavigatedSteerable extends BasicSteerable {
    private static final float MIN_DIST = 1f;
    private static final float WAIT_SECONDS = 3f;

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

    public void render(ShapeRenderer sr, Matrix4 projection) {
        if (path != null) {
            sr.setProjectionMatrix(projection);
            sr.begin(ShapeType.Filled);
            sr.setColor(0f, .3f, 1f, .4f);
            for (int i = pathIndex; i < path.getCount(); i++) {
                LocationNode node = path.get(i);
                sr.rect(node.position.x, node.position.y, 1f, 1f);
            }
            sr.end();
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
        // InvokenGame.logfmt("begin path: %s -> %s", npc.getNaturalPosition(), destination);
        path = pathManager.getPath(getNearestGround(npc.getNaturalPosition()), getNearestGround(destination));
        
        if (path == null) {
            InvokenGame.logfmt("Failed to find path: %s -> %s", npc.getNaturalPosition(), destination);
        }
        // InvokenGame.logfmt("found path: %s -> %s", npc.getNaturalPosition(), destination);
    }

    private NaturalVector2 getNearestGround(NaturalVector2 position) {
        if (pathManager.getGraph().isGround(position.x, position.y)) {
            // ideally, the nearest ground is the position itself
            return position;
        }
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                NaturalVector2 neighbor = NaturalVector2.of(position.x + dx, position.y + dy);
                if (pathManager.getGraph().isGround(neighbor.x, neighbor.y)) {
                    return position;
                }
            }
        }
        
        // still haven't found any ground
        // for now, throw an error
        throw new RuntimeException("No ground near " + position);
    }
}
