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
package com.ricodev.lumen.platform;

import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import com.ricodev.lumen.api.platform.PlatformAdapter;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.ServerVersion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns Adventure components into section-sign strings, which every version from 1.8 up understands.
 * Hex colors survive on 1.16+, older servers get the closest named color.
 */
public class LegacyAdapter implements PlatformAdapter {

    private final LegacyComponentSerializer serializer = ServerVersion.atLeast(16)
            ? LegacyComponentSerializer.builder().character(LegacyComponentSerializer.SECTION_CHAR).hexColors().useUnusualXRepeatedCharacterHexFormat().build()
            : LegacyComponentSerializer.legacySection();

    public String serialize(Component component) {
        return serializer.serialize(component);
    }

    @Override
    public Audience audience(Player player) {
        return new Audience() {
            @Override
            public void sendMessage(@NotNull Component message) {
                player.sendMessage(serialize(message));
            }

            @Override
            public void sendActionBar(@NotNull Component message) {
                ActionBar.sendActionBar(player, serialize(message));
            }

            @Override
            public void showTitle(@NotNull Title title) {
                Title.Times times = title.times();
                int fadeIn = times == null ? 10 : (int) (times.fadeIn().toMillis() / 50);
                int stay = times == null ? 70 : (int) (times.stay().toMillis() / 50);
                int fadeOut = times == null ? 20 : (int) (times.fadeOut().toMillis() / 50);
                Titles.sendTitle(player, fadeIn, stay, fadeOut, serialize(title.title()), serialize(title.subtitle()));
            }
        };
    }

    @Override
    public Audience audience(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            return audience((Player) commandSender);
        }
        return new Audience() {
            @Override
            public void sendMessage(@NotNull Component message) {
                commandSender.sendMessage(serialize(message));
            }
        };
    }

    @Override
    public void setCustomName(Entity entity, Component component) {
        entity.setCustomName(serialize(component));
    }

    @Override
    public @Nullable Component getDisplayName(ItemMeta itemMeta) {
        if (!itemMeta.hasDisplayName()) {
            return null;
        }
        return serializer.deserialize(itemMeta.getDisplayName());
    }

    @Override
    public void setDisplayName(ItemMeta itemMeta, Component component) {
        itemMeta.setDisplayName(serialize(component));
    }

    @Override
    public @Nullable List<Component> getLore(ItemMeta itemMeta) {
        if (!itemMeta.hasLore()) {
            return null;
        }
        List<Component> components = new ArrayList<>();

        for (String line : itemMeta.getLore()) {
            components.add(serializer.deserialize(line));
        }
        return components;
    }

    @Override
    public void setLore(ItemMeta itemMeta, List<Component> components) {
        List<String> lore = new ArrayList<>();

        for (Component component : components) {
            lore.add(serialize(component));
        }
        itemMeta.setLore(lore);
    }

    @Override
    public Inventory createInventory(Player player, int rows, Component title) {
        return Bukkit.createInventory(player, rows, fitTitle(serialize(title)));
    }

    private static String fitTitle(String title) {
        int max = Compats.get().maxInventoryTitleLength();

        if (title.length() <= max) {
            return title;
        }
        String cut = title.substring(0, max);

        if (cut.endsWith(String.valueOf(LegacyComponentSerializer.SECTION_CHAR))) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut;
    }
}
