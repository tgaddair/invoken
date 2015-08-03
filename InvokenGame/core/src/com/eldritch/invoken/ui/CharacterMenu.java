package com.eldritch.invoken.ui;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.actor.AgentInfo;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.factions.Faction;
import com.eldritch.invoken.actor.factions.FactionManager;
import com.eldritch.invoken.actor.items.Core;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.util.Settings;

public class CharacterMenu implements HudElement {
    private final Table container;
    private final Table table;
    private final Skin skin;
    private final Player player;

    private final Map<Discipline, Integer> attributes = new EnumMap<>(Discipline.class);

    public CharacterMenu(Player player, Skin skin) {
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
    }

    public void toggle() {
        show(!container.isVisible());
    }

    private void show(boolean visible) {
        container.setVisible(visible);
        if (visible) {
            refresh();
        }
    }

    private void refresh() {
        table.clear();

        // init attributes
        attributes.clear();
        for (Discipline d : Discipline.values()) {
            attributes.put(d, player.getInfo().getSkillLevel(d));
        }

        Table leftTable = new Table(skin);
        leftTable.top();

        // level and fragments
        {
            Label infoLabel = new Label("Overview", skin);
            leftTable.add(infoLabel).left().expandX().fillX().space(25);
            leftTable.row();

            addLabel("Name", player.getInfo().getName(), leftTable);
            addLabel("Profession", player.getInfo().getProfession().name(), leftTable);

            int level = player.getInfo().getLevel();
            addLabel("Level", String.valueOf(level), leftTable);

            int currentFragments = player.getInventory().getItemCount(Fragment.getInstance());
            int requiredFragments = AgentInfo.getFragmentRequirement(level + 1);
            String fragmentsText = String.format("%d / %d", currentFragments, requiredFragments);
            addLabel("Fragments", fragmentsText, leftTable);
            
            String coreText = player.getInventory().hasItem(Core.getInstance()) ? "Yes" : "No";
            addLabel("Core", coreText, leftTable);
            
            String backupText = player.hasBackup() ? "Yes" : "No";
            addLabel("Backup", backupText, leftTable);
        }

        // disciplines
        {
            Label disciplineLabel = new Label("Disciplines", skin);
            leftTable.add(disciplineLabel).left().expandX().fillX().space(25);
            leftTable.row();

            Table disciplinesTable = new Table(skin);
            for (Discipline d : Discipline.values()) {
                Table disciplineTable = createTable(d);
                disciplinesTable.add(disciplineTable).fillX().expandX();
                disciplinesTable.row();
            }
            leftTable.add(disciplinesTable).expandX().fillX();
            leftTable.row();
        }

        Table rightTable = new Table(skin);
        rightTable.top();

        // factions
        {
            Label factionsLabel = new Label("Factions", skin);
            rightTable.add(factionsLabel).left().expandX().fillX().space(25);
            rightTable.row();

            FactionManager factions = player.getInfo().getFactionManager();
            for (Faction faction : factions.getFactions()) {
                if (!faction.isVisible()) {
                    continue;
                }

                String key = faction.getName();

                String value = "";
                int rank = factions.getRank(faction);
                if (rank > 0) {
                    if (faction.hasTitle(rank)) {
                        value = faction.getTitle(rank);
                    } else {
                        value = String.valueOf(factions.getReputation(faction));
                    }
                }

                addLabel(key, value, rightTable);
            }
        }

        // missions
        {
            Label factionsLabel = new Label("Missions", skin);
            rightTable.add(factionsLabel).left().expandX().fillX().space(25);
            rightTable.row();
            
            // TODO
        }

        SplitPane splitPane = new SplitPane(leftTable, rightTable, false, skin,
                "default-horizontal");

        // add the top table
        table.add(splitPane).expand().fill();
        table.row();
    }

    private Label addLabel(String key, String value, Table table) {
        Label label = new Label(String.format("[WHITE]%s:   [CYAN]%s", key, value), skin.get(
                "default-nobg", LabelStyle.class));
        table.add(label).left().expandX().fillX().padLeft(25);
        table.row();
        return label;
    }

    private Table createTable(final Discipline d) {
        Table table = new Table(skin);
        table.left();

        // image
        Image image = new Image(Profession.getIcon(d));
        table.add(image).size(30, 30).padLeft(25);

        // label
        String skillLevel = String.valueOf(player.getInfo().getSkillLevel(d));
        addLabel(d.name(), skillLevel, table);

        return table;
    }
}
