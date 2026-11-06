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
package com.ricodev.lumen.compat;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Everything Lumen does that works differently between 1.8 and the latest version.
 * {@link LegacyCompat} covers 1.8, and the modern module picks up from 1.9 onwards.
 */
public interface Compat {

    void setAI(LivingEntity entity, boolean ai);

    void setSilent(Entity entity, boolean silent);

    void setInvulnerable(Entity entity, boolean invulnerable);

    void setGravity(Entity entity, boolean gravity);

    void setPersistent(Entity entity, boolean persistent);

    void setGlowing(Entity entity, boolean glowing);

    void setScale(LivingEntity entity, double scale);

    List<Entity> getPassengers(Entity entity);

    void addPassenger(Entity vehicle, Entity passenger);

    /**
     * Teleports a vehicle without losing whoever is riding it.
     */
    void teleportWithPassengers(Entity vehicle, Location location);

    /**
     * True while {@link #teleportWithPassengers} has the riders off for a moment, so dismount listeners can ignore it.
     */
    boolean isRepositioning(Entity vehicle);

    @Nullable
    ItemStack getItemInHand(Player player);

    void setItemInHand(Player player, @Nullable ItemStack itemStack);

    boolean isMainHand(PlayerInteractEvent event);

    boolean isMainHand(PlayerInteractEntityEvent event);

    void sendBlockChange(Player player, Location location, XMaterial material);

    void resetBlockChange(Player player, Block block);

    void setUnbreakable(ItemMeta meta, boolean unbreakable);

    boolean isUnbreakable(ItemMeta meta);

    void setCustomModelData(ItemMeta meta, int customModelData);

    /**
     * The block state PacketEvents needs for block particles.
     */
    @Nullable
    WrappedBlockState blockState(XMaterial material);

    int getPing(Player player);

    /**
     * Stores a hidden string on the item, PDC on 1.14+ and raw NBT before that.
     */
    ItemStack setItemTag(ItemStack itemStack, String key, String value);

    @Nullable
    String getItemTag(ItemStack itemStack, String key);

    void setMaxStackSize(ItemMeta meta, int size);

    void setGlint(ItemMeta meta, boolean glint);

    void setCollidable(LivingEntity entity, boolean collidable);

    void playHurtAnimation(LivingEntity entity);

    boolean isLeftHanded(Player player);

    /**
     * Real invisibility on 1.16.3+, an invisibility potion before that.
     */
    void setInvisible(LivingEntity entity, boolean invisible);

    void setMaxHealth(LivingEntity entity, double health);

    void setMovementSpeed(LivingEntity entity, double speed);

    void setMainHand(LivingEntity entity, ItemStack itemStack);

    void setOffHand(LivingEntity entity, ItemStack itemStack);

    /**
     * Hides one entity from one player. 1.18+ has an API for it, older servers just get a destroy packet.
     */
    void hideEntity(org.bukkit.plugin.Plugin plugin, Player player, Entity entity);

    /**
     * Changes what a thrown snowball or egg looks like, 1.14+ only.
     */
    void setProjectileItem(Entity projectile, ItemStack itemStack);

    org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, Block block);

    org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, XMaterial material);

    /**
     * Places a structure block, with all its states on 1.13+ and just the material before that.
     */
    void setBlock(Block block, com.ricodev.lumen.api.util.structure.StructureBlock data);

    org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, com.ricodev.lumen.api.util.structure.StructureBlock data);

    void sendBlockChange(Player player, Location location, com.ricodev.lumen.api.util.structure.StructureBlock data);

    ItemStack blockItem(com.ricodev.lumen.api.util.structure.StructureBlock data);

    /**
     * The block as a modern block data string, for saving structures.
     */
    String blockDataString(Block block);

    /**
     * Opens or closes a chest lid for everyone nearby, 1.16+ only.
     */
    void playChestAnimation(Block block, boolean open);

    /**
     * The grey cooldown sweep over an item in the hotbar, 1.11.2+ only.
     */
    void setItemCooldown(Player player, org.bukkit.Material material, int ticks);

    /**
     * Exact spot on the block that got clicked, only some newer versions report it.
     */
    @Nullable
    org.bukkit.util.Vector getClickedPosition(PlayerInteractEvent event);

    /**
     * Max length of an inventory title. 1.8 kicks you if it goes over 32 characters.
     */
    int maxInventoryTitleLength();
}
