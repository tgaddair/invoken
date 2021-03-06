package com.eldritch.invoken.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.type.Player;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.DefaultInputListener;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.eldritch.invoken.util.Utils;

public class InventoryMenu {
	// private static final Color EQUIPPED_COLOR = new Color(0x7693E9FF);

	private final Window window;
	private final Table table;
	private final Skin skin;
	private final Player player;
	private final SplitPane splitPane;

	public InventoryMenu(Player player, Skin skin) {
		this.player = player;
		this.skin = skin;

		table = new Table(skin);
		table.top();

		ScrollPane scroll = new ScrollPane(table, skin);
		splitPane = new SplitPane(getPlayerView(), scroll, false, skin,
				"default-horizontal");

		window = new Window("", skin);
		window.setHeight(Settings.MENU_VIEWPORT_HEIGHT - 100);
		window.setWidth(Settings.MENU_VIEWPORT_WIDTH - 100);
		window.setPosition(Settings.MENU_VIEWPORT_WIDTH / 2 - window.getWidth()
				/ 2, Settings.MENU_VIEWPORT_HEIGHT / 2 - window.getHeight() / 2);
		window.center();

		window.add(splitPane).expand().fill();
		window.setVisible(false);
	}

	public Window getTable() {
		return window;
	}

	public void toggle() {
		show(!window.isVisible());
	}

	public void show(boolean visible) {
		window.setVisible(visible);
		if (visible) {
			refresh();
		}
	}

	public void refresh() {
		table.clear();
		for (ItemState item : player.getInfo().getInventory().getItems()) {
			addItemButton(item);
		}
		refreshPortrait();
	}

	private void refreshPortrait() {
		Stack stack = new Stack();
		stack.add(getPlayerView());

		AgentInventory inv = player.getInventory();
		String weightValue = Utils.markupIfInvalid(
				String.format("%.1f", inv.getWeight()), inv.isBurdened());
		double maxWeight = inv.getMaxWeight();
		String text = String
				.format("Weight: %s / %.1f", weightValue, maxWeight);

		LabelStyle labelStyle = skin.get("response", LabelStyle.class);
		Label label = new Label(text, labelStyle);
		label.setAlignment(Align.topLeft);
		stack.add(label);

		splitPane.setFirstWidget(stack);
	}

	private ScrollPane getPlayerView() {
		return new ScrollPane(new Image(player.getPortrait()));
	}

	private String getStyle(Item item) {
		AgentInventory inventory = player.getInfo().getInventory();
		if (item.isEquipped(inventory)) {
			return "selected";
		} else if (!player.isIdentified(item)) {
			return "encrypted";
		} else if (!player.canEquip(item)) {
			return "invalid";
		} else {
			return "choice";
		}
	}

	private void refreshButton(TextButton button, Item item) {
		button.setStyle(skin.get(getStyle(item), TextButtonStyle.class));
	}

	private void addItemButton(ItemState itemState) {
		final Item item = itemState.getItem();
		TextButtonStyle buttonStyle = skin.get("choice", TextButtonStyle.class);
		final TextButton itemButton = new TextButton(getText(item,
				itemState.getCount()), buttonStyle);
		refreshButton(itemButton, item);
		itemButton.addListener(new DefaultInputListener() {
			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
			    AgentInventory inventory = player.getInfo().getInventory();
			    if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
			        // drop item
			        inventory.removeItem(item, 1);
			    } else {
			        if (item.isEquipped(inventory)) {
	                    inventory.unequip(item);
	                    InvokenGame.SOUND_MANAGER
	                            .play(SoundEffect.INVENTORY_OFF, 2);
	                } else {
	                    inventory.equip(item);
	                    InvokenGame.SOUND_MANAGER.play(SoundEffect.INVENTORY_ON, 2);
	                }
			    }
				
				refresh();
				System.out.println(item.toString());
			}
		});
		itemButton.addListener(Utils.createTooltip(item.getTooltipFor(player),
				skin));

		table.row();
		table.add(itemButton).expandX().fillX().padLeft(5).padRight(5)
				.padBottom(5).padTop(5);
	}

	private String getText(Item item, int count) {
		return String.format("%s (%d)", item.getName(player), count);
	}
}
