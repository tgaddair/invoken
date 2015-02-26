package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;

public class LocationGraphPath extends DefaultGraphPath<LocationNode> implements
        SmoothableGraphPath<LocationNode, Vector2> {

    private Vector2 tmpPosition = new Vector2();

    /**
     * Returns the position of the node at the given index.
     * <p>
     * <b>Note that the same Vector2 instance is returned each time this method is called.</b>
     * 
     * @param index
     *            the index of the node you want to know the position
     */
    @Override
    public Vector2 getNodePosition(int index) {
        LocationNode node = nodes.get(index);
        return tmpPosition.set(node.position.x, node.position.y);
    }

    @Override
    public void swapNodes(int index1, int index2) {
        nodes.set(index1, nodes.get(index2));
    }

    @Override
    public void truncatePath(int newLength) {
        nodes.truncate(newLength);
    }
}