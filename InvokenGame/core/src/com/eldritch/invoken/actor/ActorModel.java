package com.eldritch.invoken.actor;

import java.util.HashSet;
import java.util.Set;

import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.util.ActorMarshaller;

public class ActorModel {
    private final Player player;
    private final ActorMarshaller actorMarshaller = new ActorMarshaller();
    private final Set<String> deadNpcs = new HashSet<String>();
    
    public ActorModel(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }

//    public List<Npc> getActorsFor(ActorEncounter encounter) {
//        List<Npc> actors = new ArrayList<Npc>();
//        for (ActorScenario scenario : encounter.getScenarios()) {
//            String id = scenario.getActorId();
//            if (isAlive(id)) {
//                Npc actor = new Npc(getActor(id), this, scenario);
//                actors.add(actor);
//            }
//        }
//        return actors;
//    }

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
