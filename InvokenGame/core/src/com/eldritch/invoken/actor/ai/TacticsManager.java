package com.eldritch.invoken.actor.ai;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Npc;

public class TacticsManager {
    private final Npc npc;
    private Augmentation chosen = null;
    
    public TacticsManager(Npc npc) {
        this.npc = npc;
    }
    
    public void update(float delta) {
    }
    
    public void setChosen(Augmentation chosen) {
        this.chosen = chosen;
    }
    
    public boolean hasChosen() {
        return chosen != null;
    }
    
    public Augmentation getChosen() {
        return chosen;
    }
}
