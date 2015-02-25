package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.ai.pfa.Heuristic;

public class ManhattanDistance implements Heuristic<LocationNode> {
    @Override
    public float estimate(LocationNode node, LocationNode endNode) {
        return node.position.mdst(endNode.position);
    }
}
