package com.eldritch.invoken.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import shaders.Gaussian;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.ScreenUtils;
import com.eldritch.invoken.encounter.NaturalVector2;
import com.eldritch.invoken.screens.GameScreen;

public class FogOfWarMasker {
    // should be at least as large as our display
    public static final int FBO_SIZE = 1024;

    // our texture to blur
    ShapeRenderer sr = new ShapeRenderer();

    // we'll use a single batch for everything
    SpriteBatch batch;

    // our blur shader
    ShaderProgram blurShader;

    // our offscreen buffers
    private final Mesh lightMapMesh = createLightMapMesh();
    FrameBuffer frameBuffer, pingPongBuffer;
    private boolean[][] mask;

    float radius = 3f;
    final static float MAX_BLUR = 3f;

    public FogOfWarMasker() {
        ShaderProgram.pedantic = false;

        // our basic pass-through vertex shader
        final String VERT = Gdx.files.internal("shader/vertexShader.glsl").readString();

        // our fragment shader, which does the blur in one direction at a time
        final String FRAG = Gdx.files.internal("shader/blur.frag").readString();

        // create our shader program
        blurShader = new ShaderProgram(VERT, FRAG);

        // Good idea to log any warnings if they exist
        if (blurShader.getLog().length() != 0)
            System.out.println(blurShader.getLog());

        // always a good idea to set up default uniforms...
        blurShader.begin();
        blurShader.setUniformf("dir", 0f, 0f); // direction of blur; nil for now
        blurShader.setUniformf("resolution", FBO_SIZE); // size of FBO texture
        blurShader.setUniformf("radius", radius); // radius of blur
        blurShader.end();

        batch = new SpriteBatch();
        // batch.enableBlending();
        // batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void resize(int width, int height) {
        frameBuffer = new FrameBuffer(Format.RGBA8888, width, height, false);
        pingPongBuffer = new FrameBuffer(Format.RGBA8888, width, height, false);
        mask = new boolean[width][height];
//        blurShader = Gaussian.createBlurShader(width, height);
    }

    public void updateMask(Set<NaturalVector2> tiles) {
        // reset
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                mask[i][j] = false;
            }
        }

        // activate all filled tiles
        for (NaturalVector2 tile : tiles) {
            mask[tile.x][tile.y] = true;
        }
    }

    public void render(OrthographicCamera camera) {
        // clear FBO A with an opaque colour to minimize blending issues
        frameBuffer.begin();
        // Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the mask
        // TODO: iterate over view bounds
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Filled);
        // sr.setColor(1, 1, 1, 0);
        for (int x = 0; x < mask.length; x++) {
            for (int y = 0; y < mask[x].length; y++) {
                if (mask[x][y]) {
                    sr.rect(x, y, 1, 1);
                }
            }
        }

        // flush the batch, i.e. render entities to GPU
        sr.flush();
        sr.end();

        // After flushing, we can finish rendering to FBO target A
        frameBuffer.end();

//        Gdx.gl20.glDisable(GL20.GL_BLEND);
        for (int i = 0; i < 3; i++) {
            // render FBO A to FBO B, using horizontal blur
             horizontalBlur();

            // render FBO B to scene, using vertical blur
             verticalBlur();

//            frameBuffer.getColorBufferTexture().bind(0);
//            // horizontal
//            pingPongBuffer.begin();
//            {
//                blurShader.begin();
//                // blurShader.setUniformi("u_texture", 0);
//                blurShader.setUniformf("dir", 1f, 0f);
//                lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
//                blurShader.end();
//            }
//            pingPongBuffer.end();
//
//            pingPongBuffer.getColorBufferTexture().bind(0);
//            // vertical
//            frameBuffer.begin();
//            {
//                blurShader.begin();
//                // blurShader.setUniformi("u_texture", 0);
//                blurShader.setUniformf("dir", 0f, 1f);
//                lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
//                blurShader.end();
//
//            }
//            frameBuffer.end();
        }
//        Gdx.gl20.glEnable(GL20.GL_BLEND);

        // this is important! bind the FBO to the 4th texture unit
        frameBuffer.getColorBufferTexture().bind(3);
    }
    
    void horizontalBlur() {
        // swap the shaders
        // this will send the batch's (FBO-sized) projection matrix to our blur shader
        batch.setShader(blurShader);

        // ensure the direction is along the X-axis only
        blurShader.begin();
        blurShader.setUniformf("dir", 1f, 0f);
        blurShader.end();

        // start rendering to target B
        pingPongBuffer.begin();

        batch.begin();

        // no need to clear since targetA has an opaque background
        // render target A (the scene) using our horizontal blur shader
        // it will be placed into target B
        batch.draw(frameBuffer.getColorBufferTexture(), 0, 0);

        // flush the batch before ending target B
        batch.flush();
        batch.end();

        // finish rendering target B
        save(pingPongBuffer, "blurTargetB");
        pingPongBuffer.end();
    }

    void verticalBlur() {
        // now we can render to the screen using the vertical blur shader
        frameBuffer.begin();

        // apply the blur only along Y-axis
        blurShader.begin();
        blurShader.setUniformf("dir", 0f, 1f);
        blurShader.end();

        // draw the horizontally-blurred FBO B to the screen, applying the vertical blur as we go
        batch.begin();
        batch.draw(pingPongBuffer.getColorBufferTexture(), 0, 0);
        batch.flush();
        batch.end();

        save(frameBuffer, "blurTargetA");
        frameBuffer.end();
    }

    private void save(FrameBuffer fbo, String filename) {
        if (!GameScreen.SCREEN_GRAB) {
            return;
        }

        BufferedImage image = new BufferedImage(fbo.getWidth(), fbo.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, fbo.getWidth(), fbo.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = pixmap.getPixel(x, image.getHeight() - y - 1) >> 0x000000FF;
                image.setRGB(x, y, value);
            }
        }

        File outputfile = new File(System.getProperty("user.home") + "/" + filename + ".png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Mesh createLightMapMesh() {
        float[] verts = new float[VERT_SIZE];
        // vertex coord
        verts[X1] = -1;
        verts[Y1] = -1;

        verts[X2] = 1;
        verts[Y2] = -1;

        verts[X3] = 1;
        verts[Y3] = 1;

        verts[X4] = -1;
        verts[Y4] = 1;

        // tex coords
        verts[U1] = 0f;
        verts[V1] = 0f;

        verts[U2] = 1f;
        verts[V2] = 0f;

        verts[U3] = 1f;
        verts[V3] = 1f;

        verts[U4] = 0f;
        verts[V4] = 1f;

        Mesh tmpMesh = new Mesh(true, 4, 0, new VertexAttribute(
                Usage.Position, 2, "a_position"), new VertexAttribute(
                Usage.TextureCoordinates, 2, "a_texCoord"));

        tmpMesh.setVertices(verts);
        return tmpMesh;
    }
    
    static public final int VERT_SIZE = 16;
    static public final int X1 = 0;
    static public final int Y1 = 1;
    static public final int U1 = 2;
    static public final int V1 = 3;
    static public final int X2 = 4;
    static public final int Y2 = 5;
    static public final int U2 = 6;
    static public final int V2 = 7;
    static public final int X3 = 8;
    static public final int Y3 = 9;
    static public final int U3 = 10;
    static public final int V3 = 11;
    static public final int X4 = 12;
    static public final int Y4 = 13;
    static public final int U4 = 14;
    static public final int V4 = 15;
}
