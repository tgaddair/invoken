package com.eldritch.invoken.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class GameOverScreen extends AbstractScreen {
    private final String id;
    
	public GameOverScreen(InvokenGame game, String id) {
		super(game);
		this.id = id;
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
        
        TextButton menuButton = new TextButton("Menu", getSkin());
        menuButton.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y,
                    int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                InvokenGame.SOUND_MANAGER.play(SoundEffect.CLICK);
                game.setScreen(new MenuScreen(game));
            }
        });
        table.add(menuButton);
	}
}