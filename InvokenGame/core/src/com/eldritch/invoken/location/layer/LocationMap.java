package com.eldritch.invoken.location.layer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.actor.type.FixedPoint;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.type.InanimateEntity.DynamicEntity;
import com.eldritch.invoken.actor.type.InanimateEntity.StaticEntity;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.Light.LightDescription;
import com.eldritch.invoken.gfx.Light.StaticLight;
import com.eldritch.invoken.location.ConnectedRoomManager;
import com.eldritch.invoken.location.EncounterDescription;
import com.eldritch.invoken.location.NaturalVector2;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.location.proc.BspGenerator.CellType;
import com.eldritch.invoken.util.Constants;
import com.eldritch.invoken.util.Settings;

public class LocationMap extends TiledMap {
    private enum Type {
        Ground, Wall, Object, LowWall, ShortObject
    }

    // TODO: maintain a map of visited connected rooms and use it as a minimap for the player
    private final Type[][] typeMap;
    private final int[][] lightWalls;
    private final Boolean[][] convexHull;

    private final TiledMapTile ground;
    private final int width;
    private final int height;
    private Set<NaturalVector2> activeTiles = null;
    private final List<EncounterDescription> encounters = new ArrayList<>();
    private final List<Activator> activators = new ArrayList<>();
    private final List<InanimateEntity> entities = new ArrayList<>();
    private final List<Light> lights = new ArrayList<>();
    private final List<FixedPoint> coverPoints = new ArrayList<>();
    private final TiledMap overlayMap = new TiledMap();

    private ConnectedRoomManager rooms;

    // lazy creation
    private CollisionLayer collision = null;

    public LocationMap(TiledMapTile ground, int width, int height) {
        this.ground = ground;
        this.width = width;
        this.height = height;
        typeMap = new Type[width][height];
        lightWalls = new int[width][height];
        convexHull = new Boolean[width][height];
    }

    public Boolean[][] getConvexHull() {
        return convexHull;
    }

    public void buildConvexHull(CellType[][] typeMap) {
        // explore all the non-ground stemming from the border
        Set<NaturalVector2> visited = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isOnBorder(x, y)) {
                    LinkedList<NaturalVector2> queue = new LinkedList<>();
                    queue.add(NaturalVector2.of(x, y));
                    while (!queue.isEmpty()) {
                        NaturalVector2 current = queue.remove();
                        if (typeMap[current.x][current.y] == CellType.Wall
                                && !visited.contains(current)) {
                            visited.add(current);
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dy = -1; dy <= 1; dy++) {
                                    if ((dx == 0) != (dy == 0)) {
                                        // cardinal directions
                                        NaturalVector2 neighbor = NaturalVector2.of(current.x + dx,
                                                current.y + dy);
                                        if (!inBounds(neighbor.x, neighbor.y)) {
                                            continue;
                                        }

                                        if (typeMap[neighbor.x][neighbor.y] == CellType.Wall
                                                && !visited.contains(neighbor)) {
                                            queue.add(neighbor);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }

        // all the non-ground we didn't visit is part of the convex hull
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean isHull = typeMap[x][y] == CellType.Wall
                        && !visited.contains(NaturalVector2.of(x, y));
                convexHull[x][y] = isHull;
            }
        }
    }

    private boolean isOnBorder(int x, int y) {
        return x == 0 || x == width - 1 || y == 0 || y == height - 1;
    }

    public void setWall(int x, int y) {
        typeMap[x][y] = Type.Wall;
    }

    public boolean isWall(int x, int y) {
        return typeMap[x][y] == Type.Wall;
    }

    public void addEncounter(EncounterDescription encounter) {
        encounters.add(encounter);
    }

    public List<EncounterDescription> getEncounters() {
        return encounters;
    }

    public void setRooms(ConnectedRoomManager rooms) {
        this.rooms = rooms;
    }

    public void addAllCover(List<FixedPoint> points) {
        coverPoints.addAll(points);
    }

    public List<FixedPoint> getCover() {
        return coverPoints;
    }

    public ConnectedRoomManager getRooms() {
        return rooms;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isStrongLightWall(int x, int y) {
        return lightWalls[x][y] > 1;
    }

    public boolean isLightWall(int x, int y) {
        return lightWalls[x][y] > 0;
    }

    public void setLightWall(int x, int y, boolean value) {
        lightWalls[x][y] += value ? 1 : -1;
    }

    public void addOverlay(LocationLayer layer) {
        overlayMap.getLayers().add(layer);
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                if (layer.isFilled(i, j)) {
                    lightWalls[i][j] = 2; // strong light wall
                }
            }
        }
    }

    public TiledMap getOverlayMap() {
        return overlayMap;
    }

    public void add(Activator activator) {
        activators.add(activator);
    }

    public void addEntity(InanimateEntity entity) {
        entities.add(entity);
    }

    public List<Activator> getActivators() {
        return activators;
    }

    public List<InanimateEntity> getEntities() {
        return entities;
    }

    public List<Light> getLights() {
        return lights;
    }

    public void update(Set<NaturalVector2> activeTiles) {
        this.activeTiles = activeTiles;
    }

    public boolean isActive(int x, int y) {
        if (activeTiles == null) {
            // during initialization
            return true;
        }
        return activeTiles.contains(NaturalVector2.of(x, y));
    }

    public boolean isClearGround(int x, int y) {
        return isGround(x, y) && !getCollisionLayer().hasCell(x, y);
    }

    public boolean isGround(int x, int y) {
        LocationLayer base = (LocationLayer) getLayers().get(0);
        return base.isGround(x, y);
    }

    public LocationLayer getCollisionLayer() {
        if (collision == null) {
            collision = (CollisionLayer) getLayers().get("collision");
        }
        return collision;
    }

    public TiledMapTile getGround() {
        return ground;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Map<String, LocationLayer> getLayerMap() {
        Map<String, LocationLayer> map = new LinkedHashMap<String, LocationLayer>();
        for (MapLayer layer : getLayers()) {
            map.put(layer.getName(), (LocationLayer) layer);
        }
        for (MapLayer layer : overlayMap.getLayers()) {
            map.put(layer.getName(), (LocationLayer) layer);
        }
        return map;
    }

    public List<InanimateEntity> merge(TiledMap map, NaturalVector2 offset) {
        List<InanimateEntity> inanimates = new ArrayList<>();
        List<TiledMapTileLayer> collisions = new ArrayList<>();
        List<LightDescription> lights = new ArrayList<>();

        Map<String, LocationLayer> presentLayers = getLayerMap();
        for (MapLayer mapLayer : map.getLayers()) {
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
            if (layer.getName().startsWith(Constants.CONSTRAINTS)) {
                // don't add the constraints
                continue;
            }

            if (layer.getName().startsWith(Constants.DYNAMICS)) {
                // add dynamic entities separately
                InanimateEntity entity = new DynamicEntity(layer, offset);
                inanimates.add(entity);
                addEntity(entity);
                continue;
            }

            if (layer.getName().startsWith(Constants.STATICS)) {
                // add static entities separately
                InanimateEntity entity = new StaticEntity(layer, offset);
                inanimates.add(entity);
                addEntity(entity);
                continue;
            }

            if (layer.getName().startsWith(Constants.LIGHTS)) {
                // add lights separately
                lights.add(LightDescription.from(layer));
                continue;
            }

            // add collision layer
            if (layer.getName().startsWith(Constants.COLLISION)) {
                collisions.add(layer);
            }

            LocationLayer existing = presentLayers.get(mapLayer.getName());
            if (existing == null) {
                existing = new LocationLayer(getWidth(), getHeight(), Settings.PX, Settings.PX,
                        this);

                // buffer layer handled constraints, but is not visible
                existing.setVisible(!layer.getName().startsWith(Constants.BUFFER));
                existing.setOpacity(1.0f);
                existing.setName(layer.getName());

                // add the new layer
                getLayers().add(existing);
            }

            // merge the new layer into the existing
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        // add this cell at the offset position
                        existing.addCell(cell.getTile(), offset.x + x, offset.y + y);
                    }
                }
            }
        }

        // add collisions to entities
        for (int i = 0; i < inanimates.size() && i < collisions.size(); i++) {
            InanimateEntity entity = inanimates.get(i);
            TiledMapTileLayer collision = collisions.get(i);
            entity.addCollisionLayer(collision);
        }

        // add lights
        for (int i = 0; i < lights.size(); i++) {
            LightDescription light = lights.get(i);
            if (i < inanimates.size()) {
                // attach to an entity
                InanimateEntity entity = inanimates.get(i);
                entity.addLight(light);
            } else {
                // add directly to the map
                Rectangle bounds = light.getBounds();
                this.lights.add(new StaticLight(new Vector2(offset.x + bounds.x + 0.5f, offset.y
                        + bounds.y + 0.5f), light));
            }
        }

        return inanimates;
    }
}
