package com.eldritch.invoken.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.eldritch.invoken.actor.Conversable;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.util.Announcement;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DialogueMenu {
    private final Table container;
    private final Table choiceBar;
    private final Table bubble;
    private final Set<Agent> speaking = Sets.newHashSet(); // one bubble per agent at a time
    private final List<TextBubble> timedBubbles = Lists.newArrayList();
    private final Skin skin;
    private boolean active = false;

    public DialogueMenu(Skin skin) {
        this.skin = skin;

        container = new Table(skin);
        container.setHeight(Settings.MENU_VIEWPORT_HEIGHT / 2);
        container.setWidth(Settings.MENU_VIEWPORT_WIDTH);
        container.bottom();

        bubble = new Table(null);
        bubble.bottom();

        choiceBar = new Table(skin);
        choiceBar.bottom();

        ScrollPane scroll = new ScrollPane(choiceBar, skin);
        container.add(scroll).expand().fillX().bottom();
        container.setVisible(false);
    }

    public Table getTable() {
        return container;
    }

    public void update(float delta, Player player, Camera camera) {
        if (player.inDialogue()) {
            if (!active) {
                container.setVisible(true);
                active = true;
                setup(player.getConverser(), player);
            }
            setPosition(player.getConverser(), camera);
        } else {
            endDialogue();
        }

        // update temporary bubbles
        Iterator<TextBubble> it = timedBubbles.iterator();
        while (it.hasNext()) {
            TextBubble bubble = it.next();
            bubble.update(delta, camera);
            if (bubble.isFinished()) {
                bubble.onFinish();
                it.remove();
                speaking.remove(bubble.owner);
            }
        }

        // handle all other NPCs
        for (Agent agent : player.getNeighbors()) {
            if (agent.hasAnnouncements() && !speaking.contains(agent)) {
                // add a timed blurb
                addAnnouncement(agent, agent.getNextAnnouncement());
            }
        }
    }

    public void draw(Batch batch) {
        if (active) {
            bubble.draw(batch, 1.0f);
        }

        // draw temporary bubbles
        for (TextBubble bubble : timedBubbles) {
            if (bubble.isStarted()) {
                bubble.table.draw(batch, 1.0f);
            }
        }
    }

    private void setup(Conversable converser, Agent agent) {
        setup(converser, agent, converser.getDialogueHandler().getGreeting(agent));
    }

    private void setup(Conversable converser, Agent agent, Response response) {
        if (response != null) {
            // remove old content
            choiceBar.clear();
            bubble.clear();

            // handle the response outcomes
            converser.getDialogueHandler().handle(response, agent);

            List<Choice> choices = converser.getDialogueHandler().getChoicesFor(response, agent);
            if (choices.isEmpty()) {
                // display a text bubble, as there is no choice for the player to make
                addLabel(response.getText());
                container.setVisible(false);

                // continue to show the dialogue, but it's no longer forced
                agent.unforceDialogue();
            } else {
                // use the bubble to indicate that the full conversation appears elsewhere
                addLabel("...");

                // use the lower portion of the screen for the dialogue
                LabelStyle labelStyle = skin.get("response", LabelStyle.class);
                // Label name = new Label(npc.getInfo().getName(), labelStyle);
                // choiceBar.add(name).expand().fill();
                // choiceBar.row();

                Label label = new Label(response.getText(), labelStyle);
                label.setWrap(true);
                choiceBar.add(label).expand().fill().padBottom(15);
                choiceBar.row();

                // add the choices below
                for (final Choice c : choices) {
                    addChoiceButton(c, converser, agent);
                }
            }
        } else {
            // end of conversation
            container.setVisible(false);
        }
    }

    private void endDialogue() {
        container.setVisible(false);
        active = false;
    }

    private void addAnnouncement(Agent agent, Announcement announcement) {
        TextBubble bubble = new TextBubble(agent, announcement);
        timedBubbles.add(bubble);
        speaking.add(bubble.owner);
    }

    private void addLabel(String text) {
        addLabel(bubble, text);
    }

    private void addLabel(Table bubble, String text) {
        Label label = new Label(text, skin);
        label.setWrap(true);
        label.setWidth(10);
        label.setHeight(10);

        @SuppressWarnings("rawtypes")
        Cell cell = bubble.add(label).minWidth(250).maxWidth(500);
        bubble.pack(); // defines the text width and thus the preferred height
        cell.height(label.getPrefHeight());
    }

    private void addChoiceButton(final Choice c, final Conversable converser, final Agent agent) {
        TextButtonStyle buttonStyle = skin.get("choice", TextButtonStyle.class);
        final TextButton choice = new TextButton(c.getText(), buttonStyle);
        choice.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Response response = converser.getDialogueHandler().getResponseFor(c, agent);
                setup(converser, agent, response);

                // this will remove the text bubble for choices without successors
                if (response == null) {
                    converser.endDialogue();
                }
            }
        });
        choiceBar.add(choice).left().padLeft(50).padRight(25).padBottom(10);
        choiceBar.row();
    }

    private void setPosition(Conversable converser, Camera camera) {
        setPosition(bubble, converser, camera);
    }

    private void setPosition(Table bubble, Conversable converser, Camera camera) {
        Vector2 position = converser.getRenderPosition();
        float h = converser.getHeight() / 2;
        float w = converser.getWidth() / 2;
        Vector3 screen = camera.project(new Vector3(position.x - w, position.y + h, 0));
        bubble.setPosition(screen.x, screen.y);
    }

    private class TextBubble {
        private final Agent owner;
        private final Announcement announcement;
        private final Table table;
        private final float duration;
        private float elapsed = 0;

        public TextBubble(Agent owner, Announcement announcement) {
            this.owner = owner;
            this.announcement = announcement;
            table = new Table(null);
            table.bottom();
            addLabel(table, announcement.getText());

            duration = getDuration(announcement.getText());
        }

        public void update(float delta, Camera camera) {
            elapsed += delta;
            setPosition(table, owner, camera);
        }

        public boolean isStarted() {
            // wait to set awake to prevent drawing in odd locations
            return elapsed > 0;
        }

        public boolean isFinished() {
            return elapsed >= duration;
        }
        
        public void onFinish() {
            announcement.onFinish();
        }
        
        private float getDuration(String text) {
            return 2f + text.length() / 20f;
        }
    }
}
