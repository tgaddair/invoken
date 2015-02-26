package com.eldritch.invoken.actor.pathfinding;

import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.HierarchicalPathFinder;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.encounter.layer.LocationMap;

public class PathManager {
    private final LocationGraph graph;
    private final Heuristic<LocationNode> heuristic = new ManhattanDistance();
    private final HierarchicalPathFinder<LocationNode> pathFinder;
    private final PathSmoother<LocationNode, Vector2> pathSmoother;

    public PathManager(LocationMap map) {
        graph = new LocationGraph(map);
        
        IndexedAStarPathFinder<LocationNode> levelPathFinder = new IndexedAStarPathFinder<LocationNode>(
                graph, true);
        pathFinder = new HierarchicalPathFinder<LocationNode>(graph, levelPathFinder);
        pathSmoother = new PathSmoother<LocationNode, Vector2>(new TiledRaycastCollisionDetector(
                graph));
    }
    
    public LocationGraph getGraph() {
        return graph;
    }

    public LocationGraphPath getPath(NaturalVector2 origin, NaturalVector2 target) {
        LocationNode startNode = graph.getNode(origin);
        LocationNode endNode = graph.getNode(target);
        
        LocationGraphPath path = new LocationGraphPath();
        boolean found = pathFinder.searchNodePath(startNode, endNode, heuristic, path);
        if (!found) {
            // no viable path
            return null;
        }
        
        // smooth the path
//        pathSmoother.smoothPath(path);
        
        return path;
    }
}
