package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.ai.steer.utils.Collision;
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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.SecurityCamera;
import com.eldritch.invoken.actor.Drawable;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.CoverPoint;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.gfx.FogOfWarMasker;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.LightManager;
import com.eldritch.invoken.gfx.NormalMapShader;
import com.eldritch.invoken.gfx.OrthogonalShadedTiledMapRenderer;
import com.eldritch.invoken.gfx.OverlayLightMasker;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.screens.GameScreen.GameState;
import com.eldritch.invoken.ui.AgentStatusRenderer;
import com.eldritch.invoken.ui.DebugEntityRenderer;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class Location {
    private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };

    private final LineOfSightHandler losHandler = new LineOfSightHandler();
    private final Color actionsColor = new Color(1, 0, 0, 1);

    private Player player;
    private final com.eldritch.invoken.proto.Locations.Location data;
    private final LocationMap map;
    private final GameState state;
    private final long seed;

    private final List<Agent> entities = new ArrayList<Agent>();
    private final List<Agent> inactiveEntities = new ArrayList<Agent>();
    private final List<Agent> activeEntities = new ArrayList<Agent>();
    private int inactiveIndex = 0;

    private final List<Drawable> drawables = new ArrayList<Drawable>();
    private final List<TemporaryEntity> tempEntities = new ArrayList<TemporaryEntity>();
    private final List<Activator> activators = new ArrayList<Activator>();
    private final List<SecurityCamera> securityCameras = new ArrayList<SecurityCamera>();
    private final Set<NaturalVector2> activeTiles = new HashSet<NaturalVector2>();
    private final Set<NaturalVector2> filledTiles = new HashSet<NaturalVector2>();
    private final List<CoverPoint> activeCover = new ArrayList<CoverPoint>();
    private final LightManager lightManager;
    private final NormalMapShader normalMapShader;
    private final OverlayLightMasker lightMasker;
    private final FogOfWarMasker fowMasker;

    private final Optional<Faction> owningFaction;

    private OrthogonalShadedTiledMapRenderer renderer;
    private OrthogonalShadedTiledMapRenderer overlayRenderer;
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private OrthographicCamera camera;
    private final Vector3 cameraV = new Vector3();

    private int collisionIndex = -1;
    private final int groundIndex = 0;

    private final World world;
    private final RayHandler rayHandler;

    private final Vector2 offset = new Vector2();
    private NaturalVector2 currentCell = null;
    private float currentZoom = 0;
    private Rectangle viewBounds = new Rectangle();
    private ConnectedRoom currentRoom;

    DebugEntityRenderer debugEntityRenderer = DebugEntityRenderer.getInstance();
    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public Location(com.eldritch.invoken.proto.Locations.Location data, GameState state, long seed) {
        this(data, readMap(data), state, seed);
    }

    public Location(com.eldritch.invoken.proto.Locations.Location data, LocationMap map, GameState state, long seed) {
        this.data = data;
        this.map = map;
        this.state = state;
        this.seed = seed;
        owningFaction = Optional.fromNullable(data.hasFactionId() ? Faction.of(data.getFactionId())
                : null);
        lightManager = new LightManager(data);
        normalMapShader = new NormalMapShader();
        lightMasker = new OverlayLightMasker(lightManager.getVertexShaderDef());
        fowMasker = new FogOfWarMasker();

        // find layers we care about
        for (int i = 0; i < map.getLayers().getCount(); i++) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
            if (layer.getName().equals("collision")) {
                layer.setVisible(false);
                collisionIndex = i;
            }
        }

        // objects are rendered by y-ordering with other entities
        float unitScale = Settings.SCALE;
        renderer = new OrthogonalShadedTiledMapRenderer(map, unitScale, normalMapShader);
        overlayRenderer = new OrthogonalShadedTiledMapRenderer(map.getOverlayMap(), unitScale,
                normalMapShader);

        // Instantiate a new World with no gravity and tell it to sleep when
        // possible.
        world = new World(new Vector2(0, 0), true);
        addWalls(world);

        // add encounters
        addEntities(data, map);

        // add lighting and shadow engine
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        rayHandler.setBlurNum(3);
        rayHandler.setShadows(false);

        short category = Settings.BIT_DEFAULT;
        short group = 0;
        short mask = Settings.BIT_WALL; // only collide with walls
        PointLight.setContactFilter(category, group, mask);
    }
    
    public void transition(String locationName) {
        state.transition(locationName, player.serialize());
    }

    public Player getPlayer() {
        return player;
    }

    public List<Agent> getActiveEntities() {
        return activeEntities;
    }
    
    public long getSeed() {
        return seed;
    }
    
    public String getId() {
        return data.getId();
    }

    public String getName() {
        return data.getName();
    }
    
    public ConnectedRoomManager getConnections() {
        return map.getRooms();
    }

    public void dispose() {
        rayHandler.dispose();
    }

    public void alertTo(Agent intruder) {
        if (owningFaction.isPresent()) {
            Faction faction = owningFaction.get();
            intruder.changeFactionStatus(faction, -50);
        }
    }

    public void addEntity(TemporaryEntity entity) {
        tempEntities.add(entity);
        drawables.add(entity);
    }

    public void addEntities(List<Agent> entities) {
        this.entities.addAll(entities);
    }

    public void addActivators(List<Activator> activators) {
        this.activators.addAll(activators);
        for (Activator activator : activators) {
            activator.register(this);
        }
    }

    public void addSecurityCamera(SecurityCamera camera) {
        if (!securityCameras.isEmpty()) {
            securityCameras.get(securityCameras.size() - 1).setNext(camera);
        }
        securityCameras.add(camera);
    }

    public SecurityCamera getFirstSecurityCamera() {
        return securityCameras.get(0);
    }

    public boolean hasSecurityCamera() {
        return !securityCameras.isEmpty();
    }

    public void resize(int width, int height) {
        lightManager.resize(width, height);
        normalMapShader.resize(width, height);
        lightMasker.resize(width, height);
        fowMasker.resize(width, height);
    }

    public void addLights(List<Light> lights) {
        for (Light light : lights) {
            this.lightManager.addLight(light, rayHandler);
        }
    }

    public static Pool<Rectangle> getRectPool() {
        return rectPool;
    }

    public Array<Rectangle> getTiles() {
        return tiles;
    }

    private void addActor(Agent actor) {
        entities.add(actor);
    }

    public World getWorld() {
        return world;
    }

    public void setFocusPoint(float x, float y) {
        player.setFocusPoint(x, y);
    }

    public Vector2 getFocusPoint() {
        return player.getFocusPoint();
    }

    public float scale(float v, float zoom) {
        float scale = Settings.PX * zoom * 1.25f;
        return Math.round(v * scale) / scale;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void shiftView(Vector2 offset) {
        cameraV.x += offset.x;
        cameraV.y += offset.y;
    }

    public void render(float delta, OrthographicCamera camera, TextureRegion selector,
            boolean paused) {
        // update the world simulation
        world.step(1 / 60f, 8, 3);

        Vector2 position = player.getCamera().getPosition();
        if (player.isAiming()) {
            // get direction to focus
            // TODO: this scale factor should vary with weapon range
            offset.set(player.getFocusPoint()).sub(position).scl(.5f);
        } else {
            offset.set(Vector2.Zero);
        }

        // let the camera follow the player
        float x = scale(position.x + offset.x, camera.zoom);
        float y = scale(position.y + offset.y, camera.zoom);

        float lerp = 0.01f;
        cameraV.x += (x - camera.position.x) * lerp;
        cameraV.y += (y - camera.position.y) * lerp;
        cameraV.scl(0.75f);

        camera.position.add(cameraV);
        camera.update();

        // update the player (process input, collision detection, position
        // update)
        NaturalVector2 origin = NaturalVector2.of((int) position.x, (int) position.y);
        if (origin != currentCell || camera.zoom != currentZoom || activeTiles.isEmpty()
                || changedViewBounds(renderer.getViewBounds())) {
            currentCell = origin;
            currentZoom = camera.zoom;
            viewBounds.set(renderer.getViewBounds());

            resetActiveTiles(origin);
            resetActiveEntities();
            resetActiveCover();

            // reset lights
            normalMapShader.setLightGeometry(lightManager.getLights(), getWorldBounds());
        }

        // updates
        if (!paused) {
            // update one inactive entity
            if (!inactiveEntities.isEmpty()) {
                inactiveIndex = (inactiveIndex + 1) % inactiveEntities.size();
                inactiveEntities.get(inactiveIndex).update(delta, this);
            }

            // update all active entities
            for (Agent actor : activeEntities) {
                actor.update(delta, this);
            }

            // update the activators
            for (Activator activator : activators) {
                activator.update(delta, this);
            }

            // update all temporary entities
            Iterator<TemporaryEntity> it = tempEntities.iterator();
            while (it.hasNext()) {
                TemporaryEntity entity = it.next();
                entity.update(delta, this);
                if (entity.isFinished()) {
                    it.remove();
                    drawables.remove(entity); // TODO: could be more efficient
                }
            }
        }

        renderer.setView(camera);
        overlayRenderer.setView(camera);

        lightManager.update(delta);
        fowMasker.render(delta, camera);

        // draw lights
        renderer.getSpriteBatch().setShader(lightManager.getDefaultShader());
        overlayRenderer.getSpriteBatch().setShader(lightManager.getDefaultShader());
        normalMapShader.render(lightManager, player, delta, camera, renderer, overlayRenderer);

        // set the tile map render view based on what the
        // camera sees and render the map
        renderer.getSpriteBatch().setShader(normalMapShader.getShader());
        normalMapShader.useNormalMap(true);
        renderer.render();
        normalMapShader.useNormalMap(false);

        if (paused) {
            // render all pending player actions
            actionsColor.set(actionsColor.r, actionsColor.g, actionsColor.b, 1);
            for (Action action : player.getReverseActions()) {
                drawCentered(selector, action.getPosition(), actionsColor);
                actionsColor.set(actionsColor.r, actionsColor.g, actionsColor.b,
                        actionsColor.a * 0.5f);
            }
        }

        // sort drawables by descending y
        Collections.sort(drawables, new Comparator<Drawable>() {
            @Override
            public int compare(Drawable a1, Drawable a2) {
                return Float.compare(a2.getZ(), a1.getZ());
            }
        });

        // draw the disposition graph
        if (player.getTarget() != null) {
            Agent target = player.getTarget();
            switch (Settings.DRAW_GRAPH) {
                case Disposition:
                    debugEntityRenderer.renderDispositions(target, activeEntities, camera);
                    break;
                case LOS:
                    debugEntityRenderer.renderLineOfSight(target, activeEntities, camera);
                    break;
                case Enemies:
                    debugEntityRenderer.renderEnemies(target, activeEntities, camera);
                    break;
                case Visible:
                    debugEntityRenderer.renderVisible(target, activeEntities, camera);
                    break;
                case None:
            }
        }

        // draw selected entity
        for (Agent agent : activeEntities) {
            if (agent == player.getTarget() && !paused) {
                Color color = new Color(0x00FA9AFF);
                if (!agent.isAlive()) {
                    // dark slate blue
                    color = new Color(0x483D8BFF);
                }
                drawCentered(selector, agent.getRenderPosition(), color);
            }
        }

        // draw targeting reticle
        // lineRenderer.drawBetween(player.getPosition(), focusPoint, camera);

        // render the drawables
        for (Drawable drawable : drawables) {
            drawable.render(delta, renderer);
        }

        // render the overlay layers
        overlayRenderer.getSpriteBatch().setShader(normalMapShader.getShader());
        normalMapShader.useNormalMap(true);
        overlayRenderer.render();

        // render lighting
        // boolean stepped = fixedStep(delta);
        // rayHandler.setCombinedMatrix(camera);
        // if (stepped)
        // rayHandler.update();
        // rayHandler.render();

        // render status info
        renderer.getSpriteBatch().begin();
        for (Agent agent : activeEntities) {
            AgentStatusRenderer.render(agent, player, renderer);
        }
        renderer.getSpriteBatch().end();

        if (Settings.DEBUG_DRAW) {
            // draw NPC debug rays
            for (Agent agent : activeEntities) {
                if (agent instanceof Npc) {
                    Npc npc = (Npc) agent;
                    npc.render(camera);
                }
            }

            // debug render the world
            debugRenderer.render(world, camera.combined);
        }
        if (Settings.DEBUG_LIGHTS) {
            // draw lights
            lightManager.debugRender(camera);
        }
        if (Settings.DEBUG_COVER) {
            // draw cover
            debugEntityRenderer.renderCover(player.getTarget(), activeCover, camera);
        }
        // draw last seen
        debugEntityRenderer.renderLastSeen(player.getTarget(), camera);
    }

    private void drawCentered(TextureRegion region, Vector2 position, Color color) {
        float w = 1f / Settings.PX * region.getRegionWidth();
        float h = 1f / Settings.PX * region.getRegionHeight();

        Batch batch = renderer.getSpriteBatch();
        batch.setColor(color);
        batch.begin();
        batch.draw(region, position.x - w / 2, position.y - h / 2 - 0.4f, w, h);
        batch.end();
        batch.setColor(Color.WHITE);
    }

    private boolean changedViewBounds(Rectangle cameraBounds) {
        return ((int) viewBounds.x) != ((int) cameraBounds.x)
                || ((int) viewBounds.y) != ((int) cameraBounds.y)
                || ((int) viewBounds.width) != ((int) cameraBounds.width)
                || ((int) viewBounds.height) != ((int) cameraBounds.height);
    }

    public List<Agent> getActors() {
        return activeEntities;
    }

    public List<Activator> getActivators() {
        return activators;
    }

    public List<Agent> getNeighbors(Agent agent) {
        return getNeighbors(agent, agent.getNeighbors(), getActors());
    }

    public List<Agent> getNeighbors(Agent agent, List<Agent> neighbors, List<Agent> actors) {
        neighbors.clear();
        for (Agent other : actors) {
            if (agent != other && agent.isNear(other)) {
                neighbors.add(other);
            }
        }
        return neighbors;
    }

    private void resetActiveEntities() {
        inactiveEntities.clear();
        activeEntities.clear();
        drawables.clear();

        // add agents
        for (Agent other : entities) {
            if (activeTiles.contains(other.getCellPosition())) {
                activeEntities.add(other);
                drawables.add(other);
            } else if (other.hasEnemies() && player.isNear(other)) {
                activeEntities.add(other);
            } else if (other == player) {
                activeEntities.add(other);
            } else {
                // not active
                inactiveEntities.add(other);
            }
        }

        // add activators
        for (Activator activator : activators) {
            if (activeTiles.contains(getCellPosition(activator))) {
                drawables.add(activator);
            }
        }

        // add temporary entities
        drawables.addAll(tempEntities);
    }

    private void resetActiveCover() {
        activeCover.clear();
        for (CoverPoint point : map.getCover()) {
            if (activeTiles.contains(getCellPosition(point.getPosition()))) {
                activeCover.add(point);
            }
        }
    }

    public List<CoverPoint> getActiveCover() {
        return activeCover;
    }

    private NaturalVector2 getCellPosition(Drawable drawable) {
        return getCellPosition(drawable.getPosition());
    }

    private NaturalVector2 getCellPosition(Vector2 position) {
        return NaturalVector2.of((int) position.x, (int) position.y);
    }

    private Rectangle getWorldBounds() {
        final float layerTileWidth = 1;
        final float layerTileHeight = 1;

        Rectangle viewBounds = renderer.getViewBounds();
        final int x1 = Math.max(0, (int) (viewBounds.x / layerTileWidth) - 1);
        final int x2 = Math.min(map.getWidth(),
                (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth) + 1);

        final int y1 = Math.max(0, (int) (viewBounds.y / layerTileHeight) - 1);
        final int y2 = Math.min(map.getHeight(),
                (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight) + 1);

        return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    private void resetActiveTiles(NaturalVector2 origin) {
        map.update(null);
        activeTiles.clear();

        final float layerTileWidth = 1;
        final float layerTileHeight = 1;

        Rectangle viewBounds = renderer.getViewBounds();
        final int x1 = Math.max(0, (int) (viewBounds.x / layerTileWidth) - 1);
        final int x2 = Math.min(map.getWidth(),
                (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth) + 1);

        final int y1 = Math.max(0, (int) (viewBounds.y / layerTileHeight) - 1);
        final int y2 = Math.min(map.getHeight(),
                (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight) + 1);

        // sanity check
        if (origin.x < x1 || origin.x > x2 || origin.y < y1 || origin.y > y2) {
            return;
        }

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                NaturalVector2 tile = NaturalVector2.of(i, j);
                activeTiles.add(tile);
            }
        }

        // connected rooms fill
        // ConnectedRoom[][] rooms = map.getRooms();
        // if (rooms[origin.x][origin.y] != currentRoom && rooms[origin.x][origin.y] != null) {
        // currentRoom = rooms[origin.x][origin.y];
        //
        // filledTiles.clear();
        // for (int i = 0; i < map.getWidth(); i++) {
        // for (int j = 0; j < map.getHeight(); j++) {
        // NaturalVector2 tile = NaturalVector2.of(i, j);
        // if (currentRoom.isConnected(tile, rooms)) {
        // filledTiles.add(tile);
        // }
        // }
        // }
        // lightManager.updateLights(filledTiles);
        // }

        // for (int i = x1; i <= x2; i++) {
        // for (int j = y1; j <= y2; j++) {
        // NaturalVector2 tile = NaturalVector2.of(i, j);
        // if (currentRoom.isConnected(tile, rooms)) {
        // activeTiles.add(tile);
        // }
        // }
        // }

        resetFilledTiles(origin, getWorldBounds());

        map.update(activeTiles);
    }

    private void resetFilledTiles(NaturalVector2 origin, Rectangle bounds) {
        // visible ground tiles fill
        // TODO: create a mask, gaussian blur, and apply as overlay
        Set<NaturalVector2> visited = new HashSet<NaturalVector2>();
        visited.add(origin);

        filledTiles.clear();
        filledTiles.add(origin);

        int x1 = (int) bounds.x;
        int x2 = (int) (bounds.x + bounds.width);
        int y1 = (int) bounds.y;
        int y2 = (int) (bounds.y + bounds.height);

        LinkedList<NaturalVector2> queue = new LinkedList<NaturalVector2>();
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
                    if (!map.inBounds(x, y)) {
                        continue;
                    }

                    NaturalVector2 neighbor = NaturalVector2.of(x, y);
                    if (x >= x1 && x < x2 && y >= y1 && y < y2 && !visited.contains(neighbor)) {
                        filledTiles.add(neighbor);
                        if (dx == 0 || dy == 0) {
                            // we can add diagonals, but not explore them
                            continue;
                        }

                        visited.add(neighbor);
                        if (!map.isLightWall(neighbor.x, neighbor.y)) {
                            // fill it and explore
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        lightManager.updateLights(filledTiles);
        fowMasker.updateMask(filledTiles);
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

    private boolean hasCell(int x, int y, int layerIndex) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerIndex);
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

    public boolean collides(Vector2 start, Vector2 end, Collision<Vector2> output) {
        Array<Rectangle> tiles = getTiles((int) start.x, (int) start.y, (int) end.x, (int) end.y);
        Vector2 tmp = new Vector2();
        for (Rectangle tile : tiles) {
            Vector2 center = tile.getCenter(tmp);
            float r = Math.max(tile.width, tile.height);
            if (Intersector.intersectSegmentCircle(start, end, center, r)) {
                output.point = center;
                output.normal = start.cpy().sub(end).nor().scl(5);
                return true;
            }
        }
        return false;
    }

    private static LocationMap readMap(com.eldritch.invoken.proto.Locations.Location data) {
        // load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
        String mapAsset = String.format("maps/%s.tmx", data.getId());
        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load(mapAsset, LocationMap.class);
        assetManager.finishLoading();
        return assetManager.get(mapAsset);
    }

    public boolean hasLineOfSight(Vector2 origin, Vector2 target) {
        losHandler.reset();
        return rayCast(origin, target);
    }

    private boolean rayCast(Vector2 origin, Vector2 target) {
        if (origin.equals(target)) {
            // if we don't do this check explicitly, we can get the following
            // error:
            // Expression: r.LengthSquared() > 0.0f
            return true;
        }
        world.rayCast(losHandler, origin, target);
        return losHandler.hasLineOfSight();
    }

    private Vector2 getSpawnLocation() {
        TiledMapTileLayer spawnLayer = (TiledMapTileLayer) map.getLayers().get("player");
        spawnLayer.setVisible(false);
        for (int x = 0; x < spawnLayer.getWidth(); x++) {
            for (int y = 0; y < spawnLayer.getHeight(); y++) {
                Cell cell = spawnLayer.getCell(x, y);
                if (cell != null) {
                    return new Vector2(x + 0.5f, y + 0.5f);
                }
            }
        }
        return Vector2.Zero;
    }

    public Npc createTestNpc(Vector2 position, String id) {
        return createTestNpc(position.x + 0.5f, position.y + 0.5f, id);
    }

    public Npc createTestNpc(float x, float y, String id) {
        return Npc.create(InvokenGame.ACTOR_READER.readAsset(id), x, y, this);
    }

    public void addEntities(com.eldritch.invoken.proto.Locations.Location data, TiledMap map) {
        // find spawn nodes
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof EncounterLayer) {
                EncounterLayer encounterLayer = (EncounterLayer) layer;
                Encounter encounter = encounterLayer.encounter;
                if (encounter.getType() == Encounter.Type.ACTOR) {
                    // create NPCs
                    LinkedList<Vector2> spawnNodes = getSpawnNodes(encounterLayer);
                    for (ActorScenario scenario : encounter.getActorParams().getActorScenarioList()) {
                        int min = scenario.getMin();
                        int max = scenario.getMax();
                        int count = (int) (Math.random() * (max - min + 1) + min);
                        for (int i = 0; i < count; i++) {
                            if (!spawnNodes.isEmpty()) {
                                addActor(createTestNpc(spawnNodes.poll(), scenario.getActorId()));
                            }
                        }
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

    public Player createPlayer(PlayerActor proto) {
        return createPlayer(proto, proto.getX(), proto.getY());
    }
    
    public Player spawnPlayer(Player player) {
        Vector2 spawn = getSpawnLocation();
        player.setLocation(this, spawn.x, spawn.y);
        this.player = player;
        addActor(player);
        return player;
    }
    
    public Player createPlayer(PlayerActor proto, float x, float y) {
        this.player = new Player(proto, x, y, this, "sprite/characters/light-blue-hair.png");
        addActor(player);

        PointLight light = new PointLight(rayHandler, LightManager.RAYS_PER_BALL, null,
                LightManager.LIGHT_DISTANCE * 3, 0, 0);
        light.attachToBody(player.getBody(), 0, 0);

        return player;
    }

    public Player createPlayer(Profession profession) {
        // spawn and add the player
        Vector2 spawn = getSpawnLocation();
        this.player = createPlayer(profession, spawn.x, spawn.y);
        addActor(player);

        PointLight light = new PointLight(rayHandler, LightManager.RAYS_PER_BALL, null,
                LightManager.LIGHT_DISTANCE * 3, 0, 0);
        light.attachToBody(player.getBody(), 0, 0);

        return player;
    }

    private Player createPlayer(Profession profession, float x, float y) {
        // create the Player we want to move around the world
        Player player = new Player(profession, Settings.START_LEVEL, x, y, this,
                "sprite/characters/light-blue-hair.png");

        Faction playerFaction = Faction.of("_PlayerFaction");
        player.getInfo().addFaction(playerFaction, 9, 0);

        Item outfit = profession.getDefaultOutfit();
        player.getInfo().getInventory().addItem(outfit);
        player.getInfo().getInventory().equip(outfit);

        Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("RailGun"));
        player.getInfo().getInventory().addItem(weapon);
        player.getInfo().getInventory().equip(weapon);

        Item melee = Item.fromProto(InvokenGame.ITEM_READER.readAsset("Hammer"));
        player.getInfo().getInventory().addItem(melee);
        player.getInfo().getInventory().equip(melee);

        return player;
    }

    private interface ObstaclePredicate {
        boolean isObstacle(int x, int y);

        boolean isWall();
    }

    private void addWalls(World world) {
        // add walls
        addWalls(world, new ObstaclePredicate() {
            @Override
            public boolean isObstacle(int x, int y) {
                return map.isWall(x, y);
            }

            @Override
            public boolean isWall() {
                return true;
            }
        });

        // add objects
        addWalls(world, new ObstaclePredicate() {
            @Override
            public boolean isObstacle(int x, int y) {
                return Location.this.isObstacle(x, y) && !map.isWall(x, y);
            }

            @Override
            public boolean isWall() {
                return false;
            }
        });
    }

    private void addWalls(World world, ObstaclePredicate predicate) {
        for (int y = 1; y < map.getHeight(); y++) {
            boolean contiguous = false;
            int x0 = 0;

            // scan through the rows looking for collision stripes and ground
            // below
            for (int x = 0; x < map.getWidth(); x++) {
                if (predicate.isObstacle(x, y) && !predicate.isObstacle(x, y - 1)) {
                    // this is part of a valid edge
                    if (!contiguous) {
                        // this is the first point in the edge
                        x0 = x;
                        contiguous = true;
                    }
                } else {
                    // this point is not part of a valid edge
                    if (contiguous) {
                        // this point marks the end of our last edge
                        addEdge(x0, y, x, y, world, predicate.isWall());
                        contiguous = false;
                    }
                }
            }

            contiguous = false;
            x0 = 0;

            // scan through the rows looking for collision stripes and ground
            // above
            for (int x = 0; x < map.getWidth(); x++) {
                if (!predicate.isObstacle(x, y) && predicate.isObstacle(x, y - 1)) {
                    // this is part of a valid edge
                    if (!contiguous) {
                        // this is the first point in the edge
                        x0 = x;
                        contiguous = true;
                    }
                } else {
                    // this point is not part of a valid edge
                    if (contiguous) {
                        // this point marks the end of our last edge
                        addEdge(x0, y, x, y, world, predicate.isWall());
                        contiguous = false;
                    }
                }
            }
        }

        for (int x = 1; x < map.getWidth(); x++) {
            boolean contiguous = false;
            int y0 = 0;

            // scan through the columns looking for collision stripes and ground
            // left
            for (int y = 0; y < map.getHeight(); y++) {
                if (predicate.isObstacle(x, y) && !predicate.isObstacle(x - 1, y)) {
                    // this is part of a valid edge
                    if (!contiguous) {
                        // this is the first point in the edge
                        y0 = y;
                        contiguous = true;
                    }
                } else {
                    // this point is not part of a valid edge
                    if (contiguous) {
                        // this point marks the end of our last edge
                        addEdge(x, y0, x, y, world, predicate.isWall());
                        contiguous = false;
                    }
                }
            }

            contiguous = false;
            y0 = 0;

            // scan through the rows looking for collision stripes and ground
            // right
            for (int y = 0; y < map.getHeight(); y++) {
                if (!predicate.isObstacle(x, y) && predicate.isObstacle(x - 1, y)) {
                    // this is part of a valid edge
                    if (!contiguous) {
                        // this is the first point in the edge
                        y0 = y;
                        contiguous = true;
                    }
                } else {
                    // this point is not part of a valid edge
                    if (contiguous) {
                        // this point marks the end of our last edge
                        addEdge(x, y0, x, y, world, predicate.isWall());
                        contiguous = false;
                    }
                }
            }
        }
    }

    public void setLightWalls(int x0, int y0, int x1, int y1, boolean value) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                map.setLightWall(x, y, value);
            }
        }

        // don't wait for the player to move, update the filled tiles now
        if (currentCell != null) {
            resetFilledTiles(currentCell, getWorldBounds());
        }
    }

    public Body createEdge(float x0, float y0, float x1, float y1) {
        return addEdge(x0, y0, x1, y1, world, true);
    }

    private Body addEdge(float x0, float y0, float x1, float y1, World world, boolean wall) {
        EdgeShape edge = new EdgeShape();
        Vector2 start = new Vector2(x0, y0);
        Vector2 end = new Vector2(x1, y1);
        edge.set(start, end);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyType.StaticBody;
        groundBodyDef.position.set(0, 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edge;
        fixtureDef.filter.groupIndex = 0;

        Body body = world.createBody(groundBodyDef);
        Fixture fixture = body.createFixture(fixtureDef);

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = wall ? Settings.BIT_WALL : Settings.BIT_OBSTACLE;
        filter.maskBits = Settings.BIT_ANYTHING;
        fixture.setFilterData(filter);

        edge.dispose();

        System.out.println("edge: " + start + " " + end);
        return body;
    }

    private class LineOfSightHandler implements RayCastCallback {
        private boolean lineOfSight = true;

        public boolean hasLineOfSight() {
            return lineOfSight;
        }

        public void reset() {
            lineOfSight = true;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (isObstruction(fixture)) {
                lineOfSight = false;
                return fraction;
            }

            // ignore this fixture and continue
            return -1;
        }

        private boolean isObstruction(Fixture fixture) {
            // check that the fixture belongs to another agent
            if (fixture.getUserData() != null && fixture.getUserData() instanceof Agent) {
                // cannot be obstructed by an agent
                return false;
            }

            // whatever it is, it's in the way
            return true;
        }
    };

    private final static int MAX_FPS = 30;
    private final static int MIN_FPS = 15;
    public final static float TIME_STEP = 1f / MAX_FPS;
    private final static float MAX_STEPS = 1f + MAX_FPS / MIN_FPS;
    private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
    private final static int VELOCITY_ITERS = 6;
    private final static int POSITION_ITERS = 2;

    float physicsTimeLeft;
    long aika;
    int times;

    private boolean fixedStep(float delta) {
        physicsTimeLeft += delta;
        if (physicsTimeLeft > MAX_TIME_PER_FRAME)
            physicsTimeLeft = MAX_TIME_PER_FRAME;

        boolean stepped = false;
        while (physicsTimeLeft >= TIME_STEP) {
            // world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
            physicsTimeLeft -= TIME_STEP;
            stepped = true;
        }
        return stepped;
    }
}