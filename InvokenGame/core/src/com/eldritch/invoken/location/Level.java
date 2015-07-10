package com.eldritch.invoken.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.ai.steer.utils.Collision;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
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
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.Drawable;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.ai.Squad;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Icepik;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.pathfinding.PathManager;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.FixedPoint;
import com.eldritch.invoken.actor.type.DummyPlayer;
import com.eldritch.invoken.actor.type.InanimateEntity;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.Projectile;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.gfx.FogMaskManager;
import com.eldritch.invoken.gfx.FogOfWarMasker;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.LightManager;
import com.eldritch.invoken.gfx.NormalMapShader;
import com.eldritch.invoken.gfx.OrthogonalShadedTiledMapRenderer;
import com.eldritch.invoken.gfx.OverlayLightMasker;
import com.eldritch.invoken.location.EncounterDescription.AgentDescription;
import com.eldritch.invoken.location.layer.LocationLayer.CollisionLayer;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.location.proc.LocationGenerator;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Locations;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.proto.Locations.Location;
import com.eldritch.invoken.ui.AgentStatusRenderer;
import com.eldritch.invoken.ui.DebugEntityRenderer;
import com.eldritch.invoken.ui.HealthBar;
import com.eldritch.invoken.util.GameTransition;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class Level {
    private static Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };

    private final Pool<Bullet> bulletPool;
    private final LineOfSightHandler losHandler = new LineOfSightHandler();
    private final Color actionsColor = new Color(1, 0, 0, 1);

    private Player player;
    private final Locations.Level data;
    private final LocationMap map;
    private final Territory[][] territory;
    private final PathManager pathManager;
    private final GameTransition state;
    private final long seed;

    private final Map<String, Agent> ids = new HashMap<>();
    private final List<Agent> entities = new ArrayList<>();
    private final List<Agent> inactiveEntities = new ArrayList<>();
    private final Map<Agent, Float> elapsed = new HashMap<>();
    private final List<Agent> activeEntities = new ArrayList<>();
    private final Map<String, Integer> markers = new HashMap<>();
    private final Map<Agent, HealthBar> healthBars = new HashMap<>();
    private int inactiveIndex = 0;

    private final List<Drawable> drawables = new ArrayList<>();
    private final List<TemporaryEntity> pending = new ArrayList<>();
    private final List<TemporaryEntity> tempEntities = new ArrayList<>();
    private final List<Activator> activators = new ArrayList<>();
    private final List<InanimateEntity> objects = new ArrayList<>();
    private final List<SecurityCamera> securityCameras = new ArrayList<>();
    private final Set<NaturalVector2> activeTiles = new HashSet<>();
    private final Set<NaturalVector2> filledTiles = new HashSet<>();
    private final List<FixedPoint> activeCover = new ArrayList<>();
    private final LightManager lightManager;
    private final NormalMapShader normalMapShader;
    private final OverlayLightMasker lightMasker;
    private final FogOfWarMasker fowMasker;
    private final FogMaskManager fogManager;

    private OrthogonalShadedTiledMapRenderer renderer;
    private OrthogonalShadedTiledMapRenderer overlayRenderer;
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private OrthographicCamera camera;
    private final Vector3 cameraV = new Vector3();

    private final CollisionLayer collision;
    private final int groundIndex = 0;

    private final World world;
    // private final RayHandler rayHandler;
    private boolean forceRefresh = false;

    private final Vector2 offset = new Vector2();
    private final Vector2 losFocus = new Vector2();

    private final Map<NaturalVector2, Location> locations = new HashMap<>();
    private NaturalVector2 currentCell = null;
    private float currentZoom = 0;
    private Rectangle viewBounds = new Rectangle();
    private Rectangle worldBounds = new Rectangle();

    private final Set<ConnectedRoom> visitedRooms = new HashSet<>();
    private ConnectedRoom currentRoom;

    DebugEntityRenderer debugEntityRenderer = DebugEntityRenderer.getInstance();
    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public Level(Locations.Level data, LocationMap map, GameTransition state, long seed) {
        this.data = data;
        this.map = map;
        this.territory = new Territory[map.getWidth()][map.getHeight()];
        this.pathManager = new PathManager(map);
        this.state = state;
        this.seed = seed;
        lightManager = new LightManager(data);
        normalMapShader = new NormalMapShader();
        lightMasker = new OverlayLightMasker(lightManager.getVertexShaderDef());
        fowMasker = new FogOfWarMasker();
        fogManager = new FogMaskManager();

        // create territory table
        assignTerritory(map.getRooms(), data.getLocationList());
        // assignLocations(map.getRooms(), data.getLocationList());

        // find layers we care about
        CollisionLayer collision = null;
        for (int i = 0; i < map.getLayers().getCount(); i++) {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
            if (layer.getName().equals("collision")) {
                layer.setVisible(false);
                collision = (CollisionLayer) layer;
            }
        }
        this.collision = collision;

        // objects are rendered by y-ordering with other entities
        float unitScale = Settings.SCALE;
        renderer = new OrthogonalShadedTiledMapRenderer(map, unitScale, normalMapShader);
        overlayRenderer = new OrthogonalShadedTiledMapRenderer(map.getOverlayMap(), unitScale,
                normalMapShader);

        // Instantiate a new World with no gravity and tell it to sleep when
        // possible.
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new LocationContactListener());
        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet(world);
            }
        };
        addWalls(world);

        // add encounters
        addEntities(data, map);

        // add lighting and shadow engine
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);

        // rayHandler = new RayHandler(world);
        // rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        // rayHandler.setBlurNum(3);
        // rayHandler.setShadows(false);

        short category = Settings.BIT_DEFAULT;
        short group = 0;
        short mask = Settings.BIT_WALL; // only collide with walls
        PointLight.setContactFilter(category, group, mask);
    }

    public Bullet obtainBullet(Projectile projectile) {
        Bullet bullet = bulletPool.obtain();
        bullet.setup(projectile);
        return bullet;
    }

    public void freeBullet(Bullet bullet) {
        bullet.setActive(false);
        bulletPool.free(bullet);
    }

    public void transition(String locationName, Optional<String> encounterName) {
        state.transition(locationName, encounterName, player.serialize());
    }

    public void transition(int offset) {
        state.transition("", data.getLevel() + offset, player.serialize());
    }

    public Player getPlayer() {
        return player;
    }

    public Set<ConnectedRoom> getRooms() {
        return map.getRooms().getRooms();
    }

    public void addMarker(String marker, int count) {
        if (!markers.containsKey(marker)) {
            markers.put(marker, count);
        } else {
            markers.put(marker, markers.get(marker) + count);
        }
    }

    public int getMarkerCount(String marker) {
        if (!markers.containsKey(marker)) {
            return 0;
        }
        return markers.get(marker);
    }

    public List<Agent> getActiveEntities() {
        return activeEntities;
    }

    public long getSeed() {
        return seed;
    }

    public boolean hasMusic() {
        if (!locations.containsKey(currentCell)) {
            return false;
        }
        return locations.get(currentCell).hasMusic();
    }

    public String getMusicId() {
        return locations.get(currentCell).getMusic();
    }

    public String getRegion() {
        return data.getRegion();
    }

    public int getFloor() {
        return data.getLevel();
    }

    public String getId() {
        if (!locations.containsKey(currentCell)) {
            return "";
        }
        return locations.get(currentCell).getId();
    }

    public String getName() {
        if (!locations.containsKey(currentCell)) {
            return "";
        }
        return locations.get(currentCell).getName();
    }

    public LocationMap getMap() {
        return map;
    }

    public PathManager getPathManager() {
        return pathManager;
    }

    public ConnectedRoomManager getConnections() {
        return map.getRooms();
    }

    public void dispose() {
        // rayHandler.dispose();
    }

    private void assignTerritory(ConnectedRoomManager rooms, List<Location> locations) {
        // relate the owning faction to the given territory
        Map<String, Territory> factionMap = new HashMap<>();
        for (Location location : locations) {
            for (Locations.Territory territory : location.getTerritoryList()) {
                factionMap.put(territory.getFactionId(), new Territory(territory));
            }
        }

        // default the map to the empty territory
        for (int x = 0; x < territory.length; x++) {
            for (int y = 0; y < territory[x].length; y++) {
                territory[x][y] = Territory.DEFAULT;
            }
        }

        // use the owning faction for each room to assign territories
        for (ConnectedRoom room : rooms.getRooms()) {
            Optional<String> faction = room.getFaction();
            if (faction.isPresent()) {
                for (NaturalVector2 point : room.getPoints()) {
                    territory[point.x][point.y] = factionMap.get(faction.get());
                }
            }
        }

        LocationGenerator.save(territory, "territory");
    }

    public void alertTo(Agent intruder) {
        NaturalVector2 position = intruder.getCellPosition();
        territory[position.x][position.y].alertTo(intruder);
    }

    public boolean isCaughtTrespassing(Agent watcher, Agent caught) {
        NaturalVector2 position = caught.getCellPosition();
        return !isTrespasser(watcher, position) && isTrespasser(caught, position);
    }

    public boolean isTrespasser(Agent agent) {
        return isTrespasser(agent, agent.getCellPosition());
    }

    private boolean isTrespasser(Agent agent, NaturalVector2 position) {
        return territory[position.x][position.y].isTrespasser(agent);
    }

    public boolean isOnFrontier(Agent agent) {
        NaturalVector2 position = agent.getCellPosition();
        ConnectedRoom room = getConnections().getRoom(position.x, position.y);
        return territory[position.x][position.y].isOnFrontier(room);
    }

    public void addEntity(TemporaryEntity entity) {
        pending.add(entity);
    }

    private void addFromPending(TemporaryEntity entity) {
        tempEntities.add(entity);
        drawables.add(entity);
    }

    public void addEntities(LocationMap map) {
        List<Activator> activators = map.getActivators();
        this.activators.addAll(activators);
        for (Activator activator : activators) {
            activator.register(this);
        }
        for (InanimateEntity entity : map.getEntities()) {
            objects.add(entity);
            entity.register(this);
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
            this.lightManager.addLight(light);
        }
    }

    public void addLight(Light light) {
        lightManager.addLight(light);
    }

    public static Pool<Rectangle> getRectPool() {
        return rectPool;
    }

    public Array<Rectangle> getTiles() {
        return tiles;
    }

    public void addEntities(List<Agent> entities) {
        for (Agent agent : entities) {
            addActor(agent);
        }
    }

    public void addActivator(Activator activator) {
        activators.add(activator);
    }

    public void removeActivator(Activator activator) {
        activators.remove(activator);
        drawables.remove(activator);
    }

    public boolean hasAgentWithId(String id) {
        return ids.containsKey(id);
    }

    public Agent getAgentById(String id) {
        return ids.get(id);
    }

    public HealthBar createHealthBar() {
        return new HealthBar(state.getSkin());
    }

    private void addActor(Agent agent) {
        entities.add(agent);
        ids.put(agent.getInfo().getId(), agent);
        healthBars.put(agent, new HealthBar(state.getSkin()));
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

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void shiftView(Vector2 offset) {
        cameraV.x += offset.x;
        cameraV.y += offset.y;
    }

    public List<Integer> getVisitedIndices() {
        return map.getRooms().getIndices(visitedRooms);
    }

    public void render(float delta, OrthographicCamera camera, TextureRegion selector,
            boolean paused) {
        // update the world simulation
        world.step(1 / 60f, 8, 3);

        Vector2 position = player.getCamera().getPosition();
        if (player.isAiming()) {
            // get direction to focus
            float fraction = 1;
            if (!hasLineOfSight(player, player.getCamera().getPosition(), player.getFocusPoint())) {
                fraction = losHandler.getFraction();
            }
            losFocus.set(player.getFocusPoint()).sub(position).scl(fraction);
            offset.set(losFocus).scl(.5f);
        } else {
            offset.set(player.getVelocity());
            offset.clamp(0, 5);
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
                || changedViewBounds(renderer.getViewBounds()) || forceRefresh) {
            currentCell = origin;
            currentZoom = camera.zoom;
            viewBounds.set(renderer.getViewBounds());

            resetActiveTiles(origin);
            resetActiveEntities();
            resetActiveCover();

            // visit room
            ConnectedRoom room = map.getRooms().getRoom(origin.x, origin.y);
            if (room != null && room != currentRoom && !visitedRooms.contains(room)) {
                currentRoom = room;
                visitedRooms.add(room);
            }

            // reset lights
            normalMapShader.setLightGeometry(lightManager.getLights(), getWorldBounds());
            forceRefresh = false;
        }

        // updates
        if (!paused) {
            for (int i = 0; i < inactiveEntities.size(); i++) {
                Agent agent = inactiveEntities.get(i);
                if (i == inactiveIndex) {
                    // update one inactive entity
                    inactiveIndex = (inactiveIndex + 1) % inactiveEntities.size();
                    inactiveEntities.get(inactiveIndex).update(getElapsed(agent) + delta, this);
                    setElapsed(agent, 0);
                } else {
                    setElapsed(agent, getElapsed(agent) + delta);
                }
            }

            // update all active entities
            for (Agent actor : activeEntities) {
                actor.update(delta, this);
            }

            // update all activators
            {
                Iterator<Activator> it = activators.iterator();
                while (it.hasNext()) {
                    Activator entity = it.next();
                    entity.update(delta, this);
                    if (entity.isFinished()) {
                        entity.dispose();
                        it.remove();
                        drawables.remove(entity); // TODO: could be more
                                                  // efficient
                    }
                }
            }

            // check all inanimate entities
            {
                Iterator<InanimateEntity> it = objects.iterator();
                while (it.hasNext()) {
                    InanimateEntity entity = it.next();
                    if (entity.isFinished()) {
                        entity.dispose(this);
                        it.remove();
                        drawables.remove(entity); // TODO: could be more
                                                  // efficient
                    }
                }
            }

            // add pending
            if (!pending.isEmpty()) {
                for (TemporaryEntity entity : pending) {
                    addFromPending(entity);
                }
                pending.clear();
            }

            // update all temporary entities
            {
                Iterator<TemporaryEntity> it = tempEntities.iterator();
                while (it.hasNext()) {
                    TemporaryEntity entity = it.next();
                    entity.update(delta, this);
                    if (entity.isFinished()) {
                        entity.dispose();
                        it.remove();
                        drawables.remove(entity); // TODO: could be more
                                                  // efficient
                    }
                }
            }
        }

        renderer.setView(camera);
        overlayRenderer.setView(camera);

        lightManager.update(delta);
        fowMasker.render(delta, camera);

        // draw lights
        renderer.getBatch().setShader(lightManager.getDefaultShader());
        overlayRenderer.getBatch().setShader(lightManager.getDefaultShader());
        normalMapShader.render(lightManager, player, delta, camera, renderer, overlayRenderer);

        // set the tile map render view based on what the
        // camera sees and render the map
        renderer.getBatch().setShader(normalMapShader.getShader());
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

        // render the drawables
        for (Drawable drawable : drawables) {
            drawable.render(delta, renderer);
        }

        // draw overlay info
        for (Agent agent : activeEntities) {
            if (agent.getInfo().getHealthPercent() < 1) {
                HealthBar healthBar = healthBars.get(agent);
                healthBar.update(agent);
                healthBar.draw(camera);
            }
        }

        // draw targeting reticle
        if (player.isAiming()) {
            debugEntityRenderer.drawBetween(
                    player.getWeaponSentry().getPosition(),
                    losFocus.add(player.getWeaponSentry().getPosition()).sub(
                            player.getWeaponSentry().getDirection()), camera);
        }

        // render the overlay layers
        overlayRenderer.getBatch().setShader(normalMapShader.getShader());
        normalMapShader.useNormalMap(true);
        overlayRenderer.render();

        if (Settings.ENABLE_FOG) {
            fogManager.update(delta);
            fogManager.render(renderer);
        }

        // render lighting
        // boolean stepped = fixedStep(delta);
        // rayHandler.setCombinedMatrix(camera);
        // if (stepped)
        // rayHandler.update();
        // rayHandler.render();

        // render status info
        renderer.getBatch().begin();
        for (Agent agent : activeEntities) {
            AgentStatusRenderer.render(agent, player, renderer);
        }
        renderer.getBatch().end();

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
        if (Settings.DEBUG_PATHFINDING) {
            // draw pathfinding nodes
            debugEntityRenderer.renderPathfinding(player.getTarget(), camera);
        }
        if (Settings.DEBUG_STEALTH) {
            // draw threat radii
            debugEntityRenderer.renderThreat(player.getTarget(), camera);
        }

        // draw last seen
        debugEntityRenderer.renderLastSeen(player.getTarget(), camera);
    }

    private float getElapsed(Agent agent) {
        if (!elapsed.containsKey(agent)) {
            elapsed.put(agent, 0f);
        }
        return elapsed.get(agent);
    }

    private void setElapsed(Agent agent, float value) {
        elapsed.put(agent, value);
    }

    private void drawCentered(TextureRegion region, Vector2 position, Color color) {
        float w = 1f / Settings.PX * region.getRegionWidth();
        float h = 1f / Settings.PX * region.getRegionHeight();

        Batch batch = renderer.getBatch();
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

    public List<Agent> getAllAgents() {
        return entities;
    }

    public List<Activator> getActivators() {
        return activators;
    }

    public List<Agent> getNeighbors(Agent agent) {
        return getNeighbors(agent, agent.getNeighbors(), getActiveEntities());
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
        elapsed.clear();

        // add agents
        for (Agent other : entities) {
            if (activeTiles.contains(other.getCellPosition())) {
                activeEntities.add(other);
                drawables.add(other);
            } else if (other.getThreat().hasEnemies() && player.isNear(other)) {
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

        // add objects
        for (InanimateEntity entity : objects) {
            if (isActive(entity)) {
                drawables.add(entity);
            }
        }

        // add temporary entities
        drawables.addAll(tempEntities);
    }

    private boolean isActive(InanimateEntity entity) {
        // check the 4 corners
        NaturalVector2 point = getCellPosition(entity.getPosition());
        return isActive(point) || isActive(point.add((int) entity.getWidth(), 0))
                || isActive(point.add(0, (int) entity.getHeight()))
                || isActive(point.add((int) entity.getWidth(), (int) entity.getHeight()));
    }

    private boolean isActive(NaturalVector2 point) {
        return activeTiles.contains(point);
    }

    private void resetActiveCover() {
        activeCover.clear();
        for (FixedPoint point : map.getCover()) {
            if (activeTiles.contains(getCellPosition(point.getPosition()))) {
                activeCover.add(point);
            }
        }
    }

    public List<FixedPoint> getActiveCover() {
        return activeCover;
    }

    private NaturalVector2 getCellPosition(Drawable drawable) {
        return getCellPosition(drawable.getPosition());
    }

    private NaturalVector2 getCellPosition(Vector2 position) {
        return NaturalVector2.of((int) position.x, (int) position.y);
    }

    private void resetWorldBounds() {
        final float layerTileWidth = 1;
        final float layerTileHeight = 1;

        Rectangle viewBounds = renderer.getViewBounds();
        final int x1 = Math.max(0, (int) (viewBounds.x / layerTileWidth) - 1);
        final int x2 = Math.min(map.getWidth(),
                (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth) + 1);

        final int y1 = Math.max(0, (int) (viewBounds.y / layerTileHeight) - 1);
        final int y2 = Math.min(map.getHeight(),
                (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight) + 1);

        this.worldBounds = new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    private Rectangle getWorldBounds() {
        return worldBounds;
    }

    public boolean inCameraBounds(Vector2 point) {
        return worldBounds.contains(point);
    }

    public boolean isFilled(NaturalVector2 point) {
        return filledTiles.contains(point);
    }

    public boolean isVisibleOnScreen(Agent agent) {
        // not covered by fog of war, and within the camera bounds
        return isFilled(agent.getNaturalPosition()) && inCameraBounds(agent.getPosition());
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
        // if (rooms[origin.x][origin.y] != currentRoom &&
        // rooms[origin.x][origin.y] != null) {
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

        resetWorldBounds();
        Rectangle bounds = getWorldBounds();
        resetFilledTiles(origin, bounds);
        fowMasker.setBounds(bounds);

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
        return collision.hasCell(x, y);
    }

    public boolean isBulletWall(NaturalVector2 point) {
        return map.isWall(point.x, point.y) && !collision.ignoresBullets(point.x, point.y);
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
        if (collision != null) {
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    Cell cell = collision.getCell(x, y);
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

    public boolean hasLineOfSight(Agent source, Vector2 origin, Vector2 target) {
        losHandler.reset(source);
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

    public Npc createNpc(String id, Vector2 position) {
        Npc npc = createTestNpc(position, id);
        addActor(npc);
        forceRefresh = true;
        return npc;
    }

    public Npc createTestNpc(Vector2 position, String id) {
        return createTestNpc(position.x + 0.5f, position.y + 0.5f, id);
    }

    public Npc createTestNpc(float x, float y, String id) {
        return Npc.create(InvokenGame.ACTOR_READER.readAsset(id), x, y, this);
    }

    public void addEntities(Locations.Level data, LocationMap map) {
        // find spawn nodes
        List<Npc> npcs = new ArrayList<>();
        for (EncounterDescription encounter : map.getEncounters()) {
            // create NPCs
            for (AgentDescription agent : encounter.getAgents()) {
                ActorScenario scenario = agent.getScenario();
                Npc npc = createTestNpc(agent.getPosition(), scenario.getActorId());
                addActor(npc);
                npcs.add(npc);

                if (agent.hasRoom()) {
                    // give the NPC the key
                    ConnectedRoom room = agent.getRoom();
                    npc.getInventory().addItem(room.getKey());
                    room.addResident(npc);
                }
            }

            // create squads
            createSquads(npcs);
            npcs.clear();
        }
    }

    private void createSquads(List<Npc> npcs) {
        // build a map of faction to NPCs primarily within that faction
        Map<Faction, List<Npc>> cliques = Maps.newHashMap();
        for (Npc npc : npcs) {
            Faction faction = npc.getInfo().getFactionManager().getDominantFaction();
            if (faction != null) {
                if (!cliques.containsKey(faction)) {
                    cliques.put(faction, new ArrayList<Npc>());
                }
                cliques.get(faction).add(npc);
            }
        }

        // for every clique, we need to group the NPCs together into a squad and
        // define a leader
        for (Entry<Faction, List<Npc>> entry : cliques.entrySet()) {
            Squad squad = new Squad(entry.getKey(), entry.getValue());
            for (Npc npc : entry.getValue()) {
                npc.setSquad(squad);
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

    public Player createPlayerCorpse(PlayerActor proto) {
        Player corpse = createPlayer(proto, proto.getX(), proto.getY());
        corpse.kill();
        return corpse;
    }

    public Player createPlayer(PlayerActor proto) {
        this.player = createPlayer(proto, proto.getX(), proto.getY());
        return player;
    }

    public Player spawnPlayer(Player player) {
        Vector2 spawn = getSpawnLocation();
        player.setLocation(this, spawn.x, spawn.y);
        this.player = player;
        addActor(player);
        return player;
    }

    private Player createPlayer(PlayerActor proto, float x, float y) {
        Player player = new Player(proto, x, y, this);
        addActor(player);

        // PointLight light = new PointLight(rayHandler,
        // LightManager.RAYS_PER_BALL, null,
        // LightManager.LIGHT_DISTANCE * 3, 0, 0);
        // light.attachToBody(player.getBody(), 0, 0);

        return player;
    }

    public Player createDummyPlayer() {
        Vector2 spawn = getSpawnLocation();
        this.player = new DummyPlayer(Profession.Centurion, Settings.START_LEVEL, spawn.x, spawn.y,
                this, "sprite/characters/light-blue-hair.png");
        addActor(player);
        return player;
    }

    public Player createPlayer(Profession profession) {
        // spawn and add the player
        Vector2 spawn = getSpawnLocation();
        this.player = createPlayer(profession, spawn.x, spawn.y);
        addActor(player);

        return player;
    }

    private Player createPlayer(Profession profession, float x, float y) {
        // create the Player we want to move around the world
        Player player = new Player(profession, Settings.START_LEVEL, x, y, this,
                "sprite/characters/light-blue-hair.png");

        // 3 is the "standard" rank at which members of the same faction should
        // become allies
        // i.e. 30 rep
        Faction playerFaction = Faction.of("_PlayerFaction");
        player.getInfo().addFaction(playerFaction, 3, 0);

        AgentInventory inv = player.getInfo().getInventory();
        Item outfit = profession.getDefaultOutfit();
        inv.addItem(outfit);
        inv.equip(outfit);

        Item biocell = Item.fromProto(InvokenGame.ITEM_READER.readAsset("Biocell"));
        inv.addItem(biocell, 3);
        biocell.mapTo(inv, 0);

        Item stimpak = Item.fromProto(InvokenGame.ITEM_READER.readAsset("Stimpak"));
        inv.addItem(stimpak, 3);
        stimpak.mapTo(inv, 1);

        // player.getInfo().getInventory().addItem(Fragment.getInstance(),
        // 1000);

        Item grenade = Item.fromProto(InvokenGame.ITEM_READER.readAsset("FragmentationGrenade"));
        inv.addItem(grenade, 3);

        Item icepik = Icepik.from(1);
        inv.addItem(icepik, 5);

        Item melee = Item.fromProto(InvokenGame.ITEM_READER.readAsset("Hammer"));
        inv.addItem(melee);
        inv.equip(melee);
        
        addItemsFor(player, player.getInfo().getLevel());

        return player;
    }

    private void addItemsFor(Player player, int level) {
        AgentInventory inv = player.getInventory();
        if (level < 10) {
            Item bullet = Item.fromProto(InvokenGame.ITEM_READER.readAsset("PistolBullet"));
            inv.addItem(bullet, 25);
            
            Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("DamagedPistol"));
            player.identify(weapon.getId());
            inv.addItem(weapon);
            inv.equip(weapon);
        } else if (level < 25) {
            Item bullet = Item.fromProto(InvokenGame.ITEM_READER.readAsset("ShotgunShell"));
            inv.addItem(bullet, 25);
            
            Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("Shotgun"));
            // Item weapon =
            // Item.fromProto(InvokenGame.ITEM_READER.readAsset("AssaultRifle"));
            player.identify(weapon.getId());
            inv.addItem(weapon);
            inv.equip(weapon);
        } else {
            Item bullet = Item.fromProto(InvokenGame.ITEM_READER.readAsset("RifleBullet"));
            inv.addItem(bullet, 25);
            
            Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("RailGun"));
            player.identify(weapon.getId());
            inv.addItem(weapon);
            inv.equip(weapon);
        }
    }

    private interface ObstaclePredicate {
        boolean isObstacle(int x, int y);

        short categoryBits();
    }

    private void addWalls(World world) {
        // add walls
        addWalls(world, new ObstaclePredicate() {
            @Override
            public boolean isObstacle(int x, int y) {
                return map.isWall(x, y) && !collision.ignoresBullets(x, y);
            }

            @Override
            public short categoryBits() {
                return Settings.BIT_WALL;
            }
        });

        // low obstacles
        addWalls(world, new ObstaclePredicate() {
            @Override
            public boolean isObstacle(int x, int y) {
                return collision.ignoresBullets(x, y);
            }

            @Override
            public short categoryBits() {
                return Settings.BIT_SHORT_OBSTACLE;
            }
        });

        // add objects
        addWalls(world, new ObstaclePredicate() {
            @Override
            public boolean isObstacle(int x, int y) {
                return Level.this.isObstacle(x, y) && !map.isWall(x, y)
                        && !collision.ignoresBullets(x, y);
            }

            @Override
            public short categoryBits() {
                return Settings.BIT_OBSTACLE;
            }
        });

        // add perimeter
        addPerimeter(world);
    }

    private void addPerimeter(World world) {
        addEdge(0, 0, map.getWidth(), 0, world, Settings.BIT_PERIMETER);
        addEdge(0, map.getHeight(), map.getWidth(), map.getHeight(), world, Settings.BIT_PERIMETER);
        addEdge(0, 0, 0, map.getHeight(), world, Settings.BIT_PERIMETER);
        addEdge(map.getWidth(), 0, map.getWidth(), map.getHeight(), world, Settings.BIT_PERIMETER);
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
                        addEdge(x0, y, x, y, world, predicate.categoryBits());
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
                        addEdge(x0, y, x, y, world, predicate.categoryBits());
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
                        addEdge(x, y0, x, y, world, predicate.categoryBits());
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
                        addEdge(x, y0, x, y, world, predicate.categoryBits());
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
        return addEdge(x0, y0, x1, y1, world, Settings.BIT_WALL);
    }

    private Body addEdge(float x0, float y0, float x1, float y1, World world, short category) {
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
        fixture.setUserData(Wall.getInstance());

        // collision filters
        Filter filter = fixture.getFilterData();
        filter.categoryBits = category;
        filter.maskBits = Settings.BIT_ANYTHING;
        fixture.setFilterData(filter);

        edge.dispose();

        return body;
    }

    private class LineOfSightHandler implements RayCastCallback {
        private final short mask = Settings.BIT_TARGETABLE;
        private boolean lineOfSight = true;
        private float fraction = 1;
        private Agent source = null;

        public boolean hasLineOfSight() {
            return lineOfSight;
        }

        public float getFraction() {
            return fraction;
        }

        public void reset(Agent source) {
            lineOfSight = true;
            fraction = 1;
            this.source = source;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (isObstruction(fixture)) {
                lineOfSight = false;
                this.fraction = fraction;
                return fraction;
            }

            // ignore this fixture and continue
            return -1;
        }

        private boolean isObstruction(Fixture fixture) {
            short category = fixture.getFilterData().categoryBits;
            if ((mask & category) == 0) {
                // no common bits, so these items don't collide
                return false;
            }

            // check that the fixture belongs to another agent
            if (fixture.getUserData() != null && fixture.getUserData() instanceof Agent) {
                // cannot obstruct ourselves
                Agent agent = (Agent) fixture.getUserData();
                if (agent == source) {
                    return false;
                }

                // obstructed by living agents
                return agent.isAlive();
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