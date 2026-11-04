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
import com.ricodev.lumen.compat.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads item strings like {@code minecraft:player_head[profile={...}]} from the configs.
 * On 1.20.5+ the server parses them itself. Older servers only get the material plus the
 * few components Lumen's configs use: skull textures, hidden tooltips and hidden extras.
 */
public final class ItemParser {

    private static final Pattern TEXTURE = Pattern.compile("value:'([A-Za-z0-9+/=]+)'");

    /**
     * Items that only exist on newer versions, mapped to something close that 1.8 has.
     */
    private static final Map<String, String> FALLBACKS = new HashMap<>();

    /**
     * Item names from the configs this server doesn't have, so they can be listed once at startup.
     */
    public static final Set<String> MISSING = new TreeSet<>();

    static {
        FALLBACKS.put("TIPPED_ARROW", "ARROW");
        FALLBACKS.put("SPECTRAL_ARROW", "ARROW");
        FALLBACKS.put("LIGHT_BLUE_BANNER", "WHITE_BANNER");
        FALLBACKS.put("TOTEM_OF_UNDYING", "GOLDEN_APPLE");
        FALLBACKS.put("WIND_CHARGE", "SNOWBALL");
        FALLBACKS.put("SHULKER_SHELL", "PURPLE_DYE");
        FALLBACKS.put("PHANTOM_MEMBRANE", "FEATHER");
        FALLBACKS.put("HEART_OF_THE_SEA", "PRISMARINE_CRYSTALS");
        FALLBACKS.put("NAUTILUS_SHELL", "PRISMARINE_SHARD");
        FALLBACKS.put("TRIDENT", "STICK");
        FALLBACKS.put("CROSSBOW", "BOW");
        FALLBACKS.put("SHIELD", "IRON_CHESTPLATE");
        FALLBACKS.put("ELYTRA", "FEATHER");
        FALLBACKS.put("GOAT_HORN", "BONE");
        FALLBACKS.put("AMETHYST_SHARD", "PRISMARINE_CRYSTALS");
        FALLBACKS.put("HONEYCOMB", "GOLD_NUGGET");
        FALLBACKS.put("SWEET_BERRIES", "APPLE");
        FALLBACKS.put("GLOW_BERRIES", "APPLE");
        FALLBACKS.put("TROPICAL_FISH", "COD");
    }
    private static Method createItemStack;
    private static boolean nativeChecked;

    private ItemParser() {
    }

    /**
     * @return the parsed item, or null when the material doesn't exist on this version
     */
    public static @Nullable ItemStack parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String item = input.trim();
        ItemStack nativeItem = parseNative(item);

        if (nativeItem != null) {
            return nativeItem;
        }
        int bracket = item.indexOf('[');
        String name = bracket == -1 ? item : item.substring(0, bracket);
        String components = bracket == -1 ? "" : item.substring(bracket);

        if (name.contains(":")) {
            name = name.substring(name.indexOf(':') + 1);
        }
        String upper = name.toUpperCase(Locale.ROOT);
        XMaterial material = XMaterial.matchXMaterial(upper).orElse(null);

        if ((material == null || !material.isSupported()) && FALLBACKS.containsKey(upper)) {
            material = XMaterial.matchXMaterial(FALLBACKS.get(upper)).orElse(null);
        }
        if (material == null || !material.isSupported()) {
            MISSING.add(name.toLowerCase(Locale.ROOT));
            return null;
        }
        ItemStack itemStack = material.parseItem();

        if (itemStack == null) {
            return null;
        }
        Matcher texture = TEXTURE.matcher(components);

        if (texture.find()) {
            itemStack = Skulls.withTexture(itemStack, texture.group(1));
        }
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            if (components.contains("hide_tooltip:true")) {
                meta.setDisplayName(" ");
            }
            if (components.contains("!minecraft:")) {
                meta.addItemFlags(ItemFlag.values());
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private static @Nullable ItemStack parseNative(String item) {
        if (!ServerVersion.atLeast(20, 5)) {
            return null;
        }
        try {
            if (!nativeChecked) {
                nativeChecked = true;
                createItemStack = Bukkit.getItemFactory().getClass().getMethod("createItemStack", String.class);
            }
            return createItemStack == null ? null : (ItemStack) createItemStack.invoke(Bukkit.getItemFactory(), item);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
