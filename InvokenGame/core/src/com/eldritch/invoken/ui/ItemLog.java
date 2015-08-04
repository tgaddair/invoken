package com.eldritch.invoken.ui;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.LinkedList;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.actor.AgentInventory.InventoryListener;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.Settings;

public class ItemLog implements HudElement, InventoryListener {
    private final Table container;
    private final Skin skin;
    private final LinkedList<Message> messages = new LinkedList<Message>();
    
    private boolean active = false;
    private float displayTime = 1.75f;
    private float fadeTime = 0.75f;

    public ItemLog(Player player, Skin skin) {
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.right().top();
        this.skin = skin;

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
        if (!active && !messages.isEmpty()) {
            container.clear();
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
        Message message = messages.peek();
        LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
        Label label = new Label(message.text, labelStyle);

        // this is needed for the fade-in effect to work correctly; we're just
        // making the image completely transparent
        label.getColor().a = 0f;
        label.setAlignment(Align.center);
        label.setFontScale(1.25f);

        // configure the fade-in/out effect on the splash image
        active = true;
        label.addAction(sequence(fadeIn(fadeTime), delay(displayTime), fadeOut(fadeTime),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        // the last action will remove this item from the queue
                        active = false;
                        messages.remove();
                        return true;
                    }
                }));

        // and finally we add the actor to the stage
        container.add(label).expandX().fillX().padBottom(container.getHeight() / 4f);
    }

    public static class Message {
        private final String text;

        public Message(String text) {
            this.text = text;
        }
    }

    @Override
    public void onAdd(Item item, int count) {
        String text = String.format("Added %s (%d)", item.getName(), count);
        messages.add(new Message(text));
    }

    @Override
    public void onRemove(Item item, int count) {
    }
}
