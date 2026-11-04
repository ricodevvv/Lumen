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
package com.ricodev.lumen.placeholder.outgoing;

import com.ricodev.lumen.api.treasure.TreasureChest;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.Cosmetic;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final LumenPlugin plugin;

    public PlaceholderAPIExpansion(LumenPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (identifier.equals("coins")) {
            if (offlinePlayer == null) {
                return "0";
            }
            User user = plugin.getUserManager().getConnected(offlinePlayer.getUniqueId());

            if (user == null) {
                return "0";
            }
            return String.valueOf(plugin.getEconomyManager().getEconomyProvider().getCoins(user));
        } else if (identifier.contains("equipped_")) {
            for (CosmeticCategory<?, ?, ?> category : plugin.getCategoryRegistries().getCategories()) {
                if (identifier.equals("equipped_" + category.getKey())) {
                    if (offlinePlayer == null) {
                        return "None";
                    }
                    User user = plugin.getUserManager().getConnected(offlinePlayer.getUniqueId());

                    if (user == null) {
                        return "None";
                    }
                    Cosmetic<?, ?> cosmetic = user.getCosmetic(category);

                    if (cosmetic == null) {
                        return "None";
                    }
                    return cosmetic.getType().getName(plugin.getLanguageManager());
                }
            }
        } else if (identifier.contains("unlocked_")) {
            for (CosmeticCategory<?, ?, ?> category : plugin.getCategoryRegistries().getCategories()) {
                if (identifier.equals("unlocked_" + category.getKey())) {
                    int total = category.getCosmeticRegistry().getEnabledTypes().size();

                    if (offlinePlayer == null) {
                        return "0/" + total;
                    }
                    Player player = offlinePlayer.getPlayer();

                    if (player == null) {
                        return "0/" + total;
                    }
                    return category.getUnlockedCosmetics(player) + "/" + total;
                }
            }
        } else if (identifier.contains("treasures_")) {
            for (TreasureChest treasureChest : plugin.getTreasureChestManager().getTreasureChests()) {
                if (identifier.equals("treasures_" + treasureChest.getKey())) {
                    if (offlinePlayer == null) {
                        return "0";
                    }
                    User user = plugin.getUserManager().getConnected(offlinePlayer.getUniqueId());

                    if (user == null) {
                        return "0";
                    }
                    return String.valueOf(user.getTreasureChests(treasureChest));
                }
            }
        }
        return identifier;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @NotNull
    @Override
    public String getAuthor() {
        List<String> authors = plugin.getJavaPlugin().getDescription().getAuthors();

        if (authors.isEmpty()) {
            return "unknown";
        }
        return authors.get(0);
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "lumen";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getJavaPlugin().getDescription().getVersion();
    }
}
