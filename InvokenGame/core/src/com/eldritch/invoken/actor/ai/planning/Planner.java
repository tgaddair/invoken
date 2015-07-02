package com.eldritch.invoken.actor.ai.planning;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Npc;

public class Planner {
    private final List<Desire> desires = new ArrayList<>();
    private final Npc owner;
    
    private Desire active;

    private Planner(Npc owner, List<Desire> desires) {
        this.owner = owner;
        this.desires.addAll(desires);
        this.desires.add(new Loiter(owner));
        active = getGreatestDesire();
    }
    
    public void plan(float delta) {
        for (Desire desire : desires) {
            desire.update(delta);
        }
    }
    
    public boolean act() {
        boolean success = active.act();
        if (!success) {
            // choose a new desire to act upon
            active = getGreatestDesire();
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

    public static Planner from(Npc npc) {
        List<Desire> desires = new ArrayList<>();
        desires.add(new Buff(npc));
        
        if (npc.isGuard()) {
            desires.add(new Guard(npc));
        }
        
        if (npc.getInfo().getProfession() == Profession.Assassin) {
            desires.add(new Hunt(npc));
        }
        
        // TODO: check actor scenario for the Patrol routine
        desires.add(new Patrol(npc));
        
        desires.add(new Meander(npc));
        return new Planner(npc, desires);
    }
}
