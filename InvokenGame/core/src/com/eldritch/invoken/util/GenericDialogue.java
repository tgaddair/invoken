package com.eldritch.invoken.util;

import java.util.List;

import com.eldritch.invoken.actor.type.Agent;
import com.google.common.collect.ImmutableList;

public class GenericDialogue {
    private static final List<String> HOSTILITY = ImmutableList.of(
            "You will die!", "You will pay with your blood!");

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
        return sample(HOSTILITY);
    }

    public static String thank(Agent agent) {
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
