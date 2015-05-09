package com.eldritch.invoken.location.proc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.imageio.ImageIO;

import com.badlogic.gdx.math.Rectangle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.location.NaturalVector2;

public class BspGenerator {
    // 2 -> lots of big halls
    // 4 -> lots of corridors
    private static final float ROOM_SPARSITY = 4.5f;
    
    public static final int MinRoomSize = 7;
    public static final int MaxRoomSize = 15;

    private final Random rand;
    private CellType[][] map;
    private final int Width;
    private final int Height;
    private final Rectangle bounds;
    int RoomCount;

    public int Grid = 16;
    public int Padding = 4;

    public List<Rectangle> Rooms = new ArrayList<Rectangle>();
    private List<NaturalVector2> currentPath = new ArrayList<NaturalVector2>();

    enum CellType {
        Floor(1), Wall(4), Stone(20), Door(10), None(0);

        private CellType(int cost) {
            this.cost = cost;
        }

        public final int cost;
    }

    public BspGenerator(int width, int height) {
        this.rand = new Random();
        this.Width = width;
        this.Height = height;
        map = new CellType[width][height];

        RoomCount = (int) (Math.sqrt(width * height) / ROOM_SPARSITY);
        this.bounds = new Rectangle(0, 0, getWidth(), getHeight());
    }

    public BspGenerator(int roomCount, long seed) {
        this.rand = new Random(seed);
        this.RoomCount = roomCount;
        this.Width = getSize(RoomCount, 200);
        this.Height = Width;
        map = new CellType[Width][Height];
        this.bounds = new Rectangle(0, 0, getWidth(), getHeight());
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    protected int getSize(int rooms, int min) {
        return Math.max((int) (rooms * ROOM_SPARSITY), min);
    }
    
    public double random() {
        return rand.nextDouble();
    }

    public int getWidth() {
        return Width;
    }

    public int getHeight() {
        return Height;
    }

    public int getRoomCount() {
        return RoomCount;
    }

    public void generateSegments() {
        System.out.println("room count: " + RoomCount);
        System.out.println("width: " + Width);
        System.out.println("height: " + Height);

        // fill with Wall tiles
        FillMap(CellType.Wall);

        // dig stuff
        PlaceRooms();
        PlaceTunnels();
    }

    private void FillMap(CellType type) {
        for (int i = 0; i < Width; i++) {
            for (int j = 0; j < Height; j++)
                map[i][j] = type;
        }
    }

    protected void PlaceRooms() {
        // place rooms
        int placed = 0;
        int count = 0;
        while (placed < RoomCount) {
            // choose to place a rectangle room, or a templated room
            // if (btrial()) {
            int width = range(MinRoomSize, MaxRoomSize) + 2;
            int height = range(MinRoomSize, MaxRoomSize) + 2;
            if (PlaceRectRoom(bounds, width, height) != null)
                placed++;
            // } else {
            // if (PlaceTemplateRoom())
            // placed++;
            // }

            // this is for debug stuff - shouldn't ever happen
            count++;
            if (count > 1000)
                break;
        }
    }

    // / <summary>
    // / Places and digs out the tunnels from room-to-room
    // / </summary>
    protected void PlaceTunnels() {
        CostMatrix base = new DefaultCostMatrix();
        int count = 0;

        // pathfind tunnels
        Rectangle prev = Rooms.get(Rooms.size() - 1);
        for (Rectangle next : Rooms) {
            count++;
            DigTunnel(prev, next, base);
            prev = next;
            InvokenGame.log(String.format("Pathfound %d/%d", count, Rooms.size()));
        }
    }
    
    protected float getRandomX(Rectangle bounds, int width) {
        return range(bounds.x + Padding, (bounds.x + bounds.width) - width - Padding * 2);
    }
    
    protected float getRandomY(Rectangle bounds, int height) {
        return range(bounds.y + Padding, (bounds.y + bounds.height) - height - Padding * 2);
    }

    protected Rectangle PlaceRectRoom(Rectangle bounds, int width, int height) {
        Rectangle room = getRectangle(bounds, width, height);
        if (placeRectRoom(room)) {
            return room;
        }
        return null;
    }
    
    protected Rectangle getRectangle(Rectangle bounds, int width, int height) {
        return new Rectangle(getRandomX(bounds, width), getRandomY(bounds, height), width, height);
    }
    
    protected boolean canPlace(Rectangle room) {
        return isClear(room);
    }
    
    protected void place(Rectangle room) {
        Rooms.add(room);
        DigRoom(room);
    }
    
    protected boolean placeRectRoom(Rectangle room) {
        // check room
        if (canPlace(room)) {
            place(room);
            return true;
        }
        return false;
    }

    // / <summary>
    // / Digs out the rectangular room
    // / </summary>
    // / <param name="room"></param>
    private void DigRoom(Rectangle room) {
        // place floors
        for (int i = 0; i < room.width; i++) {
            for (int j = 0; j < room.height; j++) {
                Set((int) room.x + i, (int) room.y + j, CellType.Floor);
            }
        }

        // place stone around the entire thing
        for (int i = 0; i < room.width; i++) {
            Set((int) room.x + i, (int) room.y, CellType.Stone);
            Set((int) room.x + i, (int) (room.y + room.height - 1), CellType.Stone);
        }

        for (int i = 1; i < room.height - 1; i++) {
            Set((int) room.x, (int) room.y + i, CellType.Stone);
            Set((int) (room.x + room.width - 1), (int) room.y + i, CellType.Stone);
        }

        // make some doors
        // int doors = 2;
        // for (int i = 0; i < doors; i++) {
        // if (range(0, 2) == 0) {
        // Set((int) room.x + range(0, (int) room.width),
        // (int) room.y + choose(-1, (int) room.height), CellType.Wall);
        // } else {
        // Set((int) room.x + choose(-1, (int) room.width),
        // (int) room.y + range(0, (int) room.height), CellType.Wall);
        // }
        // }
    }

    protected void DigTunnel(Rectangle prev, Rectangle next, CostMatrix base) {
        // pathfind from the center of the previous room to the center of the next room
        Pathfind((int) (prev.x + prev.width / 2), (int) (prev.y + prev.height / 2),
                (int) (next.x + next.width / 2), (int) (next.y + next.height / 2), base);

        // dig out the tunnel we just found
        // int size = choose(1, 2);
        int size = 2;
        for (NaturalVector2 point : currentPath) {
            Set(point.x - size / 2, point.y - size / 2, size, size, CellType.Floor, CellType.Stone);
        }
    }

    // / <summary>
    // / Finds a path from the first poisition to the 2nd position and stores it in the currentPath
    // variable
    // / NOTE: This is probably super horrible A* Pathfinding algorithm. I'm sure there's WAY better
    // ways of writing this
    // / </summary>
    protected void Pathfind(int x, int y, int x2, int y2, CostMatrix base) {
        final int[][] cost = new int[Width][Height];
        cost[x][y] = CellType.Floor.cost;

        PriorityQueue<NaturalVector2> active = new PriorityQueue<NaturalVector2>(1,
                new Comparator<NaturalVector2>() {
                    @Override
                    public int compare(NaturalVector2 p1, NaturalVector2 p2) {
                        return Integer.compare(cost[p1.x][p1.y], cost[p2.x][p2.y]);
                    }
                });

        active.add(NaturalVector2.of(x, y));
        // pathfind
        while (true) {
            // get lowest cost point in active list
            NaturalVector2 point = active.remove();

            // if end point
            if (point.x == x2 && point.y == y2)
                break;

            // move in directions
            // cost == 0 check tells us we haven't visited this node yet
            // then we add the cost by checking the node's type in the map and adding it to the
            // cost coming from the previous node
            int currentCost = cost[point.x][point.y];
            if (point.x - 1 >= 0 && cost[point.x - 1][point.y] == 0) {
                cost[point.x - 1][point.y] = currentCost + map[point.x - 1][point.y].cost + base.getCost(point.x, point.y, point.x - 1, point.y);
                active.add(NaturalVector2.of(point.x - 1, point.y));
            }
            if (point.x + 1 < Width && cost[point.x + 1][point.y] == 0) {
                cost[point.x + 1][point.y] = currentCost + map[point.x + 1][point.y].cost + base.getCost(point.x, point.y, point.x + 1, point.y);
                active.add(NaturalVector2.of(point.x + 1, point.y));
            }
            if (point.y - 1 >= 0 && cost[point.x][point.y - 1] == 0) {
                cost[point.x][point.y - 1] = currentCost + map[point.x][point.y - 1].cost + base.getCost(point.x, point.y, point.x, point.y - 1);
                active.add(NaturalVector2.of(point.x, point.y - 1));
            }
            if (point.y + 1 < Height && cost[point.x][point.y + 1] == 0) {
                cost[point.x][point.y + 1] = currentCost + map[point.x][point.y + 1].cost + base.getCost(point.x, point.y, point.x, point.y + 1);
                active.add(NaturalVector2.of(point.x, point.y + 1));
            }
        }

        // work backwards and find path
        List<NaturalVector2> points = new ArrayList<NaturalVector2>();
        NaturalVector2 current = NaturalVector2.of(x2, y2);
        points.add(current);

        while (current.x != x || current.y != y) {
            int highest = cost[current.x][current.y];
            int left = highest, right = highest, up = highest, down = highest;

            // get cost of each direction
            if (current.x - 1 >= 0 && cost[current.x - 1][current.y] != 0) {
                left = cost[current.x - 1][current.y];
            }
            if (current.x + 1 < Width && cost[current.x + 1][current.y] != 0) {
                right = cost[current.x + 1][current.y];
            }
            if (current.y - 1 >= 0 && cost[current.x][current.y - 1] != 0) {
                up = cost[current.x][current.y - 1];
            }
            if (current.y + 1 < Height && cost[current.x][current.y + 1] != 0) {
                down = cost[current.x][current.y + 1];
            }

            // move in the lowest direction
            if (left <= min(up, down, right)) {
                points.add(current = NaturalVector2.of(current.x - 1, current.y));
            } else if (right <= min(left, down, up)) {
                points.add(current = NaturalVector2.of(current.x + 1, current.y));
            } else if (up <= min(left, right, down)) {
                points.add(current = NaturalVector2.of(current.x, current.y - 1));
            } else {
                points.add(current = NaturalVector2.of(current.x, current.y + 1));
            }
        }

        Collections.reverse(points);
        currentPath = points;
    }
    
    protected CellType get(int x, int y) {
        return map[x][y];
    }

    protected void Set(int x, int y, CellType type) {
        Set(x, y, type, CellType.None);
    }

    // / <summary>
    // / Sets the given cell to the given type, if it's not already set to the untype
    // / </summary>
    private void Set(int x, int y, CellType type, CellType untype) {
        if (x < 0 || y < 0 || x >= Width || y >= Height)
            return;
        if (map[x][y] != untype)
            map[x][y] = type;
    }

    // / <summary>
    // / Sets the given rectangle of cells to the type
    // / </summary>
    protected void Set(int x, int y, int w, int h, CellType type, CellType untype) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Set(i, j, type);
            }
        }
    }

    // / Makes sure the area doesn't overlap any floors
    // / </summary>
    // / <param name="area"></param>
    // / <returns></returns>
    private boolean isClear(Rectangle area) {
        for (int i = 0; i < area.width; i++) {
            for (int j = 0; j < area.height; j++) {
                int x = (int) area.x + i;
                int y = (int) area.y + j;
                if (map[x][y] != CellType.Wall && map[x][y] != CellType.Stone) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean btrial() {
        return rand.nextDouble() < 0.5;
    }

    private int choose(int a, int b) {
        return btrial() ? a : b;
    }
    
    protected int rangeAround(int target, int targetLength, int length, int maxLength) {
        int min = Math.max(length + Padding - target, 0);
        int max = Math.min(maxLength - Padding - target - targetLength, 2 * length);
//        System.out.println(String.format("[%d, %d]", min, max));
        int point = range(min, max);
        if (point > length) {
            return target + targetLength + point - length;
        } else {
            return target - length + point;
        }
    }
    
    protected int range(float min, float max) {
        return range((int) min, (int) max);
    }

    protected int range(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    private int min(int a, int b, int... extra) {
        int min = Math.min(a, b);
        for (int c : extra) {
            min = Math.min(min, c);
        }
        return min;
    }

    public List<Rectangle> getRooms() {
        return Rooms;
    }

    public CellType[][] getMap() {
        return map;
    }

    public void save(String base) {
        BufferedImage image = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < Width; x++) {
            for (int y = 0; y < Height; y++) {
                switch (map[x][Height - y - 1]) {
                    case Floor:
                        image.setRGB(x, y, 0xffffff);
                        break;
                    case Wall:
                        image.setRGB(x, y, 0);
                        break;
                    case Stone:
                        image.setRGB(x, y, 0x0000ff);
                        break;
                    case Door:
                        image.setRGB(x, y, 0xff0000);
                        break;
                    case None:
                        image.setRGB(x, y, 0);
                }
            }
        }

        File outputfile = new File(System.getProperty("user.home") + "/" + base + ".png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            InvokenGame.error("Failed saving level image!", e);
        }
    }
    
    protected static class DefaultCostMatrix implements CostMatrix {
        @Override
        public int getCost(int x1, int y1, int x2, int y2) {
            return 0;
        }
    }
    
    protected static interface CostMatrix {
        int getCost(int x1, int y1, int x2, int y2);
    }
}
