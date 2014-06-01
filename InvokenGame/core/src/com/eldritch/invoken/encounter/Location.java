package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.eldritch.invoken.actor.Agent;
import com.eldritch.invoken.actor.Npc;
import com.eldritch.invoken.actor.Player;
import com.eldritch.invoken.actor.TemporaryEntity;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.LightManager;
import com.eldritch.scifirpg.proto.Locations.Encounter;
import com.eldritch.scifirpg.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.google.common.primitives.Ints;

public class Location {
    private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };

    private final Player player;
    private final TiledMap map;
    private final List<Agent> entities = new ArrayList<Agent>();
    private final List<Agent> activeEntities = new ArrayList<Agent>();
    private final List<TemporaryEntity> tempEntities = new ArrayList<TemporaryEntity>();
    private final List<Activator> activators = new ArrayList<Activator>();
    private final LightManager lightManager = new LightManager();

    private OrthogonalTiledMapRenderer renderer;
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private int[] overlays;
    private int collisionIndex = -1;

    public Location(com.eldritch.scifirpg.proto.Locations.Location data, Player player) {
        this(data, player, readMap(data));
    }

    public Location(com.eldritch.scifirpg.proto.Locations.Location data, Player player,
            TiledMap map) {
        this.player = player;
        this.map = map;

        // find layers we care about
        List<Integer> overlayList = new ArrayList<Integer>();
        for (int i = 0; i < map.getLayers().getCount(); i++) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
            if (layer.getName().equals("collision")) {
                layer.setVisible(false);
                collisionIndex = i;
            } else if (layer.getName().equals("overlay") || layer.getName().equals("overlay-trim")) {
                // overlays are rendered above all objects always
                overlayList.add(i);
            }
        }
        overlays = Ints.toArray(overlayList);

        // objects are rendered by y-ordering with other entities
        float unitScale = 1 / 32f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // spawn and add the player
        Vector2 spawn = getSpawnLocation();
        player.setPosition(spawn.x, spawn.y);
        addActor(player);
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
        String asset = "sprite/characters/male-fair.png";
        for (Encounter proto : data.getEncounterList()) {
            if (proto.getType() == Encounter.Type.ACTOR) {
                // create NPCs
                LinkedList<Vector2> spawnNodes = getSpawnNodes(proto.getId(), map);
                for (ActorScenario scenario : proto.getActorParams().getActorScenarioList()) {
                    addActor(createTestNpc(spawnNodes.poll(), scenario.getActorId(), asset));
                }
            }
        }
    }

    private static LinkedList<Vector2> getSpawnNodes(String encounter, TiledMap map) {
        LinkedList<Vector2> list = new LinkedList<Vector2>();
        TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get(
                "Encounter-" + encounter);
        spawnLayer.setVisible(false);
        for (int x = 0; x < spawnLayer.getWidth(); x++) {
            for (int y = 0; y < spawnLayer.getHeight(); y++) {
                Cell cell = spawnLayer.getCell(x, y);
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

    public Npc createTestNpc(Vector2 position, String path, String asset) {
        return createTestNpc(position.x, position.y, path, asset);
    }
    
    public Npc createTestNpc(float x, float y, String path, String asset) {
        return new Npc(InvokenGame.ACTOR_READER.readAsset(path), x, y, asset,
                this);
    }

    private void addActor(Agent actor) {
        entities.add(actor);
    }

    public void render(float delta, OrthographicCamera camera, TextureRegion selector) {
        // update the player (process input, collision detection, position update)
        resetActiveEntities();
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

        // let the camera follow the player
        Vector2 position = player.getPosition();
        float scale = 32 * camera.zoom;
        camera.position.x = Math.round(position.x * scale) / scale;
        camera.position.y = Math.round(position.y * scale) / scale;
        camera.update();

        // draw lights
        lightManager.render(renderer, delta);

        // set the tile map render view based on what the
        // camera sees and render the map
        renderer.setView(camera);
        renderer.render();

        // sort drawables by descending y
        Collections.sort(activeEntities, new Comparator<Agent>() {
            @Override
            public int compare(Agent a1, Agent a2) {
                return Float.compare(a2.getPosition().y, a1.getPosition().y);
            }
        });

        // render the drawables
        for (Agent actor : activeEntities) {
            if (actor == player.getTarget()) {
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
        float w = 1 / 32f * region.getRegionWidth();
        float h = 1 / 32f * region.getRegionHeight();

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
            if (player.dst2(other) < 200) {
                activeEntities.add(other);
            }
        }
    }

    public boolean isObstacle(int x, int y) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionIndex);
        return layer.getCell(x, y) != null;
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

    private static TiledMap readMap(com.eldritch.scifirpg.proto.Locations.Location data) {
        // load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
        String mapAsset = String.format("maps/%s.tmx", data.getId());
        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load(mapAsset, TiledMap.class);
        assetManager.finishLoading();
        return assetManager.get(mapAsset);
    }
}