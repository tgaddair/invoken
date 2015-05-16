package com.eldritch.invoken.actor.ai.planning;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Npc;

public class Planner {
    private final Desire loiter;  // fallback behavior
    private final List<Desire> desires = new ArrayList<>();
    private final Npc owner;
    
    private Desire active;

    private Planner(Npc owner, List<Desire> desires) {
        this.owner = owner;
        this.loiter = new Loiter(owner);
        this.desires.addAll(desires);
        active = getGreatestDesire();
    }
    
    public void plan(float delta) {
        loiter.update(delta);
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
    
    private Desire getGreatestDesire() {
        Desire greatest = loiter;
        for (Desire desire : desires) {
            if (desire.getValue() > greatest.getValue()) {
                greatest = desire;
            }
        }
        return greatest;
    }

    public static Planner from(Npc npc) {
        List<Desire> desires = new ArrayList<>();
        desires.add(new Meander(npc));
        
        if (npc.isGuard()) {
            desires.add(new Guard(npc));
        }
        
        if (npc.getInfo().getProfession() == Profession.Assassin) {
            desires.add(new Hunt(npc));
        }
        
        return new Planner(npc, desires);
    }
}
