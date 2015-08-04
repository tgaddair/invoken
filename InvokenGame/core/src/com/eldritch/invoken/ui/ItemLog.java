package com.eldritch.invoken.ui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.eldritch.invoken.actor.AgentInventory.InventoryListener;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class ItemLog implements HudElement, InventoryListener {
    private final Table container;
    private final Skin skin;
    private final Player player;

    private final Map<String, Message> messages = new LinkedHashMap<>();
    private final Map<String, Message> active = new LinkedHashMap<>();
    private final Array<Actor> tmp = new Array<>();

    private float displayTime = 1.75f;
    private float fadeTime = 0.75f;
    
    private float delay = 0;

    public ItemLog(Player player, Skin skin) {
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.right().top();
        this.skin = skin;

        this.player = player;
        player.getInventory().addListener(this);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void resize(int width, int height) {
        container.setHeight(height);
        container.setWidth(width);
    }

    @Override
    public void update(float delta, Level level) {
        if (delay > 0) {
            delay -= delta;
            return;
        }
        
        if (active.size() < 3 && !messages.isEmpty()) {
            showNext();
        }
    }

    public void setDisplayTime(float time) {
        displayTime = time;
    }

    public float getFadeTime() {
        return fadeTime;
    }

    private void showNext() {
        final Message message = getHead();
        if (message.isActive()) {
            // already displayed
            return;
        }

        delay = 1f;
        final Label label = message.getLabel();

        // configure the fade-in/out effect on the splash image
        label.addAction(sequence(fadeIn(fadeTime), delay(displayTime), fadeOut(fadeTime),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        // the last action will remove this item from the queue
                        // messages.remove(message.getItem().getId());
                        active.remove(message.getItem().getId());
                        shiftTable(container);
                        return true;
                    }
                }));

        // and finally we add the actor to the stage
        insert(container, label);

        messages.remove(message.getItem().getId());
        active.put(message.getItem().getId(), message);
        message.setActive(true);
    }

    @Override
    public void onAdd(Item item, int count) {
        addMessage(new Message(item, count));
    }

    @Override
    public void onRemove(Item item, int count) {
    }

    private void addMessage(Message message) {
        if (messages.containsKey(message.getItem().getId())) {
            // get the element corresponding to this message, update the count, and re-insert
            Message original = messages.get(message.getItem().getId());
            messages.remove(message.getItem());
            original.update(message.getCount());
            messages.put(message.getItem().getId(), original);
        } else {
            messages.put(message.getItem().getId(), message);
        }
    }

    public void shiftTable(Table table) {
        tmp.clear();
        tmp.addAll(table.getChildren());
        table.clear();
        
        boolean skipped = false;
        for (Actor actor : tmp) {
            if (skipped) {
                insert(table, actor);
            } else {
                skipped = true;
            }
        }
    }
    
    private void insert(Table container, Actor actor) {
        container.add(actor).expandX().fillX().pad(10);
    }

    private Message getHead() {
        return messages.entrySet().iterator().next().getValue();
    }

    public class Message {
        private final Item item;
        private final Label label;

        private int count;
        private String text;
        private boolean active = false;

        public Message(Item item, int count) {
            this.item = item;
            this.count = count;

            // create the label that we will display
            LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
            label = new Label("", labelStyle);

            // this is needed for the fade-in effect to work correctly; we're just
            // making the image completely transparent
            label.getColor().a = 0f;
            label.setAlignment(Align.right);

            // update label text
            setText();
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean value) {
            this.active = value;
        }

        public void update(int delta) {
            count += delta;
            setText();
        }

        public Item getItem() {
            return item;
        }

        public Label getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }

        public String getText() {
            return text;
        }

        private void setText() {
            text = String.format("Added %s (%d)", item.getName(player), count);
            label.setText(text);
        }
    }
}
