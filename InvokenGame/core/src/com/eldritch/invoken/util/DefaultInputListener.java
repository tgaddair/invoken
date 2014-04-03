package com.eldritch.invoken.util;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * An utility {@link ActorListener} class.
 * <p>
 * Defines the {@link #touchDown(ActorEvent, float, float, int, int)} method
 * returning <code>true</code> by default, so the
 * {@link #touchDown(ActorEvent, float, float, int, int)} method gets invoked.
 */
public abstract class DefaultInputListener extends InputListener {
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		return true;
	}
}