package com.eldritch.invoken.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;
import com.eldritch.invoken.screens.GameScreen;

public class OverlayLightMasker {
    final String shaderDef = Gdx.files.internal("shader/thresholdShader.glsl").readString();
    final ShaderProgram shader;
    FrameBuffer fbo;

    public OverlayLightMasker(String vertexShader) {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexShader, shaderDef);
    }

    public void resize(int width, int height) {
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
    }

    public void render(OrthogonalShadedTiledMapRenderer renderer) {
        fbo.begin();
        renderer.getSpriteBatch().setShader(shader);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
        save();
        fbo.end();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // this is important! bind the FBO to the 4th texture unit
        fbo.getColorBufferTexture().bind(3);
    }
    
    private void save() {
        if (!GameScreen.SCREEN_GRAB) {
            return;
        }
        
        BufferedImage image = new BufferedImage(fbo.getWidth(), fbo.getHeight(), BufferedImage.TYPE_INT_RGB);
        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, fbo.getWidth(), fbo.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = pixmap.getPixel(x, image.getHeight() - y - 1) >> 0x000000FF;
                image.setRGB(x, y, value);
            }
        }
        
        File outputfile = new File(System.getProperty("user.home") + "/frame-buffer.png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
             e.printStackTrace();
        }
    }
}
