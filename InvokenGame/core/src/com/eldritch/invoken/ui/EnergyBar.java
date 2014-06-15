package com.eldritch.invoken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.type.Player;

public class EnergyBar extends ProgressBar {
    private final Player player;
    
    public EnergyBar(Player player, Skin skin) {
        super(0, player.getInfo().getBaseEnergy(), 1, true, skin);
        this.player = player;
    }

    public void update() {
        if (player.getInfo().getEnergy() != getValue()) {
            setValue(player.getInfo().getEnergy());
        }
    }
}
