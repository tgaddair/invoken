package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.screens.AbstractScreen;
import com.eldritch.invoken.util.DefaultInputListener;

public class ActionBar {
    private final Map<Augmentation, Image> images = new HashMap<Augmentation, Image>();
    private final PreparedAugmentations augmentations;
    private final Table container;
    private final Set<Augmentation> lastActive = new HashSet<Augmentation>();
    
    public ActionBar(Player player) {
        augmentations = player.getInfo().getAugmentations();
        
        container = new Table();
        resize(AbstractScreen.MENU_VIEWPORT_WIDTH, AbstractScreen.MENU_VIEWPORT_HEIGHT);
        container.bottom();
        
        for (final Augmentation aug : augmentations.getAugmentations()) {
            Image image = new Image(aug.getIcon());
            image.addListener(new DefaultInputListener() {
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    augmentations.toggleActiveAugmentation(aug);
                }
            });
            images.put(aug, image);
            container.add(image).padLeft(10).padRight(10).padBottom(10);
        }
    }
    
    public void update() {
        for (Augmentation aug : augmentations.getAugmentations()) {
            if (augmentations.isActive(aug)) {
                // check to see if this aug is already active to avoid costly color set operation
                if (!lastActive.contains(aug)) {
                    // state changed since last update, so change the color
                    images.get(aug).setColor(1, 0, 0, 1);
                    lastActive.add(aug);
                }
            } else {
                if (lastActive.contains(aug)) {
                    images.get(aug).setColor(1, 1, 1, 1);
                    lastActive.remove(aug);
                }
            }
        }
    }
    
    public void resize(int width, int height) {
        container.setHeight(height / 2);
        container.setWidth(width);
    }
    
    public Table getTable() {
        return container;
    }
}
