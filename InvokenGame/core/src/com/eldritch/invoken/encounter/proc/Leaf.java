package com.eldritch.invoken.encounter.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;

public class Leaf {
    private static final int MIN_LEAF_SIZE = 6;

    private final Random rand = new Random();
    public int y, x, width, height; // the position and size of this Leaf

    public Leaf leftChild; // the Leaf's left child Leaf
    public Leaf rightChild; // the Leaf's right child Leaf
    public Rectangle room; // the room that is inside this Leaf
    public List<Rectangle> halls = new ArrayList<Rectangle>(); // hallways to
                                                               // connect this
                                                               // Leaf to other
                                                               // Leafs

    public Leaf(int x, int y, int width, int height) {
        // initialize our leaf
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean split() {
        // begin splitting the leaf into two children
        if (leftChild != null || rightChild != null)
            return false; // we're already split! Abort!

        // determine direction of split
        // if the width is >25% larger than height, we split vertically
        // if the height is >25% larger than the width, we split horizontally
        // otherwise we split randomly
        boolean splitH = rand.nextDouble() > 0.5;
        if (width > height && height / width >= 0.05)
            splitH = false;
        else if (height > width && width / height >= 0.05)
            splitH = true;

        int max = (splitH ? height : width) - MIN_LEAF_SIZE; // determine the
                                                             // maximum height
                                                             // or width
        if (max <= MIN_LEAF_SIZE)
            return false; // the area is too small to split any more...

        // determine where we're going to split
        int split = randomNumber(MIN_LEAF_SIZE, max);

        // create our left and right children based on the direction of the
        // split
        if (splitH) {
            leftChild = new Leaf(x, y, width, split);
            rightChild = new Leaf(x, y + split, width, height - split);
        } else {
            leftChild = new Leaf(x, y, split, height);
            rightChild = new Leaf(x + split, y, width - split, height);
        }
        return true; // split successful!
    }

    public void createRooms() {
        // this function generates all the rooms and hallways for this Leaf and
        // all of its children.
        if (leftChild != null || rightChild != null) {
            // this leaf has been split, so go into the children leafs
            if (leftChild != null) {
                leftChild.createRooms();
            }
            if (rightChild != null) {
                rightChild.createRooms();
            }

            // if there are both left and right children in this Leaf, create a
            // hallway between them
            if (leftChild != null && rightChild != null) {
                createHall(leftChild.getRoom(), rightChild.getRoom());
            }
        } else {
            // this Leaf is the ready to make a room
            Vector2 roomSize;
            Vector2 roomPos;
            // the room can be between 3 x 3 tiles to the size of the leaf - 2.
            roomSize = new Vector2(randomNumber(3, width - 2), randomNumber(3, height - 2));

            // place the room within the Leaf, but don't put it right
            // against the side of the Leaf (that would merge rooms together)
            int sizeX = (int) roomSize.x;
            int sizeY = (int) roomSize.y;
            roomPos = new Vector2(
                    randomNumber(1, width - sizeX - 1), randomNumber(1, height - sizeY - 1));
            room = new Rectangle(x + roomPos.x, y + roomPos.y, roomSize.x, roomSize.y);
        }
    }

    public Rectangle getRoom() {
        // iterate all the way through these leafs to find a room, if one
        // exists.
        if (room != null)
            return room;
        else {
            Rectangle lRoom = (leftChild != null) ? leftChild.getRoom() : null;
            Rectangle rRoom = (rightChild != null) ? rightChild.getRoom() : null;
            if (lRoom == null && rRoom == null)
                return null;
            else if (rRoom == null)
                return lRoom;
            else if (lRoom == null)
                return rRoom;
            else if (Math.random() > .5)
                return lRoom;
            else
                return rRoom;
        }
    }

    public void createHall(Rectangle l, Rectangle r) {
        // now we connect these two rooms together with hallways.
        // this looks pretty complicated, but it's just trying to figure out
        // which point is where and then either draw a straight line, or a pair
        // of lines to make a right-angle to connect them.
        // you could do some extra logic to make your halls more bendy, or do
        // some more advanced things if you wanted.
        Vector2 point1 = getPoint(l);
        Vector2 point2 = getPoint(r);

        float w = point2.x - point1.x;
        float h = point2.y - point1.y;

        if (w < 0) {
            if (h < 0) {
                if (Math.random() > 0.5) {
                    halls.add(new Rectangle(point2.x, point1.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point2.x, point2.y, 1, Math.abs(h)));
                } else {
                    halls.add(new Rectangle(point2.x, point2.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point1.x, point2.y, 1, Math.abs(h)));
                }
            } else if (h > 0) {
                if (Math.random() > 0.5) {
                    halls.add(new Rectangle(point2.x, point1.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point2.x, point1.y, 1, Math.abs(h)));
                } else {
                    halls.add(new Rectangle(point2.x, point2.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point1.x, point1.y, 1, Math.abs(h)));
                }
            } else // if (h == 0)
            {
                halls.add(new Rectangle(point2.x, point2.y, Math.abs(w), 1));
            }
        } else if (w > 0) {
            if (h < 0) {
                if (Math.random() > 0.5) {
                    halls.add(new Rectangle(point1.x, point2.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point1.x, point2.y, 1, Math.abs(h)));
                } else {
                    halls.add(new Rectangle(point1.x, point1.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point2.x, point2.y, 1, Math.abs(h)));
                }
            } else if (h > 0) {
                if (Math.random() > 0.5) {
                    halls.add(new Rectangle(point1.x, point1.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point2.x, point1.y, 1, Math.abs(h)));
                } else {
                    halls.add(new Rectangle(point1.x, point2.y, Math.abs(w), 1));
                    halls.add(new Rectangle(point1.x, point1.y, 1, Math.abs(h)));
                }
            } else // if (h == 0)
            {
                halls.add(new Rectangle(point1.x, point1.y, Math.abs(w), 1));
            }
        } else // if (w == 0)
        {
            if (h < 0) {
                halls.add(new Rectangle(point2.x, point2.y, 1, Math.abs(h)));
            } else if (h > 0) {
                halls.add(new Rectangle(point1.x, point1.y, 1, Math.abs(h)));
            }
        }
    }
    
    private Vector2 getPoint(Rectangle rect) {
        int left = (int) rect.x;
        int right = (int) (rect.x + rect.width);
        int top = (int) rect.y;
        int bottom = (int) (rect.y + rect.height);
        return new Vector2(randomNumber(left + 1, right - 2), randomNumber(top + 1, bottom - 2));
    }

    private int randomNumber(int min, int max) {
        max += 1;
        return rand.nextInt(max - min) + min;
    }
}
