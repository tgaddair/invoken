package com.eldritch.invoken.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.Utils;
import com.eldritch.invoken.util.SoundManager.SoundEffect;

public class ResearchMenu implements HudElement {
    private final Map<TextButton, Item> buttons = new HashMap<>();
	private final Table container;
	private final Table table;
	private final Skin skin;
	private final Player player;
	private boolean active = false;
	
	public ResearchMenu(Player player, Skin skin) {
	    this.player = player;
		this.skin = skin;
	    container = new Table(skin);
	    resize(Settings.MENU_VIEWPORT_WIDTH, Settings.MENU_VIEWPORT_HEIGHT);
		container.center();

	    table = new Table(skin);
		table.top();
		
		ScrollPane scroll = new ScrollPane(table, skin);
		container.add(scroll).expand().fill();
		container.setVisible(false);
	}
	

    @Override
    public Table getContainer() {
        return container;
    }

    @Override
    public void update(float delta, Level level) {
        if (player.isResearching()) {
            if (!active) {
                container.setVisible(true);
                active = true;
                setup(player);
            }
        } else {
            exitMenu();
        }
    }
	
	public void resize(int width, int height) {
	    container.setHeight(height - 100);
        container.setWidth(width / 3);
        container.setPosition(
                width / 2 - container.getWidth() / 2,
                height / 2 - container.getHeight() / 2);
	}
	
	public void setup(Player player) {
	    buttons.clear();
		table.clear();
		for (ItemState item : player.getInventory().getItems()) {
		    if (!player.isIdentified(item.getItem())) {
		        addItemButton(item, player);
		    }
		}
	}
	
	private void addItemButton(ItemState itemState, final Player player) {
		final Item item = itemState.getItem();
		
		String styleName = canAfford(player, item) ? "encrypted" : "invalid";
        TextButtonStyle buttonStyle = skin.get(styleName, TextButtonStyle.class);
        
        final Tooltip tooltip = Utils.createTooltip(item.getTooltipFor(player), skin);
        
		final TextButton itemButton = new TextButton(getText(item), buttonStyle);
		itemButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			    if (canAfford(player, item)) {
			        // identify
			        player.identify(item.getId());
			        player.getInventory().removeItem(Fragment.getInstance(), item.getValue());
			    }
			    
			    // update button
                if (player.isIdentified(item.getId())) {
                    itemButton.setText(item.getName(player));
                    itemButton.setStyle(skin.get("choice", TextButtonStyle.class));
                    tooltip.setText(item.getTooltipFor(player));
                }
                
                // update cost
                for (Entry<TextButton, Item> entry : buttons.entrySet()) {
                    Item otherItem = entry.getValue();
                    if (entry.getKey() != itemButton) {
                        String styleName;
                        if (player.isIdentified(otherItem.getId())) {
                            styleName = "choice";
                        } else if (canAfford(player, otherItem)) {
                            styleName = "encrypted";
                        } else {
                            styleName = "invalid";
                        }
                        
                        TextButtonStyle buttonStyle = skin.get(styleName, TextButtonStyle.class);
                        entry.getKey().setStyle(buttonStyle);
                    }
                }
			    
				InvokenGame.SOUND_MANAGER.play(SoundEffect.INVENTORY_ON, 2);
			}
		});
		itemButton.addListener(tooltip);
		
		table.row();
		table.add(itemButton).expandX().fillX().padLeft(5).padRight(5).padBottom(5).padTop(5);
		
		buttons.put(itemButton, item);
	}
	
	private boolean canAfford(Player player, Item item) {
	    int cost = item.getValue();
        int fragments = player.getInventory().getItemCount(Fragment.getInstance());
        return fragments >= cost;
	}
	
	private String getText(Item item) {
		return String.format("%s (%d)", item.getName(player), item.getValue());
	}
	
	private void exitMenu() {
		container.setVisible(false);
		active = false;
	}
}
