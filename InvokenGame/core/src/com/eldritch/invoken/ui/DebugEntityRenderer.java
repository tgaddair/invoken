package com.eldritch.invoken.ui;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.actor.ai.NpcThreatMonitor;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.actor.type.FixedPoint;
import com.eldritch.invoken.actor.type.Npc;

public class DebugEntityRenderer {
    private static final Vector3 screen = new Vector3();
    private final BitmapFont debugFont = new BitmapFont();
    ShapeRenderer sr = new ShapeRenderer();
    SpriteBatch batch = new SpriteBatch();

    public void renderDispositions(Agent target, Collection<Agent> agents, OrthographicCamera camera) {
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
            Color c = new Color(hsv.getRed() / 255f, hsv.getGreen() / 255f, hsv.getBlue() / 255f,
                    1f);
            sr.setColor(c);
            sr.line(target.getPosition().x, target.getPosition().y, other.getPosition().x,
                    other.getPosition().y);

            // draw number
            camera.project(screen.set((target.getPosition().x + other.getPosition().x) / 2f,
                    (target.getPosition().y + other.getPosition().y) / 2f, 0));
            batch.begin();
            debugFont.draw(batch, String.format("%.2f", relation), screen.x, screen.y);
            batch.end();
        }
        sr.end();
    }

    public void renderLineOfSight(Agent target, Collection<Agent> agents, OrthographicCamera camera) {
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
            sr.line(target.getPosition().x, target.getPosition().y, other.getPosition().x,
                    other.getPosition().y);
        }
        sr.end();
    }

    public void renderEnemies(Agent target, Collection<Agent> agents, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        for (Agent other : agents) {
            if (target == other) {
                // don't draw a relation edge to yourself
                continue;
            }

            if (target.getThreat().hostileTo(other)) {
                Color c = other == target.getTarget() ? Color.BLUE : Color.RED;
                sr.setColor(c);
                sr.line(target.getPosition().x, target.getPosition().y, other.getPosition().x,
                        other.getPosition().y);
            }
        }
        sr.end();
    }

    public void renderVisible(Agent target, Collection<Agent> agents, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        for (Agent other : target.getVisibleNeighbors()) {
            if (target == other) {
                // don't draw a relation edge to yourself
                continue;
            }

            Color c = Color.BLUE;
            sr.setColor(c);
            sr.line(target.getPosition().x, target.getPosition().y, other.getPosition().x,
                    other.getPosition().y);
        }
        sr.end();
    }

    public void drawBetween(Vector2 source, Vector2 target, OrthographicCamera camera) {
        drawBetween(source, target, Color.RED, camera);
    }

    public void drawBetween(Vector2 source, Vector2 target, Color color, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        sr.setColor(color);
        sr.line(target.x, target.y, source.x, source.y);
        sr.end();
    }

    public void renderCover(Agent target, List<FixedPoint> coverPoints, OrthographicCamera camera) {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Line);
        sr.setColor(Color.WHITE);
        for (FixedPoint point : coverPoints) {
            Vector2 position = point.getPosition();
            if (target != null) {
                sr.setColor(target.getLocation().hasLineOfSight(target, target.getPosition(),
                        position) ? Color.GREEN : Color.RED);
            }
            sr.circle(position.x, position.y, 0.5f);
        }
        sr.end();
    }

    public void renderThreat(Agent target, OrthographicCamera camera) {
        if (target == null || !(target instanceof Npc)) {
            return;
        }
        Npc npc = (Npc) target;
        renderCircle(npc.getPosition(), NpcThreatMonitor.ALERT_RADIUS, camera.combined, Color.RED);
        renderCircle(npc.getPosition(), NpcThreatMonitor.SUSPICION_RADIUS, camera.combined,
                Color.ORANGE);
    }

    public void renderLastSeen(Agent target, OrthographicCamera camera) {
        if (target == null || !(target instanceof Npc)) {
            return;
        }
        Npc npc = (Npc) target;
        renderCircle(npc.getLastSeen().getPosition(), 0.5f, camera.combined);
    }

    public void renderPathfinding(Agent target, OrthographicCamera camera) {
        if (target == null || !(target instanceof Npc)) {
            return;
        }
        Npc npc = (Npc) target;
        npc.getLastSeen().render(sr, camera.combined);
    }

    public void renderCircle(Vector2 position, float radius, Matrix4 projection) {
        renderCircle(position, radius, projection, Color.BLUE);
    }

    private void renderCircle(Vector2 position, float radius, Matrix4 projection, Color color) {
        sr.setProjectionMatrix(projection);
        sr.begin(ShapeType.Line);
        sr.setColor(color);
        sr.circle(position.x, position.y, radius);
        sr.end();
    }

    private DebugEntityRenderer() {
        // singleton
    }

    public static DebugEntityRenderer getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final DebugEntityRenderer INSTANCE = new DebugEntityRenderer();
    }
}
