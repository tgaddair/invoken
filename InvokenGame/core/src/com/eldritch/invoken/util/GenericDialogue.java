package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.type.Agent;

public class GenericDialogue {
    public static String forCrime(Agent agent) {
        return "Stop right there!";
    }
    
    public static String onCharge(Agent agent) {
        return "Get over here!";
    }
    
    private GenericDialogue() {
        // singleton
    }
}
