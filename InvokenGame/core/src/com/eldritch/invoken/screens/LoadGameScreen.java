package com.eldritch.invoken.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.actor.type.Player.NewPlayerDescription;
import com.eldritch.invoken.actor.type.Player.PlayerDescription;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class LoadGameScreen extends AbstractScreen {
    private final Level level;
    private final OrthographicCamera camera;
    private Table container;

    private TextField nameField;
    private Profession selectedProfession = Profession.Centurion;

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
        
        Table savesTable = new Table(skin);
        savesTable.top().left();

        for (PlayerActor saved : Player.readSaves()) {
            ActorParams params = saved.getParams();
            addLabel(params.getName(), savesTable);
            addLabel(String.valueOf(params.getLevel()), savesTable);
            addLabel(Profession.fromProto(params.getProfession()).name(), savesTable);
            addLabel(saved.getRegion(), savesTable);
            addLabel(String.valueOf(saved.getFloor()), savesTable);
            savesTable.row();
        }
        
        table.add(savesTable).expand().fill();
        table.row();

        Table buttons = new Table(skin);
        TextButton start = createStartButton();
        TextButton back = createBackButton();
        buttons.add(back).size(200, 50).spaceRight(10).right();
        buttons.add(start).size(200, 50).right();

        table.bottom();
        table.add(buttons);

        return table;
    }
    
    private Label addLabel(String text, Table table) {
        Label label = new Label(text, getSkin().get("default-nobg", LabelStyle.class));
        table.add(label).space(25);
        return label;
    }

    private TextButton createStartButton() {
        TextButton button = new TextButton("Play", getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                PlayerDescription loader = NewPlayerDescription.from(nameField.getText(),
                        selectedProfession);
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

    private ScrollPane getPlayerView() {
        return new ScrollPane(new Image(selectedProfession.getPortrait()));
    }
}