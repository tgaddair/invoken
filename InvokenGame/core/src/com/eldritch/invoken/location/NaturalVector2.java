package com.eldritch.invoken.location;

import com.badlogic.gdx.math.Vector2;

public class NaturalVector2 {
    private static NaturalVector2[][] points;
    
    public final int x;
    public final int y;

    private NaturalVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public NaturalVector2 add(int x, int y) {
        return NaturalVector2.of(this.x + x, this.y + y);
    }
    
    // manhattan distance
    public int mdst(NaturalVector2 other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    public static void init(int width, int height) {
        points = new NaturalVector2[width][height];
    }
    
    public static NaturalVector2 of(Vector2 position) {
        return of((int) position.x, (int) position.y);
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
    
    public Vector2 toVector2() {
        return new Vector2(x, y);
    }
    
    public Vector2 getCenter() {
    	return new Vector2(x + 0.5f, y + 0.5f);
    }
    
    @Override
    public String toString() {
        return String.format("(%d,  %d)", x, y);
    }
}