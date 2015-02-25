package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.ai.pfa.DefaultConnection;

public class LocationConnection extends DefaultConnection<LocationNode> {
    private static final float DIAGONAL_COST = (float) Math.sqrt(2);

    public LocationConnection(LocationNode fromNode, LocationNode toNode) {
        super(fromNode, toNode);
    }

    @Override
    public float getCost() {
        return getToNode().position.x != getFromNode().position.x
                && getToNode().position.y != getFromNode().position.y ? DIAGONAL_COST : 1;
    }
}
