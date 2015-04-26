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
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.eldritch.invoken.util.Settings;

public class Toaster {
    private final LinkedList<Message> messages = new LinkedList<Message>();
    private final Skin skin;
    private final Table container;
    
    private boolean active = false;
    private float displayTime = 1.75f;
    private float fadeTime = 0.75f;
    
    public Toaster(Skin skin) {
        this.skin = skin;
        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();
    }
    
    public void resize(int width, int height) {
        container.setHeight(height);
        container.setWidth(width);
    }
    
    public Table getContainer() {
        return container;
    }
    
    public void add(Message message) {
        messages.add(message);
    }
    
    public void update(float delta) {
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
        label.addAction(sequence(fadeIn(fadeTime), delay(displayTime),
                fadeOut(fadeTime), new Action() {
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
}
