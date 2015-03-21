package com.eldritch.invoken.util;

import com.eldritch.invoken.actor.type.Agent;

public class GenericDialogue {
    public static String forCrime(Agent agent) {
        return "Stop right there!";
    }
    
    public static String onCharge(Agent agent) {
        return "Get over here!";
    }
    
    public static String forDuress(Agent agent) {
        return "Put down your weapon!";
    }
    
    public static String forSuspiciousActivity(Agent agent) {
        return "I'm watching you, scum.";
    }
    
    public static String forHostility(Agent agent) {
        return "You will die!";
    }
    
    public static String thank(Agent agent) {
        return "Thank you.";
    }
    
    private GenericDialogue() {
        // singleton
    }
}
