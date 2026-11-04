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

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Puts custom textures on player heads. 1.18.1+ goes through Bukkit's profile API,
 * older servers get a GameProfile set straight on the skull meta.
 */
public final class Skulls {

    private static final Pattern URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

    private Skulls() {
    }

    /**
     * @param texture either the base64 value from a texture property or a textures.minecraft.net URL
     */
    public static ItemStack withTexture(ItemStack itemStack, String texture) {
        ItemMeta meta = itemStack.getItemMeta();

        if (!(meta instanceof SkullMeta) || texture == null || texture.isEmpty()) {
            return itemStack;
        }
        String url = texture.startsWith("http") ? texture : urlFromBase64(texture);
        String base64 = texture.startsWith("http") ? base64FromUrl(texture) : texture;
        UUID id = UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8));

        if ((url != null && applyPlayerProfile(meta, id, url)) || applyGameProfile(meta, id, base64)) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private static boolean applyPlayerProfile(ItemMeta meta, UUID id, String url) {
        try {
            Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
            Object profile = Bukkit.getServer().getClass().getMethod("createPlayerProfile", UUID.class, String.class)
                    .invoke(Bukkit.getServer(), id, "lumen");
            Object textures = profileClass.getMethod("getTextures").invoke(profile);
            texturesClass.getMethod("setSkin", URL.class).invoke(textures, new URL(url));
            profileClass.getMethod("setTextures", texturesClass).invoke(profile, textures);
            SkullMeta.class.getMethod("setOwnerProfile", profileClass).invoke(meta, profile);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | java.net.MalformedURLException e) {
            return false;
        }
    }

    private static boolean applyGameProfile(ItemMeta meta, UUID id, String base64) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(id, "lumen");
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64);
            properties.getClass().getMethod("put", Object.class, Object.class).invoke(properties, "textures", property);

            try {
                Method setProfile = meta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
                setProfile.setAccessible(true);
                setProfile.invoke(meta, profile);
            } catch (NoSuchMethodException e) {
                Field field = meta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(meta, profile);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static String urlFromBase64(String base64) {
        try {
            Matcher matcher = URL_PATTERN.matcher(new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8));
            return matcher.find() ? matcher.group(1) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String base64FromUrl(String url) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
