package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.eldritch.invoken.actor.type.Player;

public class NormalMapShader {
    // our constants...
    public static final float DEFAULT_LIGHT_Z = 0.075f;
//    public static final float AMBIENT_INTENSITY = 0.2f;
    public static final float AMBIENT_INTENSITY = 0.7f;
    public static final float LIGHT_INTENSITY = 1f;

    public static final Vector3 LIGHT_POS = new Vector3(0f, 0f, DEFAULT_LIGHT_Z);

    // Light RGB and intensity (alpha)
    public static final Vector3 LIGHT_COLOR = new Vector3(1f, 0.8f, 0.6f);

    // Ambient RGB and intensity (alpha)
    public static final Vector3 AMBIENT_COLOR = new Vector3(0.6f, 0.6f, 1f);

    // Attenuation coefficients for light falloff
    public static final Vector3 FALLOFF = new Vector3(.4f, 3f, 20f);

    final String VERT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
            + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

            "uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n"
            + "varying vec2 vTexCoord;\n" +

            "void main() {\n" + "   vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "   vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";

    // no changes except for LOWP for color values
    // we would store this in a file for increased readability
    final String FRAG = Gdx.files.internal("shader/normalMappedShader.glsl").readString();

    ShaderProgram shader;
    SpriteBatch batch;
    FrameBuffer fbo;

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

        // light/ambient colors
        // LibGDX doesn't have Vector4 class at the moment, so we pass them individually...
        shader.setUniformf("LightColor", LIGHT_COLOR.x, LIGHT_COLOR.y, LIGHT_COLOR.z,
                LIGHT_INTENSITY);
        shader.setUniformf("AmbientColor", AMBIENT_COLOR.x, AMBIENT_COLOR.y, AMBIENT_COLOR.z,
                AMBIENT_INTENSITY);
        shader.setUniformf("Falloff", FALLOFF);

        // LibGDX likes us to end the shader program
        shader.end();
    }
    
    public ShaderProgram getShader() {
        return shader;
    }
    
    public void resize(int width, int height) {
        shader.begin();
        shader.setUniformf("Resolution", width, height);
        shader.end();
        
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
    }
    
    public void setLightGeomtry(List<Light> lights, OrthographicCamera camera) {
        float[] values = new float[lights.size() * 3];
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            Vector2 position = light.getPosition();
            Vector3 world = camera.unproject(new Vector3(position.x, position.y, 0));
            values[i * 3 + 0] = world.x;
            values[i * 3 + 1] = world.y;
            values[i * 3 + 2] = light.getRadius();  // store radius in z
        }
        shader.setUniform3fv("lightGeomtry[0]", values, 0, lights.size());
        shader.setUniformi("lightCount", lights.size());
    }
    
    public void begin() {
        // send a Vector4f to GLSL
        shader.setUniformf("LightPos", LIGHT_POS);
    }

    public void render(LightManager lightManager, Player player, float delta, OrthogonalShadedTiledMapRenderer... renderers) {
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // reset light Z
//        LIGHT_POS.z = DEFAULT_LIGHT_Z;
//      System.out.println("New light Z: " + LIGHT_POS.z);

        // shader will now be in use...

        // update light position, normalized to screen resolution
        float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
        float y = 1 - Gdx.input.getY() / (float) Gdx.graphics.getHeight();
        LIGHT_POS.x = x;
        LIGHT_POS.y = y;

//        LIGHT_POS.x = player.getPosition().x;
//        LIGHT_POS.y = player.getPosition().y;

        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for (OrthogonalShadedTiledMapRenderer renderer : renderers) {
            renderer.setNormalRender(true);
            renderer.render();
            renderer.setNormalRender(false);
        }
        fbo.end();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // this is important! bind the FBO to the 2nd texture unit
        fbo.getColorBufferTexture().bind(1);
    }
}
