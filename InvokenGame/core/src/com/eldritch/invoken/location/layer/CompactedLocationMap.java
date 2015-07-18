package com.eldritch.invoken.location.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.eldritch.invoken.gfx.NormalMappedTile;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.ImmutableList;

public class CompactedLocationMap extends TiledMap {
    private CompactedLocationMap(LocationLayer layer) {
        super();
        getLayers().add(layer);
    }

    public static CompactedLocationMap from(LocationMap map, MapLayers layers) {
        Map<ImmutableList<NormalMappedTile>, NormalMappedTile> combined = new HashMap<>();

        LocationLayer base = createLayer(map, "base");
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                ImmutableList.Builder<NormalMappedTile> builder = new ImmutableList.Builder<>();
                addTiles(x, y, layers, builder);

                ImmutableList<NormalMappedTile> tiles = builder.build();
                if (tiles.isEmpty()) {
                    continue;
                }
                
                if (!combined.containsKey(tiles)) {
                    // bake the tiles together
                    combined.put(tiles, bake(tiles));
                }

                Cell cell = new LocationCell(NaturalVector2.of(x, y), base);
                cell.setTile(combined.get(tiles));
                base.setCell(x, y, cell);
            }
        }
        
        return new CompactedLocationMap(base);
    }

    private static NormalMappedTile bake(List<NormalMappedTile> tiles) {
        if (tiles.size() == 1) {
            return tiles.get(0);
        }
        
        List<TextureRegion> diffuseRegions = new ArrayList<>();
        List<TextureRegion> normalRegions = new ArrayList<>();
        for (NormalMappedTile tile : tiles) {
            diffuseRegions.add(tile.getTextureRegion());
            if (tile.hasNormal()) {
                normalRegions.add(tile.getNormalRegion());
            }
        }
        
        TextureRegion diffuse = bakeRegions(diffuseRegions);
        TextureRegion normal = bakeRegions(normalRegions);
        return new NormalMappedTile(diffuse, normal);
    }

    private static TextureRegion bakeRegions(List<TextureRegion> regions) {
        FrameBuffer buffer = new FrameBuffer(Format.RGBA8888, Settings.PX, Settings.PX, false);
        TextureRegion combined = new TextureRegion(buffer.getColorBufferTexture());

        // setup the projection matrix
        buffer.begin();
        SpriteBatch batch = new SpriteBatch();
        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
        batch.setProjectionMatrix(m);

        batch.begin();
        for (TextureRegion region : regions) {
            // draw to the frame buffer
            batch.draw(region, 0, 0, region.getRegionWidth(), region.getRegionHeight());
        }
        batch.end();
        buffer.end();

        return combined;
    }

    private static void addTiles(int x, int y, MapLayers layers,
            ImmutableList.Builder<NormalMappedTile> tiles) {
        for (MapLayer mapLayer : layers) {
            if (mapLayer instanceof LocationLayer) {
                LocationLayer layer = (LocationLayer) mapLayer;
                if (layer.hasCell(x, y)) {
                    Cell cell = layer.getCell(x, y);
                    if (cell.getTile() != null) {
                        TiledMapTile tile = cell.getTile();
                        if (tile instanceof NormalMappedTile) {
                            tiles.add((NormalMappedTile) tile);
                        }
                    }
                }
            }
        }
    }

    private static LocationLayer createLayer(LocationMap map, String name) {
        LocationLayer layer = new LocationLayer(map.getWidth(), map.getHeight(), Settings.PX,
                Settings.PX, map);
        layer.setVisible(true);
        layer.setOpacity(1.0f);
        layer.setName(name);
        return layer;
    }
}
