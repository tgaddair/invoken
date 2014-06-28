package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.encounter.Location;
import com.eldritch.invoken.encounter.NaturalVector2;

public class AvoidWallsBehavior extends AbstractSteeringBehavior {
    private final Vector2 steeringForce = new Vector2();
    
    public AvoidWallsBehavior(Npc npc) {
        super(npc);
    }

    @Override
    public Vector2 getForce(Vector2 target, Location location) {
        Npc npc = getNpc();
        Vector2 source = npc.getPosition();

        // this will hold an index into the vector of walls
        NaturalVector2 closestWall = null;
        float minOvershoot = 0;

        // examine each feeler in turn
        int startX = (int) Math.floor(Math.min(source.x, target.x));
        int startY = (int) Math.floor(Math.min(source.y, target.y));
        int endX = (int) Math.ceil(Math.max(source.x, target.x));
        int endY = (int) Math.ceil(Math.max(source.y, target.y));
        Array<Rectangle> tiles = location.getTiles(startX, startY, endX, endY);

        Vector2 tmp = new Vector2();
        Vector2 tmpDisplace = new Vector2();
        for (Rectangle tile : tiles) {
            float r = 0.5f; // all rectangles are unit length
            Vector2 center = tile.getCenter(tmp);

            float overshoot = Intersector.intersectSegmentCircleDisplace(source, target, center, r,
                    tmpDisplace);
            if (overshoot < Float.POSITIVE_INFINITY) {
                // intersection detected
                if (closestWall == null || overshoot < minOvershoot) {
                    closestWall = NaturalVector2.of((int) tile.x, (int) tile.y);
                    minOvershoot = overshoot;
                }
            }
        }

        steeringForce.set(Vector2.Zero);
        if (closestWall != null) {
            steeringForce.set(npc.getReverseHeading().scl(minOvershoot));
        }
        return steeringForce;
    }

    @Override
    public double getPriority() {
        return 100;
    }
}
