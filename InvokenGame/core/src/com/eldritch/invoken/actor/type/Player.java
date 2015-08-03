package com.eldritch.invoken.actor.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.AgentInventory;
import com.eldritch.invoken.actor.ConversationHandler;
import com.eldritch.invoken.actor.ConversationHandler.DefaultConversationHandler;
import com.eldritch.invoken.actor.PreparedAugmentations;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.items.Ammunition;
import com.eldritch.invoken.actor.items.Consumable;
import com.eldritch.invoken.actor.items.Core;
import com.eldritch.invoken.actor.items.Fragment;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.util.Backup;
import com.eldritch.invoken.actor.util.SelectionHandler;
import com.eldritch.invoken.actor.util.ThreatMonitor;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.proc.LocationGenerator;
import com.eldritch.invoken.proto.Actors.InventoryItem;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Actors.PlayerActor.KillRecord;
import com.eldritch.invoken.state.Inventory.ItemState;
import com.eldritch.invoken.util.AnimationUtils;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.SoundManager.SoundEffect;
import com.google.common.base.Optional;

/** The player character, has state and state time, */
public class Player extends SteeringAgent {
    private final Vector2 targetCoord = new Vector2();
    private final ThreatMonitor<Player> threat;
    private final String bodyType;
    private final Optional<PlayerActor> priorState;

    private final Set<String> identifiedItems = new LinkedHashSet<>();

    private boolean holding = false;
    private boolean moving = false;
    private boolean fixedTarget = false;
    private boolean lightOn = false;
    private Augmentation lastAug = null;
    private Optional<Backup> backup = Optional.absent();

    private int lastFragments = 0;

    public Player(NewPlayerDescription info, int level, float x, float y, Level location,
            String body) {
        super(x, y, Human.getWidth(), Human.getHeight(), Human.MAX_VELOCITY, info, level, location,
                AnimationUtils.getHumanAnimations(body));
        this.threat = new ThreatMonitor<>(this);
        this.bodyType = body;
        this.priorState = Optional.<PlayerActor> absent();
        createBackup();
    }

    public Player(PlayerActor data, float x, float y, Level level) {
        super(data.getParams(), true, x, y, Human.getWidth(), Human.getHeight(),
                Human.MAX_VELOCITY, level, AnimationUtils.getHumanAnimations(data.getParams()
                        .getBodyType()));

        // identify items
        for (String itemId : data.getIdentifiedItemList()) {
            identifiedItems.add(itemId);
        }

        // equip items
        Set<String> equipped = new HashSet<String>(data.getEquippedItemIdList());
        for (ItemState item : info.getInventory().getItems()) {
            if (equipped.contains(item.getItem().getId())) {
                info.getInventory().equip(item.getItem());
            }
        }

        // map consumable bar
        AgentInventory inv = info.getInventory();
        for (int i = 0; i < data.getConsumableIdCount(); i++) {
            String id = data.getConsumableId(i);
            if (inv.hasItem(id)) {
                Item item = inv.getItem(id);
                item.mapTo(inv, i);
            }
        }

        if (data.hasFragments()) {
            getInfo().getInventory().addItem(Fragment.getInstance(), data.getFragments());
        }

        this.threat = new ThreatMonitor<Player>(this);
        this.bodyType = data.getParams().getBodyType();

        // restore the prior state
        this.priorState = Optional.of(data);
        for (String dialogue : data.getUniqueDialogueList()) {
            addDialogue(dialogue);
        }

        // record all kills
        for (KillRecord kill : data.getKillList()) {
            setKillCount(kill.getAgentId(), kill.getCount());
        }

        // backup
        if (data.hasBackup()) {
            backup = Optional.of(new Backup(data.getBackup()));
        }
    }

    public boolean hasBackup() {
        return backup.isPresent();
    }

    public Backup claimBackup() {
        Backup claimed = backup.get();
        backup = Optional.absent();
        return claimed;
    }

    public void createBackup() {
        Level level = getLocation();
        this.backup = Optional.of(new Backup(level.getFloor(), level.getRegion(), getPosition()));
    }

    @Override
    public boolean isIdentified(String itemId) {
        return identifiedItems.contains(itemId);
    }

    @Override
    public void identify(String itemId) {
        identifiedItems.add(itemId);
    }

    @Override
    public boolean isIdentified(Item item) {
        if (item.isEncrypted()) {
            return isIdentified(item.getId());
        }
        return true;
    }

    @Override
    public boolean canEquip(Item item) {
        return isIdentified(item) && super.canEquip(item);
    }

    public int getLastFragments() {
        return lastFragments;
    }

    @Override
    protected void onDeath() {
        int total = info.getInventory().getItemCount((Fragment.getInstance()));
        lastFragments = total;

        super.onDeath();
    }

    @Override
    protected void releaseItems() {
        // only release fragments and core, hold on to other resources
        Level level = getLocation();
        Vector2 position = getPosition();
        Fragment.getInstance().releaseFrom(info.getInventory(), level, position);
        if (info.getInventory().hasItem(Core.getInstance())) {
            Core.getInstance().releaseFrom(info.getInventory(), level, position);
        }
    }

    public void toggleLastAugmentation() {
        PreparedAugmentations prepared = getInfo().getAugmentations();
        if (prepared.hasActiveAugmentation(0)) {
            lastAug = prepared.getActiveAugmentation(0);
            prepared.toggleActiveAugmentation(lastAug, 0);
        } else if (lastAug != null) {
            prepared.toggleActiveAugmentation(lastAug, 0);
        }
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffect.HUMAN_DEATH;
    }

    @Override
    protected void takeAction(float delta, Level screen) {
        if (moving) {
            // moving = mover.takeAction(delta, targetCoord, screen);
            if (!moving) {
                fixedTarget = false;
            }
        }

        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            body.applyForceToCenter(new Vector2(-1 * getMaxLinearSpeed(), 0), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            body.applyForceToCenter(new Vector2(1 * getMaxLinearSpeed(), 0), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            body.applyForceToCenter(new Vector2(0, 1 * getMaxLinearSpeed()), true);
            moving = false;
        }

        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            body.applyForceToCenter(new Vector2(0, -1 * getMaxLinearSpeed()), true);
            moving = false;
        }
    }

    public void toggleLight() {
        lightOn = !lightOn;
    }

    public boolean hasLightOn() {
        return lightOn;
    }

    public boolean holdingPosition() {
        return holding;
    }

    public void holdPosition(boolean hold) {
        this.holding = hold;
        if (hold) {
            setMoving(false);
        }
    }

    public void moveToFixedTarget(float x, float y) {
        moveTo(x, y);
        setMoving(true);
        fixedTarget = true;
    }

    public void moveTo(float x, float y) {
        targetCoord.x = x;
        targetCoord.y = y;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean hasFixedTarget() {
        return fixedTarget;
    }

    @Override
    public void setLocation(Level level, float x, float y) {
        super.setLocation(level, x, y);
    }

    @Override
    public boolean canKeepTarget(Agent other) {
        return true;
    }

    public boolean select(Agent other, Level level) {
        if (hasSelectionHandler()) {
            SelectionHandler handler = getSelectionHandler();
            if (handler.canSelect(other) && handler.select(other)) {
                return true;
            }
        }

        endJointInteraction();
        setTarget(other);
        return true;
    }

    public void reselect(Agent other) {
        if (canInteract(other)) {
            beginInteraction(other, false);
        }
    }

    @Override
    protected void handleConfusion(boolean confused) {
        // do nothing, for now, will change to make attack at random
    }

    @Override
    protected float damage(float value) {
        if (Settings.GOD_MODE) {
            return 0;
        }
        return super.damage(value);
    }

    @Override
    public void alertTo(Agent other) {
        // does nothing
    }

    @Override
    public void suspicionTo(Agent other) {
        // does nothing
    }

    @Override
    public ThreatMonitor<?> getThreat() {
        return threat;
    }

    @Override
    public boolean canTargetProjectile(Agent other) {
        // let the player make seemingly bad shots
        return true;
    }

    @Override
    public float getCloakAlpha() {
        // players should be able to see themselves on screen even when invisible
        float greatestAlpha = 0.1f;
        for (Agent neighbor : getNeighbors()) {
            if (!neighbor.hasLineOfSight(this) || !neighbor.inFieldOfView(this)) {
                continue;
            }

            // calculate the distance from the view threshold
            float visibility = neighbor.getVisibility();
            float visibility2 = visibility * visibility;

            float delta = dst2(neighbor) - visibility2;
            if (delta < 0) {
                // we are within the visibility bounds, so full visibility
                greatestAlpha = 1f;
                break;
            } else {
                // linearly interpolate between our delta and a max delta
                float alpha = (visibility2 - delta) / visibility2;
                if (alpha > greatestAlpha) {
                    greatestAlpha = alpha;
                }
            }
        }

        return greatestAlpha;
    }

    // private boolean isTouched(float startX, float endX) {
    // // check if any finger is touch the area between startX and endX
    // // startX/endX are given between 0 (left edge of the screen) and 1
    // // (right edge of the screen)
    // for (int i = 0; i < 2; i++) {
    // float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
    // if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
    // return true;
    // }
    // }
    // return false;
    // }

    @Override
    public float getAttackSpeed() {
        return 2;
    }

    @Override
    public void recoil() {
        Vector2 shift = getPosition().cpy().sub(getLocation().getFocusPoint()).nor().scl(0.25f);
        getLocation().shiftView(shift);
    }

    @Override
    public boolean canSpeak() {
        return false;
    }

    @Override
    public ConversationHandler getDialogueHandler() {
        // not implemented
        return new DefaultConversationHandler();
    }

    public PlayerActor serialize() {
        PlayerActor.Builder builder = PlayerActor.newBuilder();
        builder.setParams(info.serialize());
        builder.getParamsBuilder().setBodyType(bodyType);

        // position
        builder.setX(getPosition().x);
        builder.setY(getPosition().y);

        // location
        Level level = getLocation();
        builder.setSeed(level.getSeed());
        builder.setRegion(level.getRegion());
        builder.setFloor(level.getFloor());

        // equipped items
        AgentInventory inventory = info.getInventory();
        if (inventory.hasOutfit()) {
            builder.addEquippedItemId(inventory.getOutfit().getId());
        }
        if (inventory.hasMeleeWeapon()) {
            builder.addEquippedItemId(inventory.getMeleeWeapon().getId());
        }
        if (inventory.hasRangedWeapon()) {
            builder.addEquippedItemId(inventory.getRangedWeapon().getId());
        }
        for (Ammunition ammo : inventory.getAmmunition()) {
            builder.addEquippedItemId(ammo.getId());
        }

        // consumables
        for (Consumable consumable : inventory.getConsumables()) {
            if (consumable != null) {
                builder.addConsumableId(consumable.getId());
            }
        }

        // carry over previous state
        if (priorState.isPresent()) {
            PlayerActor prior = priorState.get();
            builder.addAllVisitedRooms(prior.getVisitedRoomsList());
        }

        // identified items
        builder.addAllIdentifiedItem(identifiedItems);

        // state markers
        builder.addAllUniqueDialogue(getUniqueDialogue());
        for (Entry<String, Integer> kill : getKills().entrySet()) {
            builder.addKill(KillRecord.newBuilder().setAgentId(kill.getKey())
                    .setCount(kill.getValue()).build());
        }

        // backup
        if (backup.isPresent()) {
            builder.setBackup(backup.get().toProto());
        }

        return builder.build();
    }

    /**
     * For creating new players.
     */
    public static class NewPlayerDescription implements PlayerDescription {
        private final String id;
        private final String name;
        private final Profession profession;

        private NewPlayerDescription(String id, String name, Profession profession) {
            this.id = id;
            this.name = name;
            this.profession = profession;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Profession getProfession() {
            return profession;
        }

        @Override
        public long getSeed() {
            Random rand = new Random();
            return rand.nextLong();
        }

        @Override
        public Level load(LocationGenerator generator) {
            Level level = generator.generate();

            // create a new player
            Player player = level.createPlayer(this);

            // save in case they die before reaching the first save point
            save(player, Optional.<PlayerActor> absent());
            return level;
        }

        @Override
        public Optional<PlayerActor> getState() {
            return Optional.<PlayerActor> absent();
        }

        public static NewPlayerDescription from(String name, Profession profession) {
            long time = System.currentTimeMillis();
            String id = name + "_" + time;
            if (Settings.DEBUG_SAVE) {
                id = name;
            }

            return new NewPlayerDescription(id, name, profession);
        }

        public static NewPlayerDescription getDefault() {
            return from("Travid", Profession.getDefault());
        }
    }

    /**
     * For loading existing players.
     */
    public static class SavedPlayerDescription implements PlayerDescription {
        private final PlayerActor state;

        private SavedPlayerDescription(PlayerActor state) {
            this.state = state;
        }

        @Override
        public long getSeed() {
            return state.getSeed();
        }

        @Override
        public Level load(LocationGenerator generator) {
            // load the location
            Level level = generator.generate(state);

            // load from disk
            level.createPlayer(state);

            // create a corpse, if present
            if (state.hasCorpse()) {
                level.createPlayerCorpse(state.getCorpse());
            }

            return level;
        }

        @Override
        public Optional<PlayerActor> getState() {
            return Optional.of(state);
        }

        public static SavedPlayerDescription from(String id) {
            return new SavedPlayerDescription(Player.load(id));
        }
    }

    public interface PlayerDescription {
        long getSeed();

        Level load(LocationGenerator generator);

        Optional<PlayerActor> getState();
    }

    public static PlayerActor load(String id) {
        FileHandle handle = Gdx.files.local("saves/" + id + ".dat");
        return load(handle);
    }

    public static void save(Player player, Optional<PlayerActor> previous) {
        // setup the save state
        PlayerActor data = player.serialize();
        if (previous.isPresent()) {
            PlayerActor.Builder builder = PlayerActor.newBuilder(data);

            PlayerActor.Builder corpseBuilder = PlayerActor.newBuilder(data);
            corpseBuilder.clearEquippedItemId();
            corpseBuilder.getParamsBuilder().clearInventoryItem();
            corpseBuilder.getParamsBuilder().setBodyType("sprite/characters/hollow.png");
            corpseBuilder.setFragments(player.getLastFragments());

            InventoryItem coreItem = InventoryItem.newBuilder()
                    .setItemId(Core.getInstance().getId()).setCount(1).build();
            corpseBuilder.getParamsBuilder().addInventoryItem(coreItem);

            builder.setCorpse(corpseBuilder.build());

            // Set the location and position info from the backup.
            PlayerActor last = previous.get();
            if (player.hasBackup()) {
                Backup backup = player.claimBackup();
                builder.clearBackup();

                builder.setX(backup.getPosition().x);
                builder.setY(backup.getPosition().y);
                builder.setSeed(last.getSeed());
                builder.setRegion(backup.getRegion());
                builder.setFloor(backup.getFloor());
            } else {
                builder.setX(last.getX());
                builder.setY(last.getY());
                builder.setSeed(last.getSeed());
                builder.setRegion(last.getRegion());
                builder.setFloor(last.getFloor());
            }

            builder.addAllVisitedRooms(player.getLocation().getVisitedIndices());

            data = builder.build();
        }

        FileHandle handle = Gdx.files.local("saves/" + player.getInfo().getId() + ".dat");
        try {
            final boolean append = false;
            handle.writeBytes(data.toByteArray(), append);
        } catch (Exception ex) {
            InvokenGame.error("Failed writing " + handle.name(), ex);
        }
    }

    public static List<PlayerActor> readSaves() {
        List<PlayerActor> results = new ArrayList<>();

        FileHandle root = Gdx.files.local("saves/");
        FileHandle[] newHandles = root.list();
        for (FileHandle saveHandle : newHandles) {
            if (!saveHandle.isDirectory()) {
                results.add(load(saveHandle));
            }
        }

        return results;
    }

    private static PlayerActor load(FileHandle saveHandle) {
        try {
            return PlayerActor.parseFrom(saveHandle.readBytes());
        } catch (Exception ex) {
            InvokenGame.error("Failed reading " + saveHandle.name(), ex);
            return null;
        }
    }
}