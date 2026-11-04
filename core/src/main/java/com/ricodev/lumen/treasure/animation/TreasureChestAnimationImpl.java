/*
 * This file is part of Lumen, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ricodev.lumen.treasure.animation;

import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.api.util.structure.StructureBlock;

import com.ricodev.lumen.util.entity.CosmeticEntityImpl;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.ricodev.lumen.util.entity.Entities;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.event.PlayerOpenTreasureChestEvent;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.treasure.TreasureChestPlatform;
import com.ricodev.lumen.api.treasure.animation.TreasureChestAnimation;
import com.ricodev.lumen.api.treasure.loot.GeneratedLoot;
import com.ricodev.lumen.api.treasure.loot.LootEntry;
import com.ricodev.lumen.api.treasure.loot.LootTable;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.api.util.structure.type.BlockStructure;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.structure.type.BlockStructureImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class TreasureChestAnimationImpl extends BukkitRunnable implements TreasureChestAnimation, Listener {

    private static final Title.Times DEFAULT_TIMES = Title.Times.times(Ticks.duration(5), Ticks.duration(50), Ticks.duration(5));
    public static final int MAX_TIME_BEFORE_FORCE_OPEN = 1000;

    protected Lumen plugin;
    protected TreasureChestPlatform platform;
    protected TreasureChest treasureChest;
    protected User user;
    protected Player player;

    protected List<CosmeticEntity> blockDisplayChests = new ArrayList<>();
    protected List<CosmeticEntity> texts = new ArrayList<>();
    protected List<CosmeticEntity> items = new ArrayList<>();
    protected List<Location> openedChests = new ArrayList<>();

    protected AnimationState animationState = AnimationState.BUILDING;
    private int buildingStates;
    protected int ticks;

    protected Location location;

    public TreasureChestAnimationImpl(Lumen plugin, TreasureChestPlatform platform, TreasureChest treasureChest, User user) {
        this.plugin = plugin;
        this.platform = platform;
        this.treasureChest = treasureChest;
        this.user = user;
        this.player = user.getPlayer();
        this.location = LocationUtil.center(platform.getCenter().clone());

        platform.hideDisplay();

        if (treasureChest.isOpeningBroadcast()) {
            plugin.getTreasureChestManager().getOpeningBroadcaster().broadcastMessage(
                    player,
                    "treasure_chest.open.broadcast",
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.unparsed("treasure_chest", treasureChest.getName(user))
            );
        }
        user.showTitle(user.translate(
                        "treasure_chest.open.title",
                        Placeholder.unparsed("treasure_chest", treasureChest.getName(user))
                ),
                user.translate(
                        "treasure_chest.open.subtitle",
                        Placeholder.unparsed("amount", String.valueOf(treasureChest.getChestsToOpen()))
                ),
                DEFAULT_TIMES
        );
        Server server = plugin.getJavaPlugin().getServer();
        server.getPluginManager().callEvent(new PlayerOpenTreasureChestEvent(plugin, user, player, treasureChest));
        server.getPluginManager().registerEvents(this, plugin.getJavaPlugin());
        plugin.getJavaPlugin().getLogger().log(Level.INFO, "[TREASURE CHEST] " + user + " is opening a " + treasureChest.getKey() + " treasure chest.");
        runTaskTimer(plugin.getJavaPlugin(), 0L, 1L);
    }

    @Override
    public void run() {
        if (user == null || player == null || !player.isOnline()) {
            reset();
        } else {
            onUpdate();
        }
    }

    public enum AnimationState {

        BUILDING,
        SPAWNING_CHESTS,
        BUILT,
        DONE,
    }

    public void onUpdate() {
        if (ticks % 20 == 0) {
            if (animationState == AnimationState.BUILDING) {
                if (buildingStates < treasureChest.getStructures().size()) {
                    StructureData structureData = treasureChest.getStructures().get(buildingStates++);
                    BlockStructure blockStructure = new BlockStructureImpl(structureData);
                    blockStructure.spawn(platform.getCenter(), true);
                } else {
                    animationState = AnimationState.SPAWNING_CHESTS;
                }
            }
            onSecondUpdate();
            checkPlayersTooClose();
            checkOpenerLocation();
        }
        onTickUpdate();

        if (ticks == MAX_TIME_BEFORE_FORCE_OPEN) {
            forceOpenChests();
        }
        ticks++;
    }

    private void registerOpenedChest(Location location) {
        openedChests.add(location.clone());

        if (openedChests.size() >= treasureChest.getChestsToOpen()) {
            plugin.getJavaPlugin().getServer().getScheduler().runTaskLater(plugin.getJavaPlugin(), this::reset, 100L);
        }
    }

    private boolean isChestOpened(Location location) {
        return openedChests.contains(location);
    }

    private void openChest(Block block) {
        Location blockLocation = block.getLocation().add(0.5d, 0.0d, 0.5d);

        if (openedChests.size() >= treasureChest.getChestsToOpen() || isChestOpened(blockLocation)) {
            return;
        }
        registerOpenedChest(blockLocation);
        Location location = blockLocation.add(0.0d, 0.7d, 0.0d);
        LootTable lootTable = treasureChest.getLootTable();
        giveRandomLoot(lootTable, location);

        Sounds.playAt(location, XSound.BLOCK_CHEST_OPEN, 0.4f, 0.0f);
        Compats.get().playChestAnimation(block, true);
    }

    private void giveRandomLoot(LootTable lootTable, Location location) {
        LootEntry lootEntry = lootTable.rollLoot(player);

        if (lootEntry == null) {
            return;
        }
        GeneratedLoot generatedLoot = lootEntry.generate(player);

        if (generatedLoot == null) {
            plugin.getJavaPlugin().getLogger().warning("Failed to generate loot for entry " + lootEntry.getKey() + ".");
            return;
        }
        generatedLoot.give(player, user);

        CosmeticRarity rarity = generatedLoot.getRarity();
        spawnFirework(location, rarity);

        CosmeticEntity nmsEntity = CosmeticEntities.create(location.getWorld(), Entities.itemType());
        nmsEntity.setEntityItemStack(generatedLoot.getItemStack());
        nmsEntity.setVelocity(0.0d, 0.2d, 0.0d);
        nmsEntity.setPositionRotation(location);
        nmsEntity.getTracker().startTracking();
        items.add(nmsEntity);

        CosmeticEntityImpl text = CosmeticEntities.createItemHolder(location.getWorld(), null, null);
        text.setCustomName(user.translate(
                    generatedLoot.getHologramTranslationKey(),
                    Placeholder.unparsed("category", generatedLoot.getCategory().getName(user)),
                    Placeholder.component("loot", generatedLoot.getResolvedName(user)),
                    rarity.getResolvers(user),
                    generatedLoot.getHologramResolvers(user)
            ));
        text.setPositionRotation(location.clone().add(0.0d, 1.0d, 0.0d));
        text.getTracker().startTracking();
        texts.add(text);
    }

    private void spawnFirework(Location location, CosmeticRarity rarity) {
        if (rarity.getDetonations() > 0) {
            new BukkitRunnable() {
                int i;

                @Override
                public void run() {
                    Entities.spawn(location, Firework.class, entity -> {
                        FireworkMeta fireworkMeta = entity.getFireworkMeta();
                        fireworkMeta.addEffect(rarity.getFireworkEffect());
                        entity.setFireworkMeta(fireworkMeta);

                        MetadataUtil.setCustomEntity(entity);
                    }).detonate();

                    if (++i >= rarity.getDetonations()) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin.getJavaPlugin(), 0L, rarity.getTickInterval());
        }
    }

    public void forceOpenChests() {
        for (Location location : platform.getChestLocations()) {
            openChest(location.getBlock());
        }
    }

    protected void setChestBlock(Material material, Location location) {
        Block block = location.getBlock();
        block.setType(material);
        MetadataUtil.setCustomBlock(block);

        Compats.get().setBlock(block, new StructureBlock("minecraft:" + XMaterial.matchXMaterial(material).name().toLowerCase(java.util.Locale.ROOT)
                + "[facing=" + MathUtil.yawToFace(platform.getCenter(), location).name().toLowerCase(java.util.Locale.ROOT) + "]"));
        Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, location, 1);
        Sounds.playAt(block.getLocation(), XSound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.0f);
    }

    protected void despawnChest() {
        for (CosmeticEntity nmsEntity : blockDisplayChests) {
            nmsEntity.getTracker().destroy();
        }
        blockDisplayChests.clear();
    }

    protected CosmeticEntity spawnChest(Material material, Location location) {
        CosmeticEntity nmsEntity = CosmeticEntities.createItemHolder(location.getWorld(), new ItemStack(material), null);
        nmsEntity.setPositionRotation(location.clone().add(0.0d, -0.4d, 0.0d));
        nmsEntity.getTracker().startTracking();
        blockDisplayChests.add(nmsEntity);

        return nmsEntity;
    }

    public void reset() {
        if (treasureChest == null) {
            return;
        }
        if (player != null && player.getLocation().getBlock().equals(platform.getCenter().getBlock())) {
            player.setVelocity(new Vector(0.0d, 0.5d, 0.0d));
        }
        user.setCurrentPlatform(null);
        platform.setUser(null);
        platform.build();

        for (Location location : platform.getChestLocations()) {
            Block block = location.getBlock();

            MetadataUtil.removeCustomBlock(block);
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
            block.setType(Material.AIR);
        }
        despawnChest();

        items.forEach(entity -> entity.getTracker().destroy());
        items.clear();

        texts.forEach(entity -> entity.getTracker().destroy());
        texts.clear();

        plugin = null;
        platform = null;
        treasureChest = null;
        blockDisplayChests = null;
        items = null;
        openedChests = null;
        texts = null;

        HandlerList.unregisterAll(this);
        cancel();
    }

    private void checkOpenerLocation() {
        Location center = platform.getCenter();
        player.getLocation(location);

        if (openedChests.size() >= treasureChest.getChestsToOpen()
                || center.getWorld() != player.getWorld()
                || center.distanceSquared(location) < platform.getBlockStructure().getData().getHalfSizeSquared()) {
            return;
        }
        Location teleportLocation = platform.getCenter().clone();
        teleportLocation.setPitch(location.getPitch());
        teleportLocation.setYaw(location.getYaw());

        player.teleport(teleportLocation);
    }

    private void checkPlayersTooClose() {
        for (Player player : MathUtil.getClosestPlayersFromLocationSquared(location, platform.getBlockStructure().getData().getHalfSizeSquared())) {
            if (user.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                vehicle.eject();
            }
            MathUtil.pushEntity(player, location, 1.5d, 1.0d);
            User otherUser = plugin.getUserManager().getConnected(player);

            if (otherUser != null) {
                otherUser.setFallDamageProtection(4);
            }
        }
    }

    @EventHandler
    public void onClickChest(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (user.getUniqueId().equals(event.getPlayer().getUniqueId())
                && platform.getChestLocations().contains(LocationUtil.center(block.getLocation()))) {
            event.setCancelled(true);

            openChest(block);
        }
    }
}
