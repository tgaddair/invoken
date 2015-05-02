package com.eldritch.invoken.util;

import java.util.List;

import com.eldritch.invoken.actor.type.Agent;
import com.google.common.collect.ImmutableList;

public class GenericDialogue {
    private static final List<String> HOSTILITY = ImmutableList.of(
            "You will die!", "You will pay with your blood!");
    
    public static String forFrontier(Agent speaker, Agent agent) {
        return "Turn back outsider. You're not welcome here.";
    }

    public static String forCrime(Agent speaker, Agent agent) {
        return "Stop right there!";
    }

    public static String onCharge(Agent speaker, Agent agent) {
        return "Get over here!";
    }

    public static String forDuress(Agent speaker, Agent agent) {
        return "Put down your weapon!";
    }
    
    public static String forSuspiciousActivity(Agent speaker, Agent agent) {
        return "I'm watching you, scum.";
    }
    
    public static String enterCalm(Agent speaker, Agent agent) {
        return "Must have been nothing.";
    }
    
    public static String enterSuspicious(Agent speaker, Agent agent) {
        return "What was that?";
    }
    
    public static String enterAlert(Agent speaker, Agent agent) {
        return "Take them out!";
    }

    public static String forHostility(Agent speaker, Agent agent) {
        return sample(HOSTILITY);
    }
    
    public static String forDeadAlly(Agent speaker) {
        return "Find whoever did this!";
    }

    public static String thank(Agent speaker, Agent agent) {
        return "Thank you.";
    }
    
    private static String sample(List<String> list) {
        int index = (int) (Math.random() * list.size());
        return list.get(index);
    }

    private GenericDialogue() {
        // singleton
    }
}
