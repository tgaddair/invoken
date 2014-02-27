package com.eldritch.scifirpg.game.util;

public class LineBreaker {
    /**
     * Solves a problem that shouldn't exist with Swing components.
     */
    public static String breakUp(String text) {
        StringBuilder sb = new StringBuilder(text);
        int pos = 70;
        while (pos < text.length()) {
            int index = text.substring(0, pos).lastIndexOf(" ");
            sb.insert(index, "<br/>");
            pos += 75;
        }
        return sb.toString();
    }
    
    private LineBreaker() {}
}
