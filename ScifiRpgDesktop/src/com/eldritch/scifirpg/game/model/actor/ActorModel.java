package com.eldritch.scifirpg.game.model.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.scifirpg.game.util.ActorMarshaller;
import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;

public class ActorModel {
    private final Player player;
    private final ActorMarshaller actorMarshaller = new ActorMarshaller();
    private final Set<String> deadNpcs = new HashSet<>();
    
    public ActorModel(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }

    public List<Npc> getActorsFor(ActorEncounter encounter) {
        List<Npc> actors = new ArrayList<>();
        for (ActorScenario scenario : encounter.getScenarios()) {
            String id = scenario.getActorId();
            if (isAlive(id)) {
                Npc actor = new Npc(getActor(id), this, scenario);
                actors.add(actor);
            }
        }
        return actors;
    }

    public NonPlayerActor getActor(String id) {
        return actorMarshaller.readAsset(id);
    }
    
    public boolean isAlive(String id) {
        return !deadNpcs.contains(id);
    }
    
    public void markDead(String actorId) {
        deadNpcs.add(actorId);
    }
}
