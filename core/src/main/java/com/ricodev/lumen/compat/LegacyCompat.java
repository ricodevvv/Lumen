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
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.material.MaterialData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1.8 implementation. Things Bukkit didn't have back then (no AI, silent, invulnerable)
 * go through the entity's NBT, the same as summoning it with those tags.
 */
public class LegacyCompat implements Compat {

    private Class<?> tagClass;
    private Method getHandle;
    private Method saveTag;
    private Method loadTag;
    private Method setInt;
    private boolean nbtReady;

    public LegacyCompat() {
        if (ServerVersion.atLeast(9)) {
            return;
        }
        try {
            String craftPackage = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            String nmsPackage = craftPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
            Class<?> craftEntity = Class.forName(craftPackage + ".entity.CraftEntity");
            Class<?> nmsEntity = Class.forName(nmsPackage + ".Entity");
            tagClass = Class.forName(nmsPackage + ".NBTTagCompound");
            getHandle = craftEntity.getMethod("getHandle");
            saveTag = nmsEntity.getMethod("c", tagClass);
            loadTag = nmsEntity.getMethod("f", tagClass);
            setInt = tagClass.getMethod("setInt", String.class, int.class);
            nbtReady = true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            nbtReady = false;
        }
    }

    private void setFlag(Entity entity, String key, boolean value) {
        if (!nbtReady) {
            return;
        }
        try {
            Object handle = getHandle.invoke(entity);
            Object tag = tagClass.getConstructor().newInstance();
            saveTag.invoke(handle, tag);
            setInt.invoke(tag, key, value ? 1 : 0);
            loadTag.invoke(handle, tag);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    @Override
    public void setAI(LivingEntity entity, boolean ai) {
        setFlag(entity, "NoAI", !ai);
    }

    @Override
    public void setSilent(Entity entity, boolean silent) {
        setFlag(entity, "Silent", silent);
    }

    @Override
    public void setInvulnerable(Entity entity, boolean invulnerable) {
        setFlag(entity, "Invulnerable", invulnerable);
    }

    @Override
    public void setGravity(Entity entity, boolean gravity) {
        if (entity instanceof ArmorStand) {
            ((ArmorStand) entity).setGravity(gravity);
        }
    }

    @Override
    public void setPersistent(Entity entity, boolean persistent) {
    }

    @Override
    public void setGlowing(Entity entity, boolean glowing) {
    }

    @Override
    public void setScale(LivingEntity entity, double scale) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<Entity> getPassengers(Entity entity) {
        Entity passenger = entity.getPassenger();
        return passenger == null ? Collections.<Entity>emptyList() : new ArrayList<>(Collections.singletonList(passenger));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addPassenger(Entity vehicle, Entity passenger) {
        vehicle.setPassenger(passenger);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setItemInHand(Player player, @Nullable ItemStack itemStack) {
        player.setItemInHand(itemStack);
    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
        return true;
    }

    @Override
    public boolean isMainHand(PlayerInteractEntityEvent event) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendBlockChange(Player player, Location location, XMaterial material) {
        Material type = material.parseMaterial();

        if (type != null) {
            player.sendBlockChange(location, type, material.getData());
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void resetBlockChange(Player player, Block block) {
        player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
    }

    @Override
    public void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        meta.spigot().setUnbreakable(unbreakable);
    }

    @Override
    public boolean isUnbreakable(ItemMeta meta) {
        return meta.spigot().isUnbreakable();
    }

    @Override
    public void setCustomModelData(ItemMeta meta, int customModelData) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable WrappedBlockState blockState(XMaterial material) {
        Material type = material.parseMaterial();
        return type == null ? null : SpigotConversionUtil.fromBukkitMaterialData(new MaterialData(type, material.getData()));
    }

    @Override
    public int getPing(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            return handle.getClass().getField("ping").getInt(handle);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 0;
        }
    }

    @Override
    public ItemStack setItemTag(ItemStack itemStack, String key, String value) {
        ItemNbt nbt = ItemNbt.get();

        if (nbt == null) {
            return itemStack;
        }
        try {
            Object nmsItem = nbt.asNMSCopy.invoke(null, itemStack);

            if (nmsItem == null) {
                return itemStack;
            }
            Object tag = nbt.getTag.invoke(nmsItem);

            if (tag == null) {
                tag = nbt.tagClass.getConstructor().newInstance();
            }
            nbt.setString.invoke(tag, key, value);
            nbt.setTag.invoke(nmsItem, tag);
            return (ItemStack) nbt.asBukkitCopy.invoke(null, nmsItem);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return itemStack;
        }
    }

    @Override
    public @Nullable String getItemTag(ItemStack itemStack, String key) {
        ItemNbt nbt = ItemNbt.get();

        if (nbt == null) {
            return null;
        }
        try {
            Object nmsItem = nbt.asNMSCopy.invoke(null, itemStack);
            Object tag = nmsItem == null ? null : nbt.getTag.invoke(nmsItem);

            if (tag == null || !(Boolean) nbt.hasKey.invoke(tag, key)) {
                return null;
            }
            return (String) nbt.getString.invoke(tag, key);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    @Override
    public void setMaxStackSize(ItemMeta meta, int size) {
    }

    @Override
    public void setGlint(ItemMeta meta, boolean glint) {
        org.bukkit.enchantments.Enchantment enchantment = com.cryptomorin.xseries.XEnchantment.UNBREAKING.get();

        if (enchantment == null) {
            return;
        }
        if (glint) {
            meta.addEnchant(enchantment, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(enchantment);
        }
    }

    /**
     * Reflection handles for item NBT on servers without the persistent data API (before 1.14).
     */
    private static final class ItemNbt {

        private static ItemNbt instance;
        private static boolean loaded;

        private Class<?> tagClass;
        private Method asNMSCopy;
        private Method asBukkitCopy;
        private Method getTag;
        private Method setTag;
        private Method setString;
        private Method getString;
        private Method hasKey;

        static @Nullable ItemNbt get() {
            if (!loaded) {
                loaded = true;
                try {
                    String craftPackage = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
                    String nmsPackage = craftPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
                    ItemNbt nbt = new ItemNbt();
                    Class<?> craftItemStack = Class.forName(craftPackage + ".inventory.CraftItemStack");
                    Class<?> nmsItemStack = Class.forName(nmsPackage + ".ItemStack");
                    nbt.tagClass = Class.forName(nmsPackage + ".NBTTagCompound");
                    nbt.asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
                    nbt.asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);
                    nbt.getTag = nmsItemStack.getMethod("getTag");
                    nbt.setTag = nmsItemStack.getMethod("setTag", nbt.tagClass);
                    nbt.setString = nbt.tagClass.getMethod("setString", String.class, String.class);
                    nbt.getString = nbt.tagClass.getMethod("getString", String.class);
                    nbt.hasKey = nbt.tagClass.getMethod("hasKey", String.class);
                    instance = nbt;
                } catch (ReflectiveOperationException | RuntimeException e) {
                    instance = null;
                }
            }
            return instance;
        }
    }

    @Override
    public void setItemCooldown(Player player, Material material, int ticks) {
    }

    @Override
    public org.bukkit.util.@Nullable Vector getClickedPosition(PlayerInteractEvent event) {
        return Events.get(event, "getClickedPosition");
    }

    @Override
    public void setCollidable(LivingEntity entity, boolean collidable) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void playHurtAnimation(LivingEntity entity) {
        entity.playEffect(org.bukkit.EntityEffect.HURT);
    }

    @Override
    public void hideEntity(org.bukkit.plugin.Plugin plugin, Player player, Entity entity) {
        com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities(entity.getEntityId()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMainHand(LivingEntity entity, ItemStack itemStack) {
        entity.getEquipment().setItemInHand(itemStack);
    }

    @Override
    public void setOffHand(LivingEntity entity, ItemStack itemStack) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMaxHealth(LivingEntity entity, double health) {
        entity.setMaxHealth(health);
        entity.setHealth(health);
    }

    @Override
    public void setMovementSpeed(LivingEntity entity, double speed) {
    }

    @Override
    public void setInvisible(LivingEntity entity, boolean invisible) {
        if (entity instanceof ArmorStand) {
            ((ArmorStand) entity).setVisible(!invisible);
        } else if (invisible) {
            entity.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false), true);
        } else {
            entity.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
        }
    }

    @Override
    public boolean isLeftHanded(Player player) {
        return false;
    }

    @Override
    public void setProjectileItem(Entity projectile, ItemStack itemStack) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, Block block) {
        return location.getWorld().spawnFallingBlock(location, block.getType(), block.getData());
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.bukkit.entity.FallingBlock spawnFallingBlock(Location location, XMaterial material) {
        return location.getWorld().spawnFallingBlock(location, material.parseMaterial(), material.getData());
    }

    protected static XMaterial material(com.ricodev.lumen.api.util.structure.StructureBlock data) {
        XMaterial material = XMaterial.matchXMaterial(data.getMaterial()).orElse(null);
        return material != null && material.isSupported() ? material : XMaterial.STONE;
    }

    @Override
    public void setBlock(Block block, com.ricodev.lumen.api.util.structure.StructureBlock data) {
        com.cryptomorin.xseries.XBlock.setType(block, material(data), false);
    }

    @Override
    public void sendBlockChange(Player player, Location location, com.ricodev.lumen.api.util.structure.StructureBlock data) {
        sendBlockChange(player, location, material(data));
    }

    @Override
    public ItemStack blockItem(com.ricodev.lumen.api.util.structure.StructureBlock data) {
        ItemStack item = material(data).parseItem();
        return item != null ? item : XMaterial.STONE.parseItem();
    }

    @Override
    public String blockDataString(Block block) {
        return "minecraft:" + XMaterial.matchXMaterial(block.getType()).name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public void playChestAnimation(Block block, boolean open) {
    }

    @Override
    public int maxInventoryTitleLength() {
        return 32;
    }
}
