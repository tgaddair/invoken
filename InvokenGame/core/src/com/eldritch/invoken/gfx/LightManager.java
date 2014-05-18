package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.screens.AbstractScreen;

public class LightManager {
    // values passed to the shader
    public static final float ambientIntensity = .7f;
    public static final Vector3 ambientColor = new Vector3(0.3f, 0.3f, 0.7f);

    // used to make the light flicker
    public static final float zSpeed = 15.0f;
    public static final float PI2 = (float) (Math.PI * 2.0);
    public float zAngle;

    private ShaderProgram defaultShader;
    private ShaderProgram ambientShader;
    private ShaderProgram lightShader;
    private ShaderProgram finalShader;

    final String defaultPixelShader = new FileHandle("shader/defaultPixelShader.glsl").readString();
    final String ambientPixelShader = new FileHandle("shader/ambientPixelShader.glsl").readString();
    final String vertexShader = new FileHandle("shader/vertexShader.glsl").readString();
    final String lightPixelShader =  new FileHandle("shader/lightPixelShader.glsl").readString();
    final String finalPixelShader = new FileHandle("shader/pixelShader.glsl").readString();

    private final List<Light> lights = new ArrayList<Light>();
    private FrameBuffer fbo;

    public LightManager() {
        ShaderProgram.pedantic = false;
        defaultShader = new ShaderProgram(vertexShader, defaultPixelShader);
        ambientShader = new ShaderProgram(vertexShader, ambientPixelShader);
        lightShader = new ShaderProgram(vertexShader, lightPixelShader);
        finalShader = new ShaderProgram(vertexShader, finalPixelShader);
        
        ambientShader.begin();
        ambientShader.setUniformf("ambientColor", ambientColor.x, ambientColor.y,
                ambientColor.z, ambientIntensity);
        ambientShader.end();

        lightShader.begin();
        lightShader.setUniformi("u_lightmap", 1);
        lightShader.end();

        finalShader.begin();
        finalShader.setUniformi("u_lightmap", 1);
        finalShader.setUniformf("ambientColor", ambientColor.x, ambientColor.y, ambientColor.z,
                ambientIntensity);
        finalShader.end();

        int w = AbstractScreen.MENU_VIEWPORT_WIDTH;
        int h = AbstractScreen.MENU_VIEWPORT_HEIGHT;

        fbo = new FrameBuffer(Format.RGBA8888, w, h, false);

        lightShader.begin();
        lightShader.setUniformf("resolution", w, h);
        lightShader.end();

        finalShader.begin();
        finalShader.setUniformf("resolution", w, h);
        finalShader.end();
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public void render(OrthogonalTiledMapRenderer renderer, float delta) {
        // draw lights to frame buffer
        zAngle += delta * zSpeed;
        while (zAngle > PI2) {
            zAngle -= PI2;
        }

        // draw the light to the FBO
        Batch batch = renderer.getSpriteBatch();
        fbo.begin();
        batch.setShader(defaultShader);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        for (Light light : lights) {
            light.render(batch, zAngle);
        }
        batch.end();
        fbo.end();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setShader(finalShader);
        fbo.getColorBufferTexture().bind(1); // this is important! bind the FBO to the 2nd texture
                                             // unit
        for (Light light : lights) {
            light.bind(0);
        }
        // light.bind(0); //we force the binding of a texture on first texture unit to avoid
        // artefacts
        // this is because our default and ambiant shader dont use multi texturing...
        // youc can basically bind anything, it doesnt matter
    }
}
