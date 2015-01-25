package com.eldritch.invoken.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.proto.Disciplines.Profession;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class CharacterCreationScreen extends AbstractScreen {
    private Profession selectedProfession = null;
    private ScrollPane scroll;
    
	public CharacterCreationScreen(InvokenGame game) {
		super(game);
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
		
		Table infoTable = new Table(getSkin());
		infoTable.bottom().right();
		
		TextButton start = createStartButton();
		infoTable.add(start).size(200, 50);
		
		scroll = new ScrollPane(infoTable, getSkin());
        scroll.setVisible(false);
        container.add(scroll).expand().fill().bottom();
	}
	
    private TextButton createMenuItem(final Profession p) {
        TextButton button = new TextButton(p.name(), getSkin());
        button.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                selectedProfession = p;
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
                //game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new GameScreen(game));
            }
        });
        return button;
    }
}