package com.eldritch.invoken.encounter.proc;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.gfx.NormalMappedTile;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.Maps;

public class WallTileMap {
    private static final int SIZE = Settings.PX;

    public enum WallTile {
        Roof, LeftWallTop, LeftWallBottom, MidWallTop, MidWallCenter, MidWallBottom, RightWallTop,
        RightWallBottom, LeftTrim, RightTrim, TopLeftTrim, TopRightTrim, LeftCorner, RightCorner,
        OverlayBelowTrim, OverlayLeftTrim, OverlayRightTrim
    }

    private final Map<WallTile, NormalMappedTile> tiles;

    private WallTileMap(Map<WallTile, NormalMappedTile> tiles) {
        this.tiles = tiles;
    }

    public NormalMappedTile getTile(WallTile type) {
        return tiles.get(type);
    }

    public static WallTileMap from(TextureRegion wall, TextureRegion roof) {
        Map<WallTile, NormalMappedTile> tiles = Maps.newEnumMap(WallTile.class);
        tiles.put(WallTile.Roof, getTile(roof, 16, 48));
        tiles.put(WallTile.LeftWallTop, getTile(wall, 0, 96));
        tiles.put(WallTile.LeftWallBottom, getTile(wall, 0, 128));
        tiles.put(WallTile.MidWallTop, getTile(wall, 16, 96));
        tiles.put(WallTile.MidWallCenter, getTile(wall, 16, 112));
        tiles.put(WallTile.MidWallBottom, getTile(wall, 16, 128));
        tiles.put(WallTile.RightWallTop, getTile(wall, 32, 96));
        tiles.put(WallTile.RightWallBottom, getTile(wall, 32, 128));
        tiles.put(WallTile.LeftTrim, getTile(roof, 32, 48));
        tiles.put(WallTile.RightTrim, getTile(roof, 0, 48));
        tiles.put(WallTile.TopLeftTrim, getTile(roof, 0, 32));
        tiles.put(WallTile.TopRightTrim, getTile(roof, 32, 32));
        tiles.put(WallTile.LeftCorner, getTile(roof, 48, 0, SIZE / 2));
        tiles.put(WallTile.RightCorner, getTile(roof, 32, 0, SIZE / 2));
        tiles.put(WallTile.OverlayBelowTrim, getTile(roof, 16, 32));
        tiles.put(WallTile.OverlayLeftTrim, getTile(roof, 32, 32));
        tiles.put(WallTile.OverlayRightTrim, getTile(roof, 0, 32));
        return new WallTileMap(tiles);
    }

    private static NormalMappedTile getTile(TextureRegion region, int x, int y) {
        return getTile(region, x, y, SIZE);
    }

    private static NormalMappedTile getTile(TextureRegion region, int x, int y, int size) {
        TextureRegion sub = new TextureRegion(region, x, y, size, size);
        return new NormalMappedTile(sub, null);
    }

    // sb.append(formatter.format("roof", 16, 48, x, y));
    // sb.append(formatter.format("mid-wall-top", 16, 96, wx, wy));
    // sb.append(formatter.format("mid-wall-center", 16, 112, wx, wy));
    // sb.append(formatter.format("mid-wall-bottom", 16, 128, wx, wy));
    // sb.append(formatter.format("left-trim", 32, 48, x, y));
    // sb.append(formatter.format("right-trim", 0, 48, x, y));
    // sb.append(formatter.format("top-left-trim", 0, 32, x, y));
    // sb.append(formatter.format("top-right-trim", 32, 32, x, y));
    // sb.append(formatter.format("left-corner", 48, 0, x, y, SCALE / 2));
    // sb.append(formatter.format("right-corner", 32, 0, x, y, SCALE / 2));
    // sb.append(formatter.format("overlay-below-trim", 16, 32, x, y));
    // sb.append(formatter.format("overlay-left-trim", 32, 32, x, y));
    // sb.append(formatter.format("overlay-right-trim", 0, 32, x, y));
}
