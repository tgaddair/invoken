package com.eldritch.invoken.encounter;

public class NaturalVector2 {
    private static final NaturalVector2[][] points =
            new NaturalVector2[Location.MAX_WIDTH][Location.MAX_HEIGHT];
    
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
}