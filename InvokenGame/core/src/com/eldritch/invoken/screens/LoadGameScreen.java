package com.eldritch.invoken.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.Player.PlayerDescription;
import com.eldritch.invoken.actor.type.Player.SavedPlayerDescription;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class LoadGameScreen extends AbstractScreen {
    private final Map<Integer, List<Label>> rows = new HashMap<>();
    private final Level level;
    private final OrthographicCamera camera;
    
    private Table container;
    private TextButton start;
    private String selectedId;

    public LoadGameScreen(InvokenGame game, Level level, OrthographicCamera camera) {
        super(game);
        this.level = level;
        this.camera = camera;
    }

    @Override
    public void show() {
        super.show();
        
        Skin skin = getSkin();
        Table table = new Table(skin);

        container = super.getTable();

        ScrollPane scroll = new ScrollPane(getInfoTable(table), skin);
        container.add(scroll).expand().fill().uniform().pad(100);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        level.render(delta, camera, null, false);
        stage.act(delta);
        stage.draw();
    }

    private Table getInfoTable(Table table) {
        Skin skin = getSkin();
        skin.getFont("default-font").getData().markupEnabled = true;
        
        Table savesTable = new Table(skin);
        savesTable.top().left();
        
        // add header
        String formatting = "[CYAN]";
        addLabel(formatting + "Name", savesTable);
        addLabel(formatting + "Level", savesTable);
        addLabel(formatting + "Profession", savesTable);
        addLabel(formatting + "Region", savesTable);
        addLabel(formatting + "Floor", savesTable);
        savesTable.row();

        // add saves
        int i = 0;
        for (PlayerActor saved : Player.readSaves()) {
            ActorParams params = saved.getParams();
            String id = params.getId();
            addLabel(params.getName(), savesTable, id, i);
            addLabel(String.valueOf(params.getLevel()), savesTable, id, i);
            addLabel(Profession.fromProto(params.getProfession()).name(), savesTable, id, i);
            addLabel(saved.getRegion(), savesTable, id, i);
            addLabel(String.valueOf(saved.getFloor()), savesTable, id, i);
            savesTable.row();
            i++;
        }
        
        table.add(savesTable).expand().fill();
        table.row();

        Table buttons = new Table(skin);
        
        start = createStartButton();
        start.setVisible(false);
        
        TextButton back = createBackButton();
        buttons.add(back).size(200, 50).spaceRight(10).right();
        buttons.add(start).size(200, 50).right();

        table.bottom();
        table.add(buttons);

        return table;
    }
    
    private Label addLabel(String text, Table table) {
        Label label = new Label(text, getSkin().get("default-nobg", LabelStyle.class));
        table.add(label).space(25).expandX();
        return label;
    }
    
    private Label addLabel(String text, Table table, final String id, final int row) {
        if (!rows.containsKey(row)) {
            rows.put(row, new ArrayList<Label>());
        }
        
        Label label = new Label(text, getSkin().get("default-nobg", LabelStyle.class));
        label.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                InvokenGame.SOUND_MANAGER.play(SoundEffect.CLICK);
                for (int i = 0; i < rows.size(); i++) {
                    if (i == row) {
                        for (Label label : rows.get(i)) {
                            label.setColor(Color.GREEN);
                        }
                    } else {
                        for (Label label : rows.get(i)) {
                            label.setColor(Color.WHITE);
                        }
                    }
                }
                selectedId = id;
                start.setVisible(true);
            }
        });
        
        table.add(label).space(25).expandX();
        rows.get(row).add(label);
        return label;
    }

    private TextButton createStartButton() {
        TextButton button = new TextButton("Play", getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                PlayerDescription loader = SavedPlayerDescription.from(selectedId);
                game.setScreen(new GameScreen(game, loader));
            }
        });
        return button;
    }

    private TextButton createBackButton() {
        TextButton button = new TextButton("Back", getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                game.setScreen(new MenuScreen(game, level, camera));
            }
        });
        return button;
    }

//    private ScrollPane getPlayerView() {
//        return new ScrollPane(new Image(selectedProfession.getPortrait()));
//    }
}