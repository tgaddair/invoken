package com.eldritch.invoken.location.proc;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.eldritch.invoken.gfx.NormalMappedTile;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.Maps;

public class WallTileMap {
    private static final int SIZE = Settings.PX;

    public enum WallTile {
        Roof, LeftWallTop, LeftWallBottom, MidWallTop, MidWallCenter, MidWallBottom, RightWallTop, //
        RightWallBottom, LeftTrim, RightTrim, TopLeftTrim, TopRightTrim, LeftCorner, RightCorner, //
        TopLeftCorner, TopRightCorner, FrontLeftTrim, FrontMiddleTrim, FrontRightTrim, //
        OverlayBelowTrim, OverlayLeftTrim, OverlayRightTrim
    }

    private final Map<WallTile, NormalMappedTile> tiles;

    private WallTileMap(Map<WallTile, NormalMappedTile> tiles) {
        this.tiles = tiles;
    }

    public NormalMappedTile getTile(WallTile type) {
        return tiles.get(type);
    }

    public static WallTileMap from(NormalMappedTile wall, NormalMappedTile roof ) {
        Map<WallTile, NormalMappedTile> tiles = Maps.newEnumMap(WallTile.class);
        tiles.put(WallTile.Roof, getTile(roof, 16, 48));
        tiles.put(WallTile.LeftWallTop, getTile(wall, 0, 0, SIZE / 2, SIZE));
        tiles.put(WallTile.LeftWallBottom, getTile(wall, 0, 32, SIZE / 2, SIZE));
        tiles.put(WallTile.MidWallTop, getTile(wall, 16, 0));
        tiles.put(WallTile.MidWallCenter, getTile(wall, 16, 16));
        tiles.put(WallTile.MidWallBottom, getTile(wall, 16, 32));
        tiles.put(WallTile.RightWallTop, getTile(wall, 48, 0, SIZE / 2, SIZE, SIZE / 2, 0));
        tiles.put(WallTile.RightWallBottom, getTile(wall, 48, 32, SIZE / 2, SIZE, SIZE / 2, 0));
        tiles.put(WallTile.LeftTrim, getTile(roof, 32, 48));
        tiles.put(WallTile.RightTrim, getTile(roof, 0, 48));
        tiles.put(WallTile.TopLeftTrim, getTile(roof, 0, 32));
        tiles.put(WallTile.TopRightTrim, getTile(roof, 32, 32));
        tiles.put(WallTile.LeftCorner, getTile(roof, 48, 0, SIZE / 2));
        tiles.put(WallTile.RightCorner, getTile(roof, 32, 0, SIZE / 2));
        tiles.put(WallTile.TopLeftCorner, getTile(roof, 48, 16, SIZE / 2));
        tiles.put(WallTile.TopRightCorner, getTile(roof, 32, 16, SIZE / 2));
        tiles.put(WallTile.FrontLeftTrim, getTile(roof, 0, 80, SIZE / 2, SIZE / 2));
        tiles.put(WallTile.FrontMiddleTrim, getTile(roof, 16, 80, SIZE, SIZE / 2));
        tiles.put(WallTile.FrontRightTrim, getTile(roof, 48, 80, SIZE / 2, SIZE / 2, SIZE / 2, 0));
        tiles.put(WallTile.OverlayBelowTrim, getTile(roof, 16, 32));
        tiles.put(WallTile.OverlayLeftTrim, getTile(roof, 32, 32));
        tiles.put(WallTile.OverlayRightTrim, getTile(roof, 0, 32));
        return new WallTileMap(tiles);
    }

    private static NormalMappedTile getTile(NormalMappedTile source, int x, int y) {
        return getTile(source, x, y, SIZE, SIZE);
    }

    private static NormalMappedTile getTile(NormalMappedTile source, int x, int y, int size) {
        return getTile(source, x, y, size, size);
    }

    private static NormalMappedTile getTile(NormalMappedTile source, int x, int y, int width,
            int height) {
        return getTile(source, x, y, width, height, 0, 0);
    }

    private static NormalMappedTile getTile(NormalMappedTile source, int x, int y, int width,
            int height, int dx, int dy) {
        TextureRegion sub = new TextureRegion(source.getTextureRegion(), x, y, width, height);
        TextureRegion normalSub = new TextureRegion(source.getNormalRegion(), x, y, width, height);
        NormalMappedTile tile = new NormalMappedTile(sub, normalSub);
        tile.setOffsetX(dx);
        tile.setOffsetY(dy);
        return tile;
    }
}
