package com.eldritch.invoken.location.proc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.ImageResolver.DirectImageResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TmxCachedMapLoader extends TmxMapLoader implements Disposable {
    private final Set<String> knownSources = new HashSet<>();
    private final ObjectMap<String, Texture> textures = new ObjectMap<>();
    private final List<Disposable> ownedResources = new ArrayList<>();
    
    @Override
    public TiledMap load (String fileName, TmxMapLoader.Parameters parameters) {
        try {
            this.convertObjectToTileSpace = parameters.convertObjectToTileSpace;
            this.flipY = parameters.flipY;
            FileHandle tmxFile = resolve(fileName);
            root = xml.parse(tmxFile);
            
            Array<FileHandle> textureFiles = loadTilesets(root, tmxFile);
            textureFiles.addAll(loadImages(root, tmxFile));
            
            for (FileHandle textureFile : textureFiles) {
                Texture texture = new Texture(textureFile, parameters.generateMipMaps);
                texture.setFilter(parameters.textureMinFilter, parameters.textureMagFilter);
                textures.put(textureFile.path(), texture);
                ownedResources.add(texture);
            }

            DirectImageResolver imageResolver = new DirectImageResolver(textures);
            TiledMap map = loadTilemap(root, tmxFile, imageResolver);
            return map;
        } catch (IOException e) {
            throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }
    
    @Override
    protected Array<FileHandle> loadTilesets (Element root, FileHandle tmxFile) throws IOException {
        Array<FileHandle> images = new Array<FileHandle>();
        for (Element tileset : root.getChildrenByName("tileset")) {
            String source = tileset.getAttribute("source", null);
            if (source != null) {
                // don't reload the same image resource twice
                if (knownSources.contains(source)) {
                    continue;
                }
                knownSources.add(source);
                
                FileHandle tsxFile = getRelativeFileHandle(tmxFile, source);
                tileset = xml.parse(tsxFile);
                Element imageElement = tileset.getChildByName("image");
                if (imageElement != null) {
                    String imageSource = tileset.getChildByName("image").getAttribute("source");
                    FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
                    images.add(image);
                } else {
                    for (Element tile : tileset.getChildrenByName("tile")) {
                        String imageSource = tile.getChildByName("image").getAttribute("source");
                        FileHandle image = getRelativeFileHandle(tsxFile, imageSource);
                        images.add(image);
                    }
                }
            } else {
                Element imageElement = tileset.getChildByName("image");
                if (imageElement != null) {
                    String imageSource = tileset.getChildByName("image").getAttribute("source");
                    
                    // don't reload the same image resource twice
                    if (knownSources.contains(imageSource)) {
                        continue;
                    }
                    knownSources.add(imageSource);
                    
                    FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
                    images.add(image);
                } else {
                    for (Element tile : tileset.getChildrenByName("tile")) {
                        String imageSource = tile.getChildByName("image").getAttribute("source");
                        
                        // don't reload the same image resource twice
                        if (knownSources.contains(imageSource)) {
                            continue;
                        }
                        knownSources.add(imageSource);
                        
                        FileHandle image = getRelativeFileHandle(tmxFile, imageSource);
                        images.add(image);
                    }
                }
            }
        }
        return images;
    }
    
    @Override
    protected Array<FileHandle> loadImages (Element root, FileHandle tmxFile) throws IOException {
        Array<FileHandle> images = new Array<FileHandle>();
        
        for (Element imageLayer : root.getChildrenByName("imagelayer")) {
            Element image = imageLayer.getChildByName("image");
            String source = image.getAttribute("source", null);
            
            // don't reload the same image resource twice
            if (knownSources.contains(source)) {
                continue;
            }
            knownSources.add(source);
            
            if (source != null) {
                FileHandle handle = getRelativeFileHandle(tmxFile, source);
                
                if (!images.contains(handle, false)) {
                    images.add(handle);
                }
            }
        }
        
        return images;
    }
    
    @Override
    public void dispose() {
        for (Disposable resource : ownedResources) {
            resource.dispose();
        }
    }
}
