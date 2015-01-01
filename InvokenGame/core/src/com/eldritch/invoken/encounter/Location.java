package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.ai.steer.utils.Collision;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.activators.Activator;
import com.eldritch.invoken.activators.SecurityCamera;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Action;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Npc;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.TemporaryEntity;
import com.eldritch.invoken.encounter.layer.EncounterLayer;
import com.eldritch.invoken.encounter.layer.LocationMap;
import com.eldritch.invoken.gfx.Light;
import com.eldritch.invoken.gfx.LightManager;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

public class Location {
	public static boolean DEBUG_DRAW = false;
	
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
    private final List<Agent> drawableEntities = new ArrayList<Agent>();
    private final List<TemporaryEntity> tempEntities = new ArrayList<TemporaryEntity>();
    private final List<Activator> activators = new ArrayList<Activator>();
    private final List<SecurityCamera> securityCameras = new ArrayList<SecurityCamera>();
    private final Set<NaturalVector2> activeTiles = new HashSet<NaturalVector2>();
    private final LightManager lightManager;
    
    private final Optional<Faction> owningFaction;

    private OrthogonalTiledMapRenderer renderer;
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private int[] overlays;
    private int collisionIndex = -1;
    private int overlayIndex = -1;
    private final int groundIndex = 0;
    
    private final World world;
    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    
    // debug
    private BitmapFont debugFont = new BitmapFont();

    public Location(com.eldritch.invoken.proto.Locations.Location data) {
        this(data, readMap(data));
    }

    public Location(com.eldritch.invoken.proto.Locations.Location data, LocationMap map) {
        this.map = map;
        owningFaction = Optional.fromNullable(Faction.of(data.getFactionId()));
        lightManager = new LightManager(data);
        
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
        
        // Instantiate a new World with no gravity and tell it to sleep when possible.
  		world = new World(new Vector2(0, 0), true);
  		addWalls(world);

        // spawn and add the player
        Vector2 spawn = getSpawnLocation();
        this.player = createPlayer(world, spawn.x, spawn.y);
        addActor(player);
        
        // add encounters
        addEntities(data, map);
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    private Player createPlayer(World world, float x, float y) {
    	// create the Player we want to move around the world
		Player player = new Player(Profession.getDefault(), 25, x, y,
				world, "sprite/characters/light-blue-hair.png");
//    			player.addFaction(playerFaction, 9, 0);
		
		Item outfit = Item.fromProto(InvokenGame.ITEM_READER.readAsset("IcarianOperativeExosuit"));
		player.getInfo().getInventory().addItem(outfit);
		player.getInfo().getInventory().equip(outfit);
		
		Item weapon = Item.fromProto(InvokenGame.ITEM_READER.readAsset("AssaultRifle"));
        player.getInfo().getInventory().addItem(weapon);
        player.getInfo().getInventory().equip(weapon);
        
        return player;
    }
    
    private void addWalls(World world) {
    	for (int y = 1; y < map.getHeight(); y++) {
    		boolean contiguous = false;
    		int x0 = 0;
    		
    		// scan through the rows looking for collision stripes and ground below
    		for (int x = 0; x < map.getWidth(); x++) {
    			if (isObstacle(x, y) && !isObstacle(x, y - 1)) {
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
    					addEdge(x0, y, x, y, world);
    					contiguous = false;
    				}
    			}
    		}
    		
    		contiguous = false;
    		x0 = 0;
    		
    		// scan through the rows looking for collision stripes and ground above
    		for (int x = 0; x < map.getWidth(); x++) {
    			if (!isObstacle(x, y) && isObstacle(x, y - 1)) {
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
    					addEdge(x0, y, x, y, world);
    					contiguous = false;
    				}
    			}
    		}
    	}
    	
    	for (int x = 1; x < map.getWidth(); x++) {
    		boolean contiguous = false;
    		int y0 = 0;
    		
    		// scan through the columns looking for collision stripes and ground left
    		for (int y = 0; y < map.getHeight(); y++) {
    			if (isObstacle(x, y) && !isObstacle(x - 1, y)) {
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
    					addEdge(x, y0, x, y, world);
    					contiguous = false;
    				}
    			}
    		}
    		
    		contiguous = false;
    		y0 = 0;
    		
    		// scan through the rows looking for collision stripes and ground right
    		for (int y = 0; y < map.getHeight(); y++) {
    			if (!isObstacle(x, y) && isObstacle(x - 1, y)) {
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
    					addEdge(x, y0, x, y, world);
    					contiguous = false;
    				}
    			}
    		}
    	}
    }
    
    private void addEdge(int x0, int y0, int x1, int y1, World world) {
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
		body.createFixture(fixtureDef);
		edge.dispose();
		
		System.out.println("edge: " + start + " " + end);
    }
    
    public void alertTo(Agent intruder) {
        if (owningFaction.isPresent()) {
            Faction faction = owningFaction.get();
            intruder.changeFactionStatus(faction, -50);
        }
    }

    public void addEntity(TemporaryEntity entity) {
        tempEntities.add(entity);
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
    
    public void resize(int width, int height) {
        lightManager.resize(width, height);
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
        return createTestNpc(position.x + 0.5f, position.y + 0.5f, id);
    }

    public Npc createTestNpc(float x, float y, String id) {
        return Npc.create(InvokenGame.ACTOR_READER.readAsset(id), x, y, this);
    }

    private void addActor(Agent actor) {
        entities.add(actor);
    }
    
    public World getWorld() {
    	return world;
    }

    public void render(float delta, OrthographicCamera camera, TextureRegion selector,
            boolean paused) {
    	// update the world simulation
    	world.step(1 / 60f, 8, 3);
        
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
        Vector2 position = player.getCamera().getPosition();
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
        Collections.sort(drawableEntities, new Comparator<Agent>() {
            @Override
            public int compare(Agent a1, Agent a2) {
                return Float.compare(a2.getPosition().y, a1.getPosition().y);
            }
        });
        
        // draw the disposition graph
        if (player.getTarget() != null) {
        	Agent target = player.getTarget();
	        ShapeRenderer sr = new ShapeRenderer();
	        sr.setProjectionMatrix(camera.combined);
	        sr.begin(ShapeType.Line);
	        SpriteBatch batch = new SpriteBatch();
	        for (Agent other : drawableEntities) {
	        	if (target == other) {
	        		// don't draw a relation edge to yourself
	        		continue;
	        	}
	        	
	        	float relation = target.getRelation(other);
	        	float val = relation;
	        	
	        	// 0 = red, 60 = yellow, 120 = green
//	        	float hue = (float) Math.floor((100 - val) * 120 / 100f);  // go from green to red
//	        	float saturation = Math.abs(val - 50) / 50f;   // fade to white as it approaches 50
	        	float hue = Math.min(Math.max(((val + 100) / 200f) * 120, 0), 120) / 360f;
	        	float saturation = 1;
	        	float brightness = 1;
	        	java.awt.Color hsv = java.awt.Color.getHSBColor(hue, saturation, brightness);
	        	Color c = new Color(hsv.getRed() / 255f, hsv.getGreen() / 255f, hsv.getBlue() / 255f, 1f);
	        	sr.setColor(c);
	        	sr.line(target.getPosition().x, target.getPosition().y,
	                    other.getPosition().x, other.getPosition().y);
	        	
	        	// draw number
	        	Vector3 screen = camera.project(new Vector3(
	        			(target.getPosition().x + other.getPosition().x) / 2f,
	                    (target.getPosition().y + other.getPosition().y) / 2f, 0));
//	        	Batch batch = renderer.getSpriteBatch();
	        	batch.begin();
	        	debugFont.draw(batch,
	        			String.format("%.2f", relation), screen.x, screen.y);
	        	batch.end();
	        }
	        sr.end();
        }

        // render the drawables
        for (Agent actor : drawableEntities) {
            if (actor == player.getTarget() && !paused) {
                Color color = new Color(0x00FA9AFF);
                if (!actor.isAlive()) {
                    // dark slate blue
                    color = new Color(0x483D8BFF);
                }
                drawCentered(selector, actor.getRenderPosition(), color);
            }
            actor.render(delta, renderer);
        }
        for (TemporaryEntity entity : tempEntities) {
            entity.render(delta, renderer);
        }

        // render the overlay layer
        renderer.render(overlays);
        
        if (DEBUG_DRAW) {
	        // draw NPC debug rays
	        for (Agent agent : drawableEntities) {
	        	if (agent instanceof Npc) {
	        		Npc npc = (Npc) agent;
	        		npc.render(camera);
	        	}
	        }
	        
	        // debug render the world
	        debugRenderer.render(world, camera.combined);
        }
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
        drawableEntities.clear();
        for (Agent other : entities) {
            if (activeTiles.contains(other.getCellPosition())) {
                activeEntities.add(other);
                drawableEntities.add(other);
            } else if (other.hasEnemies() && player.isNear(other)) {
                activeEntities.add(other);
            } else if (other == player) {
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
        
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                activeTiles.add(NaturalVector2.of(i, j));
            }
        }

        /*
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
        */

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
}