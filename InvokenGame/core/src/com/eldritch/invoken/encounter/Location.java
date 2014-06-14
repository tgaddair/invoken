package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.LightManager;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.google.common.primitives.Ints;

public class Location {
    public static final int PX = 32;
    public static final int MAX_WIDTH = 100;
    public static final int MAX_HEIGHT = 100;

    private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    
    private final Color actionsColor = new Color(1, 0, 0, 1);

    private final Player player;
    private final LocationMap map;
    private final List<Agent> entities = new ArrayList<Agent>();
    private final List<Agent> activeEntities = new ArrayList<Agent>();
    private final List<TemporaryEntity> tempEntities = new ArrayList<TemporaryEntity>();
    private final List<Activator> activators = new ArrayList<Activator>();
    private final Set<NaturalVector2> activeTiles = new HashSet<NaturalVector2>();
    private final LightManager lightManager = new LightManager();

    private OrthogonalTiledMapRenderer renderer;
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private int[] overlays;
    private int collisionIndex = -1;
    private int overlayIndex = -1;
    private final int groundIndex = 0;

    public Location(com.eldritch.scifirpg.proto.Locations.Location data, Player player) {
        this(data, player, readMap(data));
    }

    public Location(com.eldritch.scifirpg.proto.Locations.Location data, Player player,
            LocationMap map) {
        this.player = player;
        this.map = map;

        // find layers we care about
        List<Integer> overlayList = new ArrayList<Integer>();
        for (int i = 0; i < map.getLayers().getCount(); i++) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
            if (layer.getName().equals("collision")) {
                layer.setVisible(false);
                collisionIndex = i;
            } else if (layer.getName().equals("overlay")) {
                overlayList.add(i);
                overlayIndex = i;

            } else if (layer.getName().equals("overlay-trim")) {
                // overlays are rendered above all objects always
                overlayList.add(i);
            }
        }
        overlays = Ints.toArray(overlayList);

        // objects are rendered by y-ordering with other entities
        float unitScale = 1.0f / PX;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // spawn and add the player
        Vector2 spawn = getSpawnLocation();
        player.setPosition(spawn.x, spawn.y);
        addActor(player);

        // add encounters
        addEntities(data, map);
    }

    public void addEntity(TemporaryEntity entity) {
        tempEntities.add(entity);
    }

    public void addEntities(List<Agent> entities) {
        this.entities.addAll(entities);
    }

    public void addActivators(List<Activator> activators) {
        this.activators.addAll(activators);
    }

    public void addEntities(com.eldritch.scifirpg.proto.Locations.Location data, TiledMap map) {
        // find spawn nodes
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof EncounterLayer) {
                EncounterLayer encounterLayer = (EncounterLayer) layer;
                Encounter encounter = encounterLayer.encounter;
                if (encounter.getType() == Encounter.Type.ACTOR) {
                    // create NPCs
                    LinkedList<Vector2> spawnNodes = getSpawnNodes(encounterLayer);
                    for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
                        addActor(createTestNpc(spawnNodes.poll(), scenario.getActorId()));
                    }
                }
            }
        }
    }

    private static LinkedList<Vector2> getSpawnNodes(TiledMapTileLayer layer) {
        LinkedList<Vector2> list = new LinkedList<Vector2>();
        layer.setVisible(false);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    list.add(new Vector2(x, y));
                }
            }
        }
        return list;
    }

    public void addLights(List<Light> lights) {
        for (Light light : lights) {
            this.lightManager.addLight(light);
        }
    }

    public static Pool<Rectangle> getRectPool() {
        return rectPool;
    }

    public Array<Rectangle> getTiles() {
        return tiles;
    }

    private Vector2 getSpawnLocation() {
        TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get("player");
        spawnLayer.setVisible(false);
        for (int x = 0; x < spawnLayer.getWidth(); x++) {
            for (int y = 0; y < spawnLayer.getHeight(); y++) {
                Cell cell = spawnLayer.getCell(x, y);
                if (cell != null) {
                    return new Vector2(x, y);
                }
            }
        }
        return Vector2.Zero;
    }

    public Npc createTestNpc(Vector2 position, String id) {
        return createTestNpc(position.x, position.y, id);
    }

    public Npc createTestNpc(float x, float y, String id) {
        return Npc.create(InvokenGame.ACTOR_READER.readAsset(id), x, y, this);
    }

    private void addActor(Agent actor) {
        entities.add(actor);
    }

    public void render(float delta, OrthographicCamera camera, TextureRegion selector,
            boolean paused) {
        
        // update the player (process input, collision detection, position update)
        resetActiveTiles();
        resetActiveEntities();
        if (!paused) {
            for (Agent actor : activeEntities) {
                actor.update(delta, this);
            }
            Iterator<TemporaryEntity> it = tempEntities.iterator();
            while (it.hasNext()) {
                TemporaryEntity entity = it.next();
                entity.update(delta, this);
                if (entity.isFinished()) {
                    it.remove();
                }
            }
        }

        // let the camera follow the player
        Vector2 position = player.getPosition();
        float scale = PX * camera.zoom;
        camera.position.x = Math.round(position.x * scale) / scale;
        camera.position.y = Math.round(position.y * scale) / scale;
        camera.update();

        // draw lights
        lightManager.render(renderer, delta, paused);

        // set the tile map render view based on what the
        // camera sees and render the map
        renderer.setView(camera);
        renderer.render();
        
        if (paused) {
            // render all pending player actions
            actionsColor.set(actionsColor.r, actionsColor.g, actionsColor.b, 1);
            for (Action action : player.getReverseActions()) {
                drawCentered(selector, action.getPosition(), actionsColor);
                actionsColor.set(
                        actionsColor.r, actionsColor.g, actionsColor.b, actionsColor.a * 0.5f);
            }
        }

        // sort drawables by descending y
        Collections.sort(activeEntities, new Comparator<Agent>() {
            @Override
            public int compare(Agent a1, Agent a2) {
                return Float.compare(a2.getPosition().y, a1.getPosition().y);
            }
        });

        // render the drawables
        for (Agent actor : activeEntities) {
            if (actor == player.getTarget() && !paused) {
                Color color = new Color(0x00FA9AFF);
                if (!actor.isAlive()) {
                    // dark slate blue
                    color = new Color(0x483D8BFF);
                }
                drawCentered(selector, actor.getPosition(), color);
            }
            actor.render(delta, renderer);
        }
        for (TemporaryEntity entity : tempEntities) {
            entity.render(delta, renderer);
        }

        // render the overlay layer
        renderer.render(overlays);
    }

    private void drawCentered(TextureRegion region, Vector2 position, Color color) {
        float w = 1f / PX * region.getRegionWidth();
        float h = 1f / PX * region.getRegionHeight();

        Batch batch = renderer.getSpriteBatch();
        batch.setColor(color);
        batch.begin();
        batch.draw(region, position.x - w / 2, position.y - h / 2 - 0.4f, w, h);
        batch.end();
        batch.setColor(Color.WHITE);
    }

    public List<Agent> getActors() {
        return activeEntities;
    }

    public List<Activator> getActivators() {
        return activators;
    }

    public List<Agent> getNeighbors(Npc agent) {
        return getNeighbors(agent, agent.getNeighbors(), getActors());
    }

    public List<Agent> getNeighbors(Agent agent, List<Agent> neighbors, List<Agent> actors) {
        neighbors.clear();
        for (Agent other : actors) {
            if (agent != other && agent.canTarget(other, this)) {
                neighbors.add(other);
            }
        }
        return neighbors;
    }

    private void resetActiveEntities() {
        activeEntities.clear();
        for (Agent other : entities) {
            if (activeTiles.contains(other.getCellPosition()) 
                    || (other.inCombat() && player.canTarget(other, this))) {
                activeEntities.add(other);
            }
        }
    }

    private void resetActiveTiles() {
        map.update(null);
        activeTiles.clear();

        Vector2 position = player.getPosition();
        NaturalVector2 origin = NaturalVector2.of((int) position.x, (int) (position.y - 0.5f));

        final float layerTileWidth = 1;
        final float layerTileHeight = 1;

        Rectangle viewBounds = renderer.getViewBounds();
        final int x1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
        final int x2 = Math.min(MAX_WIDTH,
                (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

        final int y1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
        final int y2 = Math.min(MAX_HEIGHT,
                (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

        Set<NaturalVector2> visited = new HashSet<NaturalVector2>();
        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();

        visited.add(origin);
        activeTiles.add(origin);

        queue.add(origin);
        while (!queue.isEmpty()) {
            NaturalVector2 point = queue.remove();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    int x = point.x + dx;
                    int y = point.y + dy;
                    NaturalVector2 neighbor = NaturalVector2.of(x, y);
                    if (x >= x1 && x < x2 && y >= y1 && y < y2 && !visited.contains(neighbor)) {
                        if (!isObstacle(point) && isGround(point)) {
                            // ground can spread to anything except collision below
                            if (dy >= 0 || !isObstacle(neighbor)) {
                                visited.add(neighbor);
                                activeTiles.add(neighbor);
                                queue.add(neighbor);
                            }
                        } else if (isObstacle(point) && isGround(neighbor) && isObstacle(neighbor)) {
                            // obstacles can spread up to ground collisions
                            if (dy == 1 && dx == 0) {
                                visited.add(neighbor);
                                activeTiles.add(neighbor);
                                queue.add(neighbor);
                            }
                        } else if (isObstacle(point) && isGround(point) && !isGround(neighbor)
                                && isObstacle(neighbor)) {
                            // grounded obstacles can spread sideways to non-ground obstacles
                            if (dy == 0 && dx != 0) {
                                visited.add(neighbor);
                                activeTiles.add(neighbor);
                                queue.add(neighbor);
                            }
                        } else if (isObstacle(point) && isGround(point) && isOverlay(neighbor)) {
                            // grounded obstacles can spread to overlays, but overlays cannot
                            // spread further
                            visited.add(neighbor);
                            activeTiles.add(neighbor);
                        }
                    }
                }
            }
        }

        map.update(activeTiles);
    }

    public boolean isGround(NaturalVector2 point) {
        return hasCell(point.x, point.y, groundIndex);
    }

    public boolean isObstacle(NaturalVector2 point) {
        return isObstacle(point.x, point.y);
    }

    public boolean isObstacle(int x, int y) {
        return hasCell(x, y, collisionIndex);
    }

    public boolean isOverlay(NaturalVector2 point) {
        return hasCell(point.x, point.y, overlayIndex);
    }

    private boolean hasCell(int x, int y, int layerIndex) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerIndex);
        return layer.getCell(x, y) != null;
    }

    private Cell getCell(NaturalVector2 point, int layerIndex) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerIndex);
        return layer.getCell(point.x, point.y);
    }

    public Array<Rectangle> getTiles(int startX, int startY, int endX, int endY) {
        return getTiles(startX, startY, endX, endY, getTiles());
    }

    public Array<Rectangle> getTiles(int startX, int startY, int endX, int endY,
            Array<Rectangle> tiles) {
        rectPool.freeAll(tiles);
        tiles.clear();

        // check for collision layer
        if (collisionIndex >= 0) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionIndex);
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    Cell cell = layer.getCell(x, y);
                    if (cell != null) {
                        Rectangle rect = rectPool.obtain();
                        rect.set(x, y, 1, 1);
                        tiles.add(rect);
                    }
                }
            }
        }
        return tiles;
    }

    private static LocationMap readMap(com.eldritch.scifirpg.proto.Locations.Location data) {
        // load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
        String mapAsset = String.format("maps/%s.tmx", data.getId());
        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load(mapAsset, LocationMap.class);
        assetManager.finishLoading();
        return assetManager.get(mapAsset);
    }
}