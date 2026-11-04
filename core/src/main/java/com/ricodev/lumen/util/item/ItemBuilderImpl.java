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
package com.ricodev.lumen.util.item;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.compat.Compats;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.util.LogUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ItemBuilderImpl implements ItemBuilder {

    private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/%s";

    public static final ItemFlag[] ITEM_FLAGS = Arrays.stream(ItemFlag.values())
            .filter(flag -> !flag.name().toLowerCase().contains("lore"))
            .toArray(ItemFlag[]::new);

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private final ItemStack itemStack;
    private int slot;

    public ItemBuilderImpl(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public ItemBuilderImpl(ItemStack itemStack, int slot) {
        this.itemStack = itemStack.clone();
        this.slot = slot;
    }

    public ItemBuilderImpl(Material material) {
        this.itemStack = new ItemStack(material, 1);
    }

    public ItemBuilderImpl(Material material, int amount) {
        this.itemStack = new ItemStack(material, Math.max(1, amount));
    }

    public ItemBuilderImpl(Config config, String path) {
        if (!path.endsWith(".")) {
            path = path + ".";
        }
        String item = config.getString(path + "item");
        ItemStack itemStack = ItemParser.parse(item);

        if (itemStack == null) {
            PLUGIN.getLogger().fine("Item " + item + " doesn't exist on this version [Path: " + path + " Config: " + config.getFile().getName() + "], using a barrier instead.");
            itemStack = fallbackItem();
        }
        this.itemStack = itemStack;
        this.slot = config.getInt(path + "slot");
        int count = config.getInt(path + "count");

        if (count > 0) {
            setAmount(count);
        }
    }

    public ItemBuilderImpl(String itemData) {
        ItemStack itemStack = ItemParser.parse(itemData);
        this.itemStack = itemStack == null ? fallbackItem() : itemStack;
        this.slot = -1;
    }

    private static ItemStack fallbackItem() {
        ItemStack barrier = XMaterial.BARRIER.parseItem();
        return barrier != null ? barrier : new ItemStack(Material.STONE);
    }

    public static ItemBuilderImpl of(ItemStack itemStack) {
        return new ItemBuilderImpl(itemStack);
    }

    public static ItemBuilderImpl of(Material material) {
        return new ItemBuilderImpl(material);
    }

    public static ItemBuilderImpl of(Material material, int amount) {
        return new ItemBuilderImpl(material, amount);
    }

    @Override
    public ItemBuilderImpl setType(Material material) {
        itemStack.setType(material);
        return this;
    }

    @Override
    public ItemBuilderImpl setAmount(int amount) {
        itemStack.setAmount(Math.max(1, amount));
        return this;
    }

    @Override
    public ItemBuilderImpl setDisplayName(@Nullable Component displayName) {
        return modifyItemMeta(meta -> {
            if (displayName != null) {
                PLUGIN.getPlatformAdapter().setDisplayName(meta, displayName);
            }
        });
    }

    @Override
    public Component getDisplayName() {
        return PLUGIN.getPlatformAdapter().getDisplayName(itemStack.getItemMeta());
    }

    @Override
    public ItemBuilderImpl setLore(@Nullable List<Component> lines) {
        return modifyItemMeta(meta -> {
            if (lines != null && !lines.isEmpty()) {
                PLUGIN.getPlatformAdapter().setLore(meta, lines);
            } else {
                meta.setLore(null);
            }
        });
    }

    @Override
    public ItemBuilderImpl addLore(Component line) {
        List<Component> currentLore = getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.add(line);
        return setLore(currentLore);
    }

    @Override
    public ItemBuilderImpl addLore(List<Component> lines) {
        List<Component> currentLore = getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.addAll(lines);
        return setLore(currentLore);
    }

    @Override
    public List<Component> getLore() {
        return PLUGIN.getPlatformAdapter().getLore(itemStack.getItemMeta());
    }

    @Override
    public ItemBuilderImpl setUnbreakable() {
        return setUnbreakable(true);
    }

    @Override
    public ItemBuilderImpl setUnbreakable(boolean unbreakable) {
        return modifyItemMeta(meta -> {
            Compats.get().setUnbreakable(meta, unbreakable);

            if (unbreakable) {
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            } else {
                meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        });
    }

    @Override
    public ItemBuilderImpl setMaxSize(int amount) {
        return modifyItemMeta(meta -> Compats.get().setMaxStackSize(meta, amount));
    }

    @Override
    public ItemBuilderImpl setGlintOverride(boolean override) {
        return modifyItemMeta(meta -> Compats.get().setGlint(meta, override));
    }

    @Override
    public ItemBuilderImpl addEnchant(Enchantment enchantment, int level) {
        return addEnchant(enchantment, level, true);
    }

    @Override
    public ItemBuilderImpl addEnchant(Enchantment enchantment, int level, boolean unsafe) {
        return modifyItemMeta(meta -> meta.addEnchant(enchantment, level, unsafe));
    }

    @Override
    public ItemBuilderImpl removeEnchant(Enchantment enchantment) {
        return modifyItemMeta(meta -> meta.removeEnchant(enchantment));
    }

    @Override
    public ItemBuilderImpl setLeatherArmorColor(Color color) {
        return modifyItemMeta(meta -> {
            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                leatherMeta.setColor(color);
            }
        });
    }

    @Override
    public ItemBuilderImpl addPotionEffect(PotionEffect effect, boolean overwrite) {
        return modifyItemMeta(meta -> {
            if (meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                potionMeta.addCustomEffect(effect, overwrite);
            }
        });
    }

    @Override
    public ItemBuilderImpl setSkullOwner(Player player) {
        return modifyItemMeta(meta -> {
            if (meta instanceof SkullMeta) {
                ((SkullMeta) meta).setOwner(player.getName());
            }
        });
    }

    @Override
    public ItemBuilderImpl setSkullTexture(String texture) {
        if (texture == null || texture.trim().isEmpty()) {
            return this;
        }
        return modifyItemStack(item -> item.setItemMeta(Skulls.withTexture(item, String.format(TEXTURE_URL, texture)).getItemMeta()));
    }

    @Override
    public ItemBuilderImpl addFlags(ItemFlag... flags) {
        return modifyItemMeta(meta -> meta.addItemFlags(flags));
    }

    @Override
    public ItemBuilderImpl removeFlags(ItemFlag... flags) {
        return modifyItemMeta(meta -> meta.removeItemFlags(flags));
    }

    @Override
    public ItemBuilderImpl hide() {
        return addFlags(ITEM_FLAGS);
    }

    @Override
    public ItemBuilderImpl modifyItemMeta(Consumer<ItemMeta> modifier) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            modifier.accept(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return this;
    }

    @Override
    public ItemBuilderImpl modifyItemStack(Consumer<ItemStack> modifier) {
        modifier.accept(itemStack);
        return this;
    }

    @Override
    public boolean hasEnchant(Enchantment enchantment) {
        return itemStack.containsEnchantment(enchantment);
    }

    @Override
    public int getEnchantLevel(Enchantment enchantment) {
        return itemStack.getEnchantmentLevel(enchantment);
    }

    @Override
    public boolean hasEnchants() {
        return !itemStack.getEnchantments().isEmpty();
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return new HashMap<>(itemStack.getEnchantments());
    }

    @Override
    public Material getType() {
        return itemStack.getType();
    }

    @Override
    public int getAmount() {
        return itemStack.getAmount();
    }

    @Override
    public boolean hasDisplayName() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasDisplayName();
    }

    @Override
    public boolean hasLore() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasLore();
    }

    @Override
    public boolean isUnbreakable() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && Compats.get().isUnbreakable(meta);
    }

    @Override
    public String toString() {
        return "ItemBuilder{" +
                "item=" + itemStack.toString() +
                ", slot=" + slot
                + "}";
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }
}
