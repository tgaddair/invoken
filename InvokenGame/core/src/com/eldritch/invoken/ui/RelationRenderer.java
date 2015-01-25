package com.eldritch.invoken.ui;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.actor.type.Agent;

public class RelationRenderer {
    private final BitmapFont debugFont = new BitmapFont();
    ShapeRenderer sr = new ShapeRenderer();
    SpriteBatch batch = new SpriteBatch();
    
	public void renderDispositions(
			Agent target, Collection<Agent> agents, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        for (Agent other : agents) {
        	if (target == other) {
        		// don't draw a relation edge to yourself
        		continue;
        	}
        	
        	float relation = target.getRelation(other);
        	float val = relation;
        	
        	// 0 = red, 60 = yellow, 120 = green
        	float hue = Math.min(Math.max(((val + 100) / 200f) * 120, 0), 120) / 360f;
        	float saturation = 1;
        	float brightness = 1;
        	java.awt.Color hsv = java.awt.Color.getHSBColor(hue, saturation, brightness);
        	Color c = new Color(hsv.getRed() / 255f, hsv.getGreen() / 255f, hsv.getBlue() / 255f, 1f);
        	sr.setColor(c);
        	sr.line(target.getPosition().x, target.getPosition().y,
                    other.getPosition().x, other.getPosition().y);
        	
        	// draw number
        	Vector3 screen = camera.project(new Vector3(
        			(target.getPosition().x + other.getPosition().x) / 2f,
                    (target.getPosition().y + other.getPosition().y) / 2f, 0));
        	batch.begin();
        	debugFont.draw(batch,
        			String.format("%.2f", relation), screen.x, screen.y);
        	batch.end();
        }
        sr.end();
	}
	
	public void renderLineOfSight(
			Agent target, Collection<Agent> agents, OrthographicCamera camera) {
		sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        for (Agent other : agents) {
        	if (target == other) {
        		// don't draw a relation edge to yourself
        		continue;
        	}
        	
        	boolean los = target.hasLineOfSight(other);
        	Color c = los ? Color.GREEN : Color.RED;
        	sr.setColor(c);
        	sr.line(target.getPosition().x, target.getPosition().y,
                    other.getPosition().x, other.getPosition().y);
        }
        sr.end();
	}
	
	public void renderEnemies(
            Agent target, Collection<Agent> agents, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        for (Agent other : agents) {
            if (target == other) {
                // don't draw a relation edge to yourself
                continue;
            }
            
            if (target.hostileTo(other)) {
                Color c = other == target.getTarget() ? Color.BLUE : Color.RED;
                sr.setColor(c);
                sr.line(target.getPosition().x, target.getPosition().y,
                        other.getPosition().x, other.getPosition().y);
            }
        }
        sr.end();
    }
}
