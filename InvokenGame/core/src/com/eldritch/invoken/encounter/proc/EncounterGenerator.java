package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eldritch.invoken.proto.Locations.Encounter;
import com.google.common.base.Preconditions;

public class EncounterGenerator extends BspGenerator {
    private final List<Encounter> encounters = new ArrayList<Encounter>();

    public EncounterGenerator(int roomCount, List<Encounter> encounters) {
        super(roomCount);
        this.encounters.addAll(encounters);
        generateDependencyGraph(encounters);
    }

    private static void generateDependencyGraph(List<Encounter> encounters) {
        // scan through the list and pick out the origin
        List<EncounterNode> nodes = new ArrayList<EncounterNode>();
        EncounterNode origin = null;
        for (Encounter encounter : encounters) {
            EncounterNode node = new EncounterNode(encounter);
            if (node.isOrigin()) {
                origin = node;
            }
            nodes.add(node);
        }
        Preconditions.checkState(origin != null, "Origin must be provided in the encounter list");
        
        // add the origin as a key of all unlocked encounters
        Map<String, List<EncounterNode>> keys = new HashMap<String, List<EncounterNode>>();
        for (EncounterNode node : nodes) {
            if (!node.isOrigin() && !node.isLocked()) {
                node.addKey(origin);
                origin.addLock(node);
            }
            
            // add keys
            for (String key : node.getAvailableKeys()) {
                if (!keys.containsKey(key)) {
                    keys.put(key, new ArrayList<EncounterNode>());
                }
                keys.get(key).add(node);
            }
        }
        
        // finally, add normal dependencies
        for (EncounterNode node : nodes) {
            if (!node.isOrigin() && node.isLocked()) {
                for (EncounterNode key : keys.get(node.getLock())) {
                    node.addKey(key);
                    key.addLock(node);
                }
            }
        }
    }

    private static class EncounterNode {
        // encounters that unlock this one
        private final List<EncounterNode> keys = new ArrayList<EncounterNode>();
        
        // encounters that are unlocked by this one
        private final List<EncounterNode> locks = new ArrayList<EncounterNode>();
        
        private final Encounter encounter;

        public EncounterNode(Encounter encounter) {
            this.encounter = encounter;
        }
        
        public void addKey(EncounterNode key) {
            keys.add(key);
        }
        
        public void addLock(EncounterNode lock) {
            locks.add(lock);
        }
        
        public boolean isOrigin() {
            return encounter.getOrigin();
        }
        
        public boolean isLocked() {
            return encounter.hasRequiredKey() && !encounter.getRequiredKey().isEmpty();
        }
        
        public String getLock() {
            return encounter.getRequiredKey();
        }
        
        public List<String> getAvailableKeys() {
            return encounter.getAvailableKeyList();
        }
    }
}
