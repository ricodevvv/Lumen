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
package com.ricodev.lumen.modern;

import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import com.ricodev.lumen.compat.LegacyCompat;
import com.ricodev.lumen.compat.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.9 and newer. Compiled against the 1.16.5 API, so anything added after 1.9 is
 * gated by version and falls back to the 1.8 behaviour when it isn't there.
 */
public class ModernCompat extends LegacyCompat {

    private static final @Nullable Method PAPER_TELEPORT;
    private static final @Nullable Object RETAIN_PASSENGERS;

    static {
        Method teleport = null;
        Object flags = null;

        try {
            Class<?> flagClass = Class.forName("io.papermc.paper.entity.TeleportFlag");
            Class<?> stateClass = Class.forName("io.papermc.paper.entity.TeleportFlag$EntityState");
            Object retain = stateClass.getField("RETAIN_PASSENGERS").get(null);
            flags = Array.newInstance(flagClass, 1);
            Array.set(flags, 0, retain);
            teleport = Entity.class.getMethod("teleport", Location.class, flags.getClass());
        } catch (ReflectiveOperationException | LinkageError ignored) {
            flags = null;
        }
        PAPER_TELEPORT = teleport;
        RETAIN_PASSENGERS = flags;
    }

    @Override
    public void setAI(LivingEntity entity, boolean ai) {
        entity.setAI(ai);
    }

    @Override
    public void setSilent(Entity entity, boolean silent) {
        entity.setSilent(silent);
    }

    @Override
    public void setInvulnerable(Entity entity, boolean invulnerable) {
        entity.setInvulnerable(invulnerable);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        if (ServerVersion.atLeast(10)) {
            entity.setGravity(gravity);
        } else {
            super.setGravity(entity, gravity);
        }
    }

    @Override
    public void setPersistent(Entity entity, boolean persistent) {
        if (ServerVersion.atLeast(13)) {
            entity.setPersistent(persistent);
        }
    }

    @Override
    public void setGlowing(Entity entity, boolean glowing) {
        entity.setGlowing(glowing);
    }

    @Override
    public void setScale(LivingEntity entity, double scale) {
        if (!ServerVersion.atLeast(20, 5) || scale == 1.0d) {
            return;
        }
        Attribute attribute = XAttribute.SCALE.get();

        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);

            if (instance != null) {
                instance.setBaseValue(scale);
            }
        }
    }

    @Override
    public List<Entity> getPassengers(Entity entity) {
        if (ServerVersion.atLeast(12)) {
            return new ArrayList<>(entity.getPassengers());
        }
        return super.getPassengers(entity);
    }

    @Override
    public void addPassenger(Entity vehicle, Entity passenger) {
        if (ServerVersion.atLeast(12)) {
            vehicle.addPassenger(passenger);
        } else {
            super.addPassenger(vehicle, passenger);
        }
    }

    @Override
    public void teleportWithPassengers(Entity vehicle, Location location) {
        if (PAPER_TELEPORT != null && !getPassengers(vehicle).isEmpty()) {
            Location current = vehicle.getLocation();
            Location target = location.clone();
            target.setYaw(current.getYaw());
            target.setPitch(current.getPitch());

            try {
                PAPER_TELEPORT.invoke(vehicle, target, RETAIN_PASSENGERS);
                vehicle.setRotation(location.getYaw(), location.getPitch());
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        super.teleportWithPassengers(vehicle, location);
    }

    @Override
    public @Nullable ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInHand(Player player, @Nullable ItemStack itemStack) {
        player.getInventory().setItemInMainHand(itemStack);
    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
        return event.getHand() != EquipmentSlot.OFF_HAND;
    }

    @Override
    public boolean isMainHand(PlayerInteractEntityEvent event) {
        return event.getHand() != EquipmentSlot.OFF_HAND;
    }

    @Override
    public void sendBlockChange(Player player, Location location, XMaterial material) {
        if (!ServerVersion.atLeast(13)) {
            super.sendBlockChange(player, location, material);
            return;
        }
        Material type = material.get();

        if (type != null && type.isBlock()) {
            player.sendBlockChange(location, type.createBlockData());
        }
    }

    @Override
    public void resetBlockChange(Player player, Block block) {
        if (ServerVersion.atLeast(13)) {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        } else {
            super.resetBlockChange(player, block);
        }
    }

    @Override
    public void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        if (ServerVersion.atLeast(11)) {
            meta.setUnbreakable(unbreakable);
        } else {
            super.setUnbreakable(meta, unbreakable);
        }
    }

    @Override
    public boolean isUnbreakable(ItemMeta meta) {
        return ServerVersion.atLeast(11) ? meta.isUnbreakable() : super.isUnbreakable(meta);
    }

    @Override
    public void setCustomModelData(ItemMeta meta, int customModelData) {
        if (ServerVersion.atLeast(14)) {
            meta.setCustomModelData(customModelData);
        }
    }

    @Override
    public @Nullable WrappedBlockState blockState(XMaterial material) {
        if (!ServerVersion.atLeast(13)) {
            return super.blockState(material);
        }
        Material type = material.get();
        return type == null || !type.isBlock() ? null : SpigotConversionUtil.fromBukkitBlockData(type.createBlockData());
    }

    @Override
    public int getPing(Player player) {
        if (ServerVersion.atLeast(17)) {
            return player.getPing();
        }
        return super.getPing(player);
    }

    @Override
    public ItemStack setItemTag(ItemStack itemStack, String key, String value) {
        if (!ServerVersion.atLeast(14)) {
            return super.setItemTag(itemStack, key, value);
        }
        ItemStack copy = itemStack.clone();
        ItemMeta meta = copy.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey("lumen", key), PersistentDataType.STRING, value);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    @Override
    public @Nullable String getItemTag(ItemStack itemStack, String key) {
        if (!ServerVersion.atLeast(14)) {
            return super.getItemTag(itemStack, key);
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(new NamespacedKey("lumen", key), PersistentDataType.STRING);
    }

    @Override
    public void setMaxStackSize(ItemMeta meta, int size) {
        invokeIfPresent(meta, "setMaxStackSize", Integer.class, size);
    }

    @Override
    public void setGlint(ItemMeta meta, boolean glint) {
        if (!invokeIfPresent(meta, "setEnchantmentGlintOverride", Boolean.class, glint)) {
            super.setGlint(meta, glint);
        }
    }

    private static boolean invokeIfPresent(Object target, String name, Class<?> type, Object value) {
        try {
            Method method = target.getClass().getMethod(name, type);
            method.invoke(target, value);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    @Override
    public void setItemCooldown(Player player, Material material, int ticks) {
        if (ServerVersion.atLeast(12)) {
            player.setCooldown(material, ticks);
        }
    }

    @Override
    public void setCollidable(LivingEntity entity, boolean collidable) {
        entity.setCollidable(collidable);
    }

    @Override
    public void playHurtAnimation(LivingEntity entity) {
        if (!invokeIfPresent(entity, "playHurtAnimation", float.class, 0.0f)) {
            super.playHurtAnimation(entity);
        }
    }

    @Override
    public void hideEntity(org.bukkit.plugin.Plugin plugin, Player player, Entity entity) {
        try {
            Player.class.getMethod("hideEntity", org.bukkit.plugin.Plugin.class, Entity.class).invoke(player, plugin, entity);
        } catch (ReflectiveOperationException | RuntimeException e) {
            super.hideEntity(plugin, player, entity);
        }
    }

    @Override
    public void setMainHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInMainHand(itemStack);
    }

    @Override
    public void setOffHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInOffHand(itemStack);
    }

    @Override
    public void setMaxHealth(LivingEntity entity, double health) {
        if (setAttribute(entity, XAttribute.MAX_HEALTH, health)) {
            entity.setHealth(health);
        }
    }

    @Override
    public void setMovementSpeed(LivingEntity entity, double speed) {
        setAttribute(entity, XAttribute.MOVEMENT_SPEED, speed);
    }

    private static boolean setAttribute(LivingEntity entity, XAttribute attribute, double value) {
        Attribute bukkit = attribute.get();
        AttributeInstance instance = bukkit == null ? null : entity.getAttribute(bukkit);

        if (instance == null) {
            return false;
        }
        instance.setBaseValue(value);
        return true;
    }

    @Override
    public void setInvisible(LivingEntity entity, boolean invisible) {
        if (!ServerVersion.atLeast(16, 3) || !invokeIfPresent(entity, "setInvisible", boolean.class, invisible)) {
            super.setInvisible(entity, invisible);
        }
    }

    @Override
    public boolean isLeftHanded(Player player) {
        return player.getMainHand() == org.bukkit.inventory.MainHand.LEFT;
    }

    @Override
    public void setProjectileItem(Entity projectile, ItemStack itemStack) {
        if (ServerVersion.atLeast(14) && projectile instanceof org.bukkit.entity.ThrowableProjectile) {
            ((org.bukkit.entity.ThrowableProjectile) projectile).setItem(itemStack);
        }
    }

    @Override
    public org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, Block block) {
        if (ServerVersion.atLeast(13)) {
            return location.getWorld().spawnFallingBlock(location, block.getBlockData());
        }
        return super.spawnFallingBlock(location, block);
    }

    @Override
    public org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, XMaterial material) {
        if (ServerVersion.atLeast(13)) {
            return location.getWorld().spawnFallingBlock(location, material.get().createBlockData());
        }
        return super.spawnFallingBlock(location, material);
    }

    private static org.bukkit.block.data.@Nullable BlockData blockData(com.ricodev.lumen.api.util.structure.StructureBlock data) {
        try {
            return org.bukkit.Bukkit.createBlockData(data.getData());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void setBlock(Block block, com.ricodev.lumen.api.util.structure.StructureBlock data) {
        org.bukkit.block.data.BlockData blockData = ServerVersion.atLeast(13) ? blockData(data) : null;

        if (blockData != null) {
            block.setBlockData(blockData, false);
        } else {
            super.setBlock(block, data);
        }
    }

    @Override
    public void sendBlockChange(Player player, Location location, com.ricodev.lumen.api.util.structure.StructureBlock data) {
        org.bukkit.block.data.BlockData blockData = ServerVersion.atLeast(13) ? blockData(data) : null;

        if (blockData != null) {
            player.sendBlockChange(location, blockData);
        } else {
            super.sendBlockChange(player, location, data);
        }
    }

    @Override
    public String blockDataString(Block block) {
        return ServerVersion.atLeast(13) ? block.getBlockData().getAsString() : super.blockDataString(block);
    }

    @Override
    public void playChestAnimation(Block block, boolean open) {
        try {
            Object state = block.getState();
            state.getClass().getMethod(open ? "open" : "close").invoke(state);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    @Override
    public int maxInventoryTitleLength() {
        return ServerVersion.atLeast(13) ? 256 : 32;
    }
}
