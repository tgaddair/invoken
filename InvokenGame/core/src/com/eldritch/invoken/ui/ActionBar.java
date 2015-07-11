package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.PreparedAugmentations.AugmentationListener;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;

public class ActionBar implements AugmentationListener {
    private final Map<Augmentation, Label> labels = new HashMap<>();
    private final Map<Augmentation, Image> images = new HashMap<>();
    private final PreparedAugmentations augmentations;
    private final Table container;
    private final Player player;
    private final Skin skin;
    
    public ActionBar(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;
        augmentations = player.getInfo().getAugmentations();
        augmentations.addListener(this);
        
        container = new Table();
        resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
        container.bottom();
        refresh();
    }
    
    public void update() {
        for (Augmentation aug : augmentations.getAugmentations()) {
            if (images.containsKey(aug)) {
                float a = aug.isValid(player) && aug.hasEnergy(player) ? 1 : 0.25f;
                if (augmentations.isActive(aug)) {
                    images.get(aug).setColor(1, 0, 0, a);
                } else {
                    images.get(aug).setColor(1, 1, 1, a);
                }
            }
            if (labels.containsKey(aug)) {
                labels.get(aug).setText(aug.getLabel(player));
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

    @Override
    public void onClear() {
        container.clear();
        labels.clear();
        images.clear();
        refresh();
    }
    
    @Override
    public void onAdd(Augmentation aug) {
        add(aug);
    }
    
    private void refresh() {
        for (Augmentation aug : augmentations.getAugmentations()) {
            add(aug);
        }
    }
    
    private void add(final Augmentation aug) {
        LabelStyle labelStyle = skin.get("toast", LabelStyle.class);
        Label label = new Label("", labelStyle);
        label.setAlignment(Align.bottomRight);
        label.setColor(Color.GREEN);
        labels.put(aug, label);
        
//        Label keyLabel = new Label("Z", labelStyle);
//        keyLabel.setAlignment(Align.bottomLeft, Align.left);
//        keyLabel.setColor(Color.CYAN);
        
        Image image = new Image(aug.getIcon());
        images.put(aug, image);
        
        Stack stack = new Stack();
        stack.addActor(image);
//        stack.addActor(keyLabel);
        stack.addActor(label);
        stack.addListener(new DefaultInputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                augmentations.toggleActiveAugmentation(aug, button);
            }
        });
        
        TooltipManager manager = new TooltipManager();
        manager.setMaxWidth(500);
        Tooltip tooltip = new Tooltip("This is a test of the tooltip system.  Look upon my works, ye mighty, and despair.", manager, skin);
        tooltip.setInstant(true);
        tooltip.setAlways(true);
        stack.addListener(tooltip);
        
        container.add(stack).padLeft(10).padRight(10).padBottom(10);
    }
}
