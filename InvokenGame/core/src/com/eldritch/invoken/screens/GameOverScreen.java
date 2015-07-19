package com.eldritch.invoken.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.util.DefaultInputListener;

public class GameOverScreen extends AbstractScreen {
    private final String player;
    
	public GameOverScreen(InvokenGame game, String player) {
		super(game);
		this.player = player;
	}

	@Override
	public void show() {
		super.show();

		// retrieve the default table actor
		Table table = super.getTable();
		table.center();
		
		LabelStyle headingStyle = new LabelStyle(getFont(), Color.WHITE);
        Label heading = new Label("Game Over!!", headingStyle);
        heading.setFontScale(2);
        
        table.add(heading).spaceBottom(75);
        table.row();

		// register the button "start game"
		TextButton startGameButton = new TextButton("Try Again?", getSkin());
		startGameButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				//game.getSoundManager().play(TyrianSound.CLICK);
				game.setScreen(new GameScreen(game, player));
			}
		});
		table.add(startGameButton).spaceBottom(15);
        table.row();
        
        TextButton menuButton = new TextButton("Menu", getSkin());
        menuButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                //game.getSoundManager().play(TyrianSound.CLICK);
                game.setScreen(new MenuScreen(game));
            }
        });
        table.add(menuButton);
	}
}