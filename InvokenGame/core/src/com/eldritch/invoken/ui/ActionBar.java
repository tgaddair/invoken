package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.Map;

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
    private Augmentation lastActive = null;
    
    public ActionBar(Player player) {
        augmentations = player.getInfo().getAugmentations();
        
        container = new Table();
        container.setHeight(AbstractScreen.MENU_VIEWPORT_HEIGHT / 2);
        container.setWidth(AbstractScreen.MENU_VIEWPORT_WIDTH);
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
        if (lastActive != augmentations.getActiveAugmentation()) {
            if (lastActive != null) {
                images.get(lastActive).setColor(1, 1, 1, 1);
            }
            lastActive = augmentations.getActiveAugmentation();
            if (lastActive != null) {
                images.get(lastActive).setColor(1, 0, 0, 1);
            }
        }
    }
    
    public Table getTable() {
        return container;
    }
}
