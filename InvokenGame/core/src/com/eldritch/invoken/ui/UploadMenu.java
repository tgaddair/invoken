package com.eldritch.invoken.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class UploadMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;

    private final List<LevelListener> listeners = new ArrayList<>();
    private final Map<Discipline, Integer> attributes = new EnumMap<>(Discipline.class);
    private final Set<Augmentation> preparedAugs = new HashSet<>();

    private int level;
    private int currentFragments;
    private int requiredFragments;
    private boolean active = false;

    public UploadMenu(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;

        table = new Table(skin);
        table.top();

        container = new Table(skin);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.center();

        ScrollPane scroll = new ScrollPane(table, skin);
        container.add(scroll).expand().fill();
        container.setVisible(false);
    }

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void resize(int width, int height) {
        container.setHeight(height - 100);
        container.setWidth(width - 100);
        container.setPosition(width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
        container.center();
    }

    @Override
    public void update(float delta, Level level) {
        if (player.isUploading()) {
            if (!active) {
                setActive(true);
                refresh();
            }
        } else {
            setActive(false);
        }
    }

    private void setActive(boolean value) {
        if (active != value) {
            container.setVisible(value);
            active = value;
        }
    }

    private void refresh() {
        table.clear();
        listeners.clear();

        // init attributes
        attributes.clear();
        for (Discipline d : Discipline.values()) {
            attributes.put(d, player.getInfo().getSkillLevel(d));
        }

        // init prepared augs
        preparedAugs.clear();
        for (Augmentation aug : player.getInfo().getAugmentations().getAugmentations()) {
            preparedAugs.add(aug);
        }

        Table topTable = new Table(skin);
        topTable.top();

        // level and fragments
        {
            level = player.getInfo().getLevel();
            final Label levelLabel = new Label("Level: " + level, skin);
            topTable.add(levelLabel).left().expandX().fillX().space(25);
            topTable.row();

            currentFragments = player.getInventory().getItemCount(Fragment.getInstance());
            requiredFragments = AgentInfo.getFragmentRequirement(level + 1);
            String fragmentsText = String.format("%d / %d", currentFragments, requiredFragments);
            final Label fragmentsLabel = new Label(fragmentsText, skin.get("default-nobg", LabelStyle.class));
            fragmentsLabel.setColor(canLevel() ? Color.WHITE : Color.GRAY);
            topTable.add(fragmentsLabel).left().expandX().fillX().space(10).padLeft(25);
            topTable.row();

            listeners.add(new LevelListener() {
                @Override
                public void onLevel() {
                    levelLabel.setText("Level: " + level);
                    
                    String text = String.format("%d / %d", currentFragments, requiredFragments);
                    fragmentsLabel.setText(text);
                    fragmentsLabel.setColor(canLevel() ? Color.WHITE : Color.GRAY);
                }
            });
        }

        // disciplines
        {
            Label disciplineLabel = new Label("Disciplines", skin);
            topTable.add(disciplineLabel).left().expandX().fillX().space(25);
            topTable.row();

            int i = 0;
            Table disciplinesTable = new Table(skin);
            for (Discipline d : Discipline.values()) {
                Table disciplineTable = createTable(d);
                disciplinesTable.add(disciplineTable).fillX().expandX().space(25);
                if (++i % 2 == 0) {
                    disciplinesTable.row();
                }
            }
            topTable.add(disciplinesTable).expandX().fillX().space(10);
            topTable.row();
        }

        // augmentations
        {
            Label augLabel = new Label("Augmentations", skin);
            topTable.add(augLabel).left().expandX().fillX().space(25);
            topTable.row();

            int i = 0;
            Table augTable = new Table(skin);
            for (final Augmentation aug : player.getInfo().getKnownAugmentations()) {
                final Image image = new Image(aug.getIcon());
                image.addListener(new DefaultInputListener() {
                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        toggle(aug, image);
                    }
                });

                // initial color
                image.setColor(preparedAugs.contains(aug) ? Color.WHITE : Color.GRAY);

                augTable.add(image).size(48, 48).space(50);
                if (++i % 5 == 0) {
                    augTable.row();
                }
            }
            topTable.add(augTable).expandX().fillX().space(25);
            topTable.row();
        }

        // add the top table
        table.add(topTable).expand().fill();
        table.row();

        // commit button
        table.bottom().right();
        TextButton start = createCommitButton();
        table.add(start).size(200, 50).right();
    }

    private void increaseLevel() {
        level++;
        currentFragments -= requiredFragments;
        requiredFragments = AgentInfo.getFragmentRequirement(level + 1);
        onLevel();
    }

    private void decreaseLevel() {
        level--;
        requiredFragments = AgentInfo.getFragmentRequirement(level + 1);
        currentFragments += requiredFragments;
        onLevel();
    }

    private void commit() {
        int fragments = player.getInventory().getItemCount(Fragment.getInstance())
                - currentFragments;
        player.getInfo().levelUp(level, attributes, fragments);
        player.getInfo().setPreparedAugmentations(preparedAugs);
    }

    private void onLevel() {
        for (LevelListener listener : listeners) {
            listener.onLevel();
        }
    }

    private boolean canLevel() {
        return currentFragments >= requiredFragments;
    }

    private void toggle(Augmentation aug, Image image) {
        if (preparedAugs.contains(aug)) {
            preparedAugs.remove(aug);
            image.setColor(Color.GRAY);
        } else {
            preparedAugs.add(aug);
            image.setColor(Color.WHITE);
        }
    }

    private Table createTable(final Discipline d) {
        Table table = new Table(skin);

        // image
        Image image = new Image(Profession.getIcon(d));
        table.add(image).size(50, 50).space(10);

        // label
        Label label = new Label(d.name(), skin.get("default-nobg", LabelStyle.class));
        label.setColor(Color.GRAY);
        table.add(label).size(150, 50).space(10);

        // current value
        String skillLevel = String.valueOf(player.getInfo().getSkillLevel(d));
        final TextButton button = new TextButton(skillLevel, skin);
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int buttonNumber) {
                super.touchUp(event, x, y, pointer, buttonNumber);

                if (buttonNumber == Input.Buttons.LEFT && canLevel()) {
                    // increment
                    int value = attributes.get(d) + 1;
                    attributes.put(d, value);
                    button.setText(String.valueOf(value));
                    increaseLevel();
                } else if (buttonNumber == Input.Buttons.RIGHT
                        && level > player.getInfo().getLevel()) {
                    // decrement
                    int value = attributes.get(d) - 1;
                    attributes.put(d, value);
                    button.setText(String.valueOf(value));
                    decreaseLevel();
                }
            }
        });
        listeners.add(new LevelListener() {
            @Override
            public void onLevel() {
                button.getLabel().setColor(canLevel() ? Color.WHITE : Color.GRAY);
            }
        });
        button.getLabel().setColor(canLevel() ? Color.WHITE : Color.GRAY);

        table.add(button).size(50, 50).space(25);
        return table;
    }

    private TextButton createCommitButton() {
        TextButton button = new TextButton("Commit", skin);
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                commit();
                GameScreen.toast("Saving...");
                GameScreen.save(player.getLocation());
            }
        });
        return button;
    }

    private interface LevelListener {
        void onLevel();
    }
}
