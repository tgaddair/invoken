package com.eldritch.invoken.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.MusicManager;

public class MenuScreen extends AbstractScreen {
	public MenuScreen(InvokenGame game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();

		// retrieve the default table actor
		Table table = super.getTable();
		table.add("Welcome to Invoken for Android!").spaceBottom(50);
		table.row();

		// register the button "start game"
		TextButton startGameButton = new TextButton("New Game", getSkin());
		startGameButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				//game.getSoundManager().play(TyrianSound.CLICK);
				game.setScreen(new CharacterCreationScreen(game));
			}
		});
		table.add(startGameButton).size(300, 60).uniform().spaceBottom(10);
		table.row();

		// register the button "options"
		TextButton optionsButton = new TextButton("Options", getSkin());
		optionsButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				//game.getSoundManager().play(TyrianSound.CLICK);
				//game.setScreen(new OptionsScreen(game));
			}
		});
		table.add(optionsButton).uniform().fill().spaceBottom(10);
		table.row();

		// register the button "high scores"
		TextButton highScoresButton = new TextButton("High Scores", getSkin());
		highScoresButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				//game.getSoundManager().play(TyrianSound.CLICK);
				//game.setScreen(new HighScoresScreen(game));
			}
		});
		table.add(highScoresButton).uniform().fill();
		
		// play title music
		InvokenGame.MUSIC_MANAGER.play(MusicManager.MAIN);
	}
}