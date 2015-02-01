package com.eldritch.invoken.gfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.eldritch.invoken.proto.Locations.Location;
import com.eldritch.invoken.util.Settings;

public class LightManager {
    // values passed to the shader
    public static final Vector3 pauseColor = new Vector3(0.3f, 0.6f, 0.9f);

    // used to make the light flicker
    public static final float zSpeed = 15.0f;
    public static final float PI2 = (float) (Math.PI * 2.0);
    public float zAngle;
    
    public final float ambientIntensity;
    public final Vector3 ambientColor;

    private ShaderProgram defaultShader;
    private ShaderProgram finalShader;
    private ShaderProgram pauseShader;

    final String defaultPixelShader = Gdx.files.internal("shader/defaultPixelShader.glsl").readString();
    final String ambientPixelShader = Gdx.files.internal("shader/ambientPixelShader.glsl").readString();
    final String vertexShader = Gdx.files.internal("shader/vertexShader.glsl").readString();
    final String lightPixelShader = Gdx.files.internal("shader/lightPixelShader.glsl").readString();
    final String finalPixelShader = Gdx.files.internal("shader/pixelShader.glsl").readString();
    
    private final ShapeRenderer debugRenderer = new ShapeRenderer();

    private final List<Light> lights = new ArrayList<Light>();
    private FrameBuffer fbo;
    private int width;
    private int height;

    public LightManager(Location location) {
    	ambientIntensity = location.getLight().getIntensity();
    	ambientColor = getColor(
    			location.getLight().getR(),
    			location.getLight().getG(),
    			location.getLight().getB());
    	
        ShaderProgram.pedantic = false;
        defaultShader = new ShaderProgram(vertexShader, defaultPixelShader);
        finalShader = createShader(finalPixelShader, ambientColor, ambientIntensity);
        pauseShader = createShader(finalPixelShader, pauseColor, ambientIntensity * 2);
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
    }
    
    public String getVertexShaderDef() {
        return vertexShader;
    }
    
    public ShaderProgram getDefaultShader() {
        return defaultShader;
    }
    
    public ShaderProgram getFinalShader() {
        return finalShader;
    }
    
    private ShaderProgram createShader(String pixelShader, Vector3 color, float intensity) {
        ShaderProgram shader = new ShaderProgram(vertexShader, pixelShader);
        shader.begin();
        shader.setUniformi("u_lightmap", 1);
        shader.setUniformf("ambientColor", color.x, color.y, color.z, intensity);
        shader.end();
        return shader;
    }
    
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        
        resize(defaultShader);
        resize(finalShader);
        resize(pauseShader);
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
    }
    
    private void resize(ShaderProgram shader) {
        shader.begin();
        shader.setUniformf("resolution", width, height);
        shader.end();
    }
    
    public List<Light> getLights() {
        return lights;
    }

    public void addLight(Light light) {
        lights.add(light);
    }
    
    public void removeLight(Light light) {
    	lights.remove(light);
    }

    public void render(OrthogonalTiledMapRenderer renderer, float delta, boolean paused) {
        // draw lights to frame buffer
        zAngle += delta * zSpeed;
        while (zAngle > PI2) {
            zAngle -= PI2;
        }

        // draw the light to the FBO
        Batch batch = renderer.getSpriteBatch();
        fbo.begin();
        batch.setShader(paused ? pauseShader : defaultShader);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        for (Light light : lights) {
            light.update(zAngle);
            light.render(batch, zAngle);
        }
        batch.end();
        fbo.end();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setShader(paused ? pauseShader : finalShader);
        fbo.getColorBufferTexture().bind(2); // this is important! bind the FBO to the 3rd texture
                                             // unit
        for (Light light : lights) {
            // we force the binding of a texture on first texture unit to avoid artifacts
            // this is because our default and ambient shaders don't use multi-texturing...
            // you can basically bind anything, it doesn't matter
            light.bind(0);
        }
    }
    
    public void debugRender(OrthographicCamera camera) {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeType.Line);
        for (Light light : lights) {
            Vector2 position = light.getPosition();
            debugRenderer.circle(position.x, position.y, light.getRadius());
        }
        debugRenderer.end();
    }
    
    private static Vector3 getColor(int r, int g, int b) {
    	return new Vector3(r / 255.0f, g / 255.0f, b / 255.0f);
    }
}
