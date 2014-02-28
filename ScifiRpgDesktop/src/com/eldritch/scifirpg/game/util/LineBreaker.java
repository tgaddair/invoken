package com.eldritch.scifirpg.game.util;

public class LineBreaker {
    /**
     * Solves a problem that shouldn't exist with Swing components.
     */
    public static String breakUp(String text) {
        return breakUp(text, 70);
    }
    
    public static String breakUp(String text, int lineWidth) {
        StringBuilder sb = new StringBuilder(text);
        int pos = lineWidth;
        while (pos < text.length()) {
            int index = text.substring(0, pos).lastIndexOf(" ");
            sb.insert(index, "<br/>");
            pos += lineWidth + 5;
        }
        return sb.toString();
    }
    
    private LineBreaker() {}
}
