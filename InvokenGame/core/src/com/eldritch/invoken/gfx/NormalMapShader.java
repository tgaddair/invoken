package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.eldritch.invoken.actor.type.Player;

public class NormalMapShader {
    // our constants...
    public static final float DEFAULT_LIGHT_Z = 0.075f;
    public static final float AMBIENT_INTENSITY = 0.5f;
    public static final float DEFAULT_LIGHT_INTENSITY = .15f;
    
    // TODO: future biome has:
    //  ambient intensity = 0.9f
    //  light intensity = 0.1f
    //  light color = new Vector3(0.8f, 0.6f, 0.9f)
    
    public static float LIGHT_INTENSITY = DEFAULT_LIGHT_INTENSITY;

    public static final Vector3 LIGHT_POS = new Vector3(0f, 0f, DEFAULT_LIGHT_Z);

    // Light RGB and intensity (alpha)
//    public static final Vector3 LIGHT_COLOR = new Vector3(0.8f, 0.6f, 0.9f);  // subtle blue
//    public static final Vector3 LIGHT_COLOR = new Vector3(0.3f, 0.4f, 1f);  // strong blue
//    public static final Vector3 LIGHT_COLOR = new Vector3(1, 0.8f, 0.6f);  // default
    public static final Vector3 LIGHT_COLOR = new Vector3(0.9f, 0.4f, 0.2f);  // warm
//    public static final Vector3 LIGHT_COLOR = new Vector3(0.71f, 0.25f, 0.05f).scl(0.5f);  // rust

    // Ambient RGB and intensity (alpha)
    public static final Vector3 AMBIENT_COLOR = new Vector3(0.6f, 0.6f, 1f);

    // Attenuation coefficients for light falloff
    // the greater the falloff, the lower the dispersion of light
    public static final Vector3 FALLOFF = new Vector3(.005f, .005f, 3f);

    final String VERT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
            + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

            "uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n"
            + "varying vec2 vTexCoord;\n" +

            "void main() {\n" + "   vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "   vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";

    // no changes except for LOWP for color values
    final String FRAG = Gdx.files.internal("shader/normalMappedShader.glsl").readString();
    
    private final List<Light> visibleLights = new ArrayList<Light>();
    private float[] values;
    private float[] colors;

    ShaderProgram shader;
    SpriteBatch batch;
    FrameBuffer fbo;
    FrameBuffer emptyFrame;
    
    public NormalMapShader() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERT, FRAG);

        // ensure it compiled
        if (!shader.isCompiled())
            throw new GdxRuntimeException("Could not compile shader: " + shader.getLog());
        // print any warnings
        if (shader.getLog().length() != 0)
            System.out.println(shader.getLog());

        // setup default uniforms
        shader.begin();

        // our normal map and lights
        shader.setUniformi("u_normals", 1); // GL_TEXTURE1
        shader.setUniformi("u_lights", 2); // GL_TEXTURE2
        shader.setUniformi("u_overlay", 3); // GL_TEXTURE2
        
        shader.setUniform3fv("lightGeometry", new float[] {}, 0, 0);
        shader.setUniform3fv("lightColors", new float[] {}, 0, 0);
        shader.setUniformi("lightCount", 0);
        shader.setUniformi("useNormal", 0);

        // light/ambient colors
        // LibGDX doesn't have Vector4 class at the moment, so we pass them individually...
        //shader.setUniformf("LightColor", LIGHT_COLOR.x, LIGHT_COLOR.y, LIGHT_COLOR.z,
        //        LIGHT_INTENSITY);
        shader.setUniformf("AmbientColor", AMBIENT_COLOR.x, AMBIENT_COLOR.y, AMBIENT_COLOR.z,
                AMBIENT_INTENSITY);
        shader.setUniformf("Falloff", FALLOFF);

        // LibGDX likes us to end the shader program
        shader.end();
    }
    
    public ShaderProgram getShader() {
        return shader;
    }
    
    public void useNormalMap(boolean use) {
        shader.begin();
        shader.setUniformi("useNormal", use ? 1 : 0);
        shader.end();
    }
    
    public void resize(int width, int height) {
        shader.begin();
        shader.setUniformf("Resolution", width, height);
        shader.end();
        
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
        emptyFrame = new FrameBuffer(Format.RGBA8888, width, height, false);
    }
    
    public void setLightGeometry(List<Light> lights, Rectangle worldBounds) {
        Circle c = new Circle();
        visibleLights.clear();
        for (Light light : lights) {
            Vector2 position = light.getPosition();
            c.set(position, light.getRadius());
            if (Intersector.overlaps(c, worldBounds)) {
                visibleLights.add(light);
//                System.out.println("light: " + screen.x + ", " + screen.y);
            }
        }
//        System.out.println("visible lights: " + visibleLights.size());
        values = new float[(visibleLights.size() + 1) * 3];
        colors = new float[(visibleLights.size() + 1) * 4];
    }
    
    private void updateLightGeometry(OrthographicCamera camera, Player player) {
        for (int i = 0; i < visibleLights.size(); i++) {
            Light light = visibleLights.get(i);
            Vector2 position = light.getPosition();
            Vector3 screen = camera.project(new Vector3(position.x, position.y, 0));
            values[i * 3 + 0] = screen.x;
            values[i * 3 + 1] = screen.y;
            values[i * 3 + 2] = light.getRadius();
            
            Color color = light.getColor();
            colors[i * 4 + 0] = color.r;
            colors[i * 4 + 1] = color.g;
            colors[i * 4 + 2] = color.b;
            colors[i * 4 + 3] = DEFAULT_LIGHT_INTENSITY;
        }
        
        values[visibleLights.size() * 3 + 0] = LIGHT_POS.x;
        values[visibleLights.size() * 3 + 1] = LIGHT_POS.y;
        values[visibleLights.size() * 3 + 2] = player.hasLightOn() ? 3 : 0;
        
        colors[visibleLights.size() * 4 + 0] = 1;
        colors[visibleLights.size() * 4 + 1] = 1;
        colors[visibleLights.size() * 4 + 2] = 1;
        colors[visibleLights.size() * 4 + 3] = DEFAULT_LIGHT_INTENSITY;
        
        shader.begin();
        shader.setUniform3fv("lightGeometry", values, 0, values.length);
        shader.setUniform4fv("lightColors", colors, 0, colors.length);
        shader.setUniformi("lightCount", visibleLights.size() + 1);
        shader.end();
    }
    
    public void begin() {
        // send a Vector4f to GLSL
        shader.setUniformf("LightPos", LIGHT_POS);
    }

    public void render(LightManager lightManager, Player player, float delta, OrthographicCamera camera, OrthogonalShadedTiledMapRenderer... renderers) {
        // update light position, normalized to screen resolution
        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY() - 1;
        LIGHT_POS.x = x;
        LIGHT_POS.y = y;
        LIGHT_POS.z = 0.1f / camera.zoom;
//        LIGHT_INTENSITY = DEFAULT_LIGHT_INTENSITY / camera.zoom;
        
        // shader will now be in use...
        updateLightGeometry(camera, player);

        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for (OrthogonalShadedTiledMapRenderer renderer : renderers) {
            renderer.setNormalRender(true);
            renderer.render();
            renderer.setNormalRender(false);
        }
        fbo.end();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // this is important! bind the FBO to the 2nd texture unit
        fbo.getColorBufferTexture().bind(1);
    }
}
