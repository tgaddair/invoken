package com.eldritch.invoken.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class DoorActivator implements Activator {
    private final Vector2 position = new Vector2();
    private final List<RemovableCell> cells = new ArrayList<RemovableCell>();
    private boolean open = false;

    public DoorActivator(int x, int y, Collection<RemovableCell> cells) {
        position.set(x, y);
        this.cells.addAll(cells);
    }

    @Override
    public boolean click(float x, float y) {
        boolean clicked = x >= position.x && x <= position.x + 1 && y >= position.y
                && y <= position.y + 1;
        if (clicked) {
            activate();
        }
        return clicked;
    }

    @Override
    public void activate() {
        open = !open;
        for (RemovableCell cell : cells) {
            cell.set(open);
        }
    }
}
