package com.eldritch.invoken.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class CharacterCreationScreen extends AbstractScreen {
    private final Level level;
    private final OrthographicCamera camera;
    
    private Profession selectedProfession = Profession.Centurion;
    private ScrollPane scroll;
    private SplitPane descriptionPane;
    
	public CharacterCreationScreen(InvokenGame game, Level level, OrthographicCamera camera) {
        super(game);
        this.level = level;
        this.camera = camera;
    }

	@Override
	public void show() {
		super.show();
		
		// retrieve the default table actor
		Table container = super.getTable();
		container.setSize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
		container.left();
		
		Table table = new Table(getSkin());
		table.left();
		for (Profession p : Profession.values()) {
            TextButton button = createMenuItem(p);
            
            // register the button
            table.add(button).size(200, 50).uniform().spaceBottom(10);
            table.row();
        }
		container.add(table);
		
		scroll = new ScrollPane(getInfoTable(), getSkin());
        scroll.setVisible(false);
        container.add(scroll).expandX().fill().bottom().uniform().space(25);
	}
	
	@Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        level.render(delta, camera, null, false);
        stage.act(delta);
        stage.draw();
    }
	
	private Table getInfoTable() {
	    Table rightTable = new Table(getSkin());
	    
	    Table iconTable = new Table(getSkin());
	    for (Texture icon : selectedProfession.getIcons()) {
	        Image image = new Image(icon);
	        iconTable.add(image).left().uniform().space(10);
	    }
	    rightTable.add(iconTable).space(20);
	    rightTable.row();
	    
	    TextArea area = new TextArea(selectedProfession.getDescription(), getSkin());
	    rightTable.add(area).expand().fill();
	    
        descriptionPane = new SplitPane(
                getPlayerView(), rightTable,
                false, getSkin(), "default-horizontal");
        
        Table infoTable = new Table(getSkin());
        infoTable.add(descriptionPane).expand().fill().spaceBottom(50);

        infoTable.row();
        infoTable.bottom().right();
        TextButton start = createStartButton();
        infoTable.add(start).size(200, 50).right();
        
        return infoTable;
	}
	
	private void setInfoTable(Table table) {
	    scroll.setWidget(table);
	}
	
    private TextButton createMenuItem(final Profession p) {
        TextButton button = new TextButton(p.name(), getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                selectedProfession = p;
                setInfoTable(getInfoTable());
                scroll.setVisible(true);
            }
        });
        return button;
    }
    
    private TextButton createStartButton() {
        TextButton button = new TextButton("Play", getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                game.setScreen(new GameScreen(game, selectedProfession));
            }
        });
        return button;
    }
    
    private ScrollPane getPlayerView() {
        return new ScrollPane(new Image(selectedProfession.getPortrait()));
    }
}