package com.eldritch.invoken.encounter;

import com.eldritch.invoken.util.Settings;

public class NaturalVector2 {
    private static final NaturalVector2[][] points =
            new NaturalVector2[Settings.MAX_WIDTH][Settings.MAX_HEIGHT];
    
    public final int x;
    public final int y;

    private NaturalVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public static NaturalVector2 of(int x, int y) {
        if (x >= 0 && x < points.length && y >= 0 && y < points[x].length) {
            if (points[x][y] == null) {
                points[x][y] = new NaturalVector2(x, y);
            }
            return points[x][y];
        }
        return new NaturalVector2(x, y);
    }
    
    @Override
    public String toString() {
        return String.format("(%d,  %d)", x, y);
    }
}