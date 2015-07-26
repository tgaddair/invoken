package com.eldritch.invoken.actor.type;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.eldritch.invoken.actor.pathfinding.LocationGraphPath;
import com.eldritch.invoken.actor.pathfinding.LocationNode;
import com.eldritch.invoken.actor.pathfinding.PathManager;
import com.eldritch.invoken.actor.util.Locatable;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.NaturalVector2;
import com.google.common.collect.ImmutableList;

public abstract class NavigatedSteerable implements Locatable {
    private static final float MIN_DIST = 1f;
    private static final float WAIT_SECONDS = 3f;

    private final Agent npc;
    private final Vector2 lastSeen = new Vector2();
    private final Navpoint navpoint = new Navpoint();
    private Locatable target = null;
    private boolean arrived = false; // done navigating

    // path state
    private LocationGraphPath path = null;
    private float pathAge = 0;
    private int pathIndex = 0;

    public NavigatedSteerable(Agent npc, Level level) {
        this.npc = npc;
    }

    public void update(float delta) {
        if (arrived) {
            // don't update
            return;
        }

        if (path != null) {
            pathAge += delta;

            // if we're close to the current node, then move on to the next one
            if (npc.getPosition().dst2(path.getNodePosition(pathIndex)) <= MIN_DIST) {
                if (pathIndex + 1 < path.getCount()) {
                    // proceed to the next node
                    navpoint.setPosition(path.getNodePosition(++pathIndex));
                    pathAge = 0;
                } else {
                    // fallback to moving in a straight line
                    navpoint.setPosition(lastSeen);
                    resetPath();
                }
            }
        }

        // consider updating the path if it has gone stale
        updatePath();

        // make note of when we reach our navigation target
        if (target != null && npc.getPosition().dst2(lastSeen) < MIN_DIST) {
            arrived = true;
        }
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public boolean hasArrived() {
        return arrived;
    }

    public void render(ShapeRenderer sr, Matrix4 projection) {
        sr.setProjectionMatrix(projection);
        sr.begin(ShapeType.Filled);

        // draw the path
        if (path != null) {
            sr.setColor(0f, .3f, 1f, .4f);
            for (int i = pathIndex; i < path.getCount(); i++) {
                LocationNode node = path.get(i);
                sr.rect(node.position.x, node.position.y, 1f, 1f);
            }
        }

        // draw start end end nodes
        sr.setColor(Color.GREEN);
        sr.rect(npc.getNaturalPosition().x, npc.getNaturalPosition().y, 1f, 1f);

        if (target != null) {
            sr.setColor(Color.RED);
            sr.rect(target.getNaturalPosition().x, target.getNaturalPosition().y, 1f, 1f);
        }

        sr.end();
    }
    
    public void setLastLocation(Vector2 location) {
        lastSeen.set(location);
    }
    
    public Navpoint getNavpoint() {
        return navpoint;
    }
    
    @Override
    public Vector2 getPosition() {
        return lastSeen;
    }

    @Override
    public NaturalVector2 getNaturalPosition() {
        return NaturalVector2.of(lastSeen);
    }

    @Override
    public Vector2 getPhysicalPosition() {
        return lastSeen;
    }

    @Override
    public Iterable<Fixture> getFixtures() {
        return ImmutableList.of();
    }

    public void locate(Locatable target) {
        setPosition(target, true);
    }

    public void setPosition(Locatable target) {
        setPosition(target, false);
    }

    public void setPosition(Locatable target, boolean invalidate) {
        if (target != this.target || invalidate) {
            // invalidate the path
            resetPath();
            this.target = target;
            if (target != null) {
                this.lastSeen.set(target.getPosition());
            }
        }

        // new last seen point means new path
        arrived = false;
        updatePath();
    }

    protected void updatePath() {
        if (target == null) {
            // no path possible
            return;
        }

        if (npc.hasVisibilityTo(target)) {
            // we don't need pathfinding if we have line of sight
            lastSeen.set(target.getPosition());
            navpoint.setPosition(target.getPosition());
        } else if (npc.hasLineOfSight(lastSeen)) {
            navpoint.setPosition(lastSeen);
        } else {
            // only update the path if the new position is sufficiently different from the last we
            // computed a path for, and a certain amount of time has elapsed
            if (path == null
                    || (pathAge > WAIT_SECONDS && lastSeen.dst2(path.getNodePosition(pathIndex)) > MIN_DIST)) {
                resetPath();
                computePath(NaturalVector2.of(lastSeen));

                if (path != null && path.getCount() > 0) {
                    // begin at the first node in the path
                    navpoint.setPosition(path.getNodePosition(0));
                } else {
                    // pathfinding failed, so fallback to the default behavior
                    navpoint.setPosition(lastSeen);
                    resetPath();
                }
            }
        }
    }
    
    protected void setNavpoint(Vector2 position) {
        navpoint.setPosition(position);
    }

    public Locatable getTarget() {
        return target;
    }

    private void resetPath() {
        path = null;
        pathAge = 0;
        pathIndex = 0;
    }
    
    protected void setPath(LocationGraphPath path) {
        this.path = path;
    }
    
    protected final LocationGraphPath getPath() {
        return path;
    }
    
    protected Agent getOwner() {
        return npc;
    }
    
    protected abstract void computePath(NaturalVector2 destination);
    
    public abstract boolean hasPath();

    public static class AStarNavigatedSteerable extends NavigatedSteerable {
        private final PathManager pathManager;

        public AStarNavigatedSteerable(Agent npc, Level level) {
            super(npc, level);
            pathManager = level.getPathManager();
        }
        
        @Override
        protected void computePath(NaturalVector2 destination) {
            NaturalVector2 a = getNearestGround(getOwner().getNaturalPosition());
            NaturalVector2 b = getNearestGround(destination);
            if (a == null || b == null) {
                // no path possible
                return;
            }

            setPath(pathManager.getPath(a, b));
            // if (path == null) {
            // InvokenGame.logfmt("Failed to find path: %s -> %s", npc.getNaturalPosition(),
            // destination);
            // }
        }

        private NaturalVector2 getNearestGround(NaturalVector2 position) {
            if (pathManager.getGraph().isGround(position.x, position.y)) {
                // ideally, the nearest ground is the position itself
                return position;
            }

            for (int dx = 0; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    NaturalVector2 neighbor = NaturalVector2.of(position.x + dx, position.y + dy);
                    if (pathManager.getGraph().isGround(neighbor.x, neighbor.y)) {
                        return neighbor;
                    }
                }
            }

            // still haven't found any ground
            // for now, an error
            return null;
        }

        @Override
        public boolean hasPath() {
            return getPath() != null;
        }
    }
    
    public static class BasicNavigatedSteerable extends NavigatedSteerable {
        public BasicNavigatedSteerable(Agent npc, Level level) {
            super(npc, level);
        }

        @Override
        protected void computePath(NaturalVector2 destination) {
        }
        
        @Override
        protected void updatePath() {
            Locatable target = getTarget();
            if (target == null) {
                // no path possible
                return;
            }
            
            if (getOwner().hasVisibilityTo(target)) {
                // we don't need pathfinding if we have line of sight
                setLastLocation(target.getPosition());
                setNavpoint(target.getPosition());
            } else {
                setNavpoint(getPosition());
            }
        }

        @Override
        public boolean hasPath() {
            return true;
        }
    }
    
    public class Navpoint extends BasicSteerable {
    }
}
