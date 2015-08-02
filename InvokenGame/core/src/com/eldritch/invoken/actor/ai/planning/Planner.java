package com.eldritch.invoken.actor.ai.planning;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.planning.Desire.DesireFactory;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.proto.Locations.DesireProto;

public class Planner {
    private final List<Desire> desires = new ArrayList<>();
    private final Npc owner;
    
    private Desire active;

    private Planner(Npc owner, List<Desire> desires) {
        this.owner = owner;
        this.desires.addAll(desires);
        this.desires.add(new Loiter(owner));
        
        active = getGreatestDesire();
        active.start();
    }
    
    public void plan(float delta) {
        for (Desire desire : desires) {
            desire.update(delta);
        }
    }
    
    public boolean act() {
        owner.setTask("Desire " + active.getClass().getSimpleName());
        boolean success = active.act();
        if (!success) {
            // choose a new desire to act upon
            setActiveDesire(getGreatestDesire());
        }
        return success;
    }
    
    public List<Desire> getDesires() {
        return desires;
    }
    
    private Desire getGreatestDesire() {
        Desire greatest = desires.get(0);
        for (int i = 1; i < desires.size(); i++) {
            Desire desire = desires.get(i);
            if (desire.getValue() > greatest.getValue()) {
                greatest = desire;
            }
        }
        return greatest;
    }
    
    private void setActiveDesire(Desire desire) {
        active.stop();
        active = desire;
        active.start();
    }

    public static Planner from(Npc npc) {
        List<Desire> desires = new ArrayList<>();
        
        // universal desires
        desires.add(new Buff(npc));
        if (npc.isGuard()) {
            desires.add(new Guard(npc));
        }
        desires.add(new Follow(npc));
        
        // check actor scenario for the various routines
        if (npc.hasScenario() && !npc.getScenario().getDesireList().isEmpty()) {
            for (DesireProto proto : npc.getScenario().getDesireList()) {
                desires.add(DesireFactory.fromProto(proto, npc));
            }
        } else {
            // default desires
            if (npc.getInfo().getProfession() == Profession.Assassin) {
                desires.add(new Hunt(npc));
            }
//            desires.add(new Patrol(npc));
            desires.add(new Rally(npc));
            desires.add(new Meander(npc));
        }
        
        return new Planner(npc, desires);
    }
}
