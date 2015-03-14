package com.eldritch.invoken.actor.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.eldritch.invoken.actor.type.Agent;

public class ThreatMonitor<T extends Agent> {
    private static float FORGET_THRESHOLD_SECS = 10;
    
    private final T agent;
    private final Set<Agent> enemies = new HashSet<Agent>();
    private final Map<Agent, Float> lastSeen = new HashMap<Agent, Float>();
    
    public ThreatMonitor(T agent) {
        this.agent = agent;
    }
    
    public void update(float delta) {
        // remove agents we haven't seen in a while
        Iterator<Entry<Agent, Float>> observations = lastSeen.entrySet().iterator();
        while (observations.hasNext()) {
            Entry<Agent, Float> observation = observations.next();
            observation.setValue(observation.getValue() + delta);
            if (observation.getValue() > FORGET_THRESHOLD_SECS) {
                observations.remove();
            }
        }
        
        // add agents that are visible to our last seen cache
        for (Agent neighbor : agent.getVisibleNeighbors()) {
            lastSeen.put(neighbor, 0f);
        }
        
        // remove dead and forgotten enemies
        Iterator<Agent> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Agent enemy = enemyIterator.next();
            if (!enemy.isAlive() || !lastSeen.containsKey(enemy)) {
                enemyIterator.remove();
            }
        }
    }
    
    public boolean hostileTo(Agent other) {
        if (agent.isFollowing()) {
            // do not call hostileTo directly to avoid an infinite loop
            if (agent.getFollowed().getThreat().enemies.contains(other)) {
                // always hostile to those our leader is hostile to
                return true;
            }
        }
        return enemies.contains(other);
    }
    
    public void addEnemy(Agent enemy) {
        if (agent.isAlive()) {
            enemies.add(enemy);
        }
    }

    public int getEnemyCount() {
        return enemies.size();
    }

    public Iterable<Agent> getEnemies() {
        return enemies;
    }
    
    public boolean hasEnemy(Agent other) {
        return enemies.contains(other);
    }

    public boolean hasEnemies() {
        if (agent.isFollowing() && !agent.getFollowed().getThreat().enemies.isEmpty()) {
            // share enemies with leader
            return true;
        }
        return !enemies.isEmpty();
    }
    
    public T getAgent() {
        return agent;
    }
    
    public void clear() {
        enemies.clear();
    }
}
