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
package com.ricodev.lumen.menu.menus;

import com.ricodev.lumen.util.Cooldowns;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.entity.Player;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.cosmetic.status.StatusType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.menu.CosmeticMenuImpl;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

public class StatusMenu extends CosmeticMenuImpl<StatusType> {

    private static final int COOLDOWN = 20;

    public StatusMenu(Lumen plugin, User user, CosmeticCategory<StatusType, ?, ?> category) {
        super(plugin, user, category);
    }

    @Override
    protected void addCustomItems() {
        if (category.getConfig().getBoolean("menu.items.toggle_self_view.enabled")) {
            Player player = getPlayer();
            ItemBuilder itemBuilder = new ItemBuilderImpl(category.getConfig(), "menu.items.toggle_self_view");
            String path = user.hasSelfViewStatus() ? "disable" : "enable";
            itemBuilder.setDisplayName(user.translate("menu.statuses.toggle_self_view." + path + ".name"));
            itemBuilder.setLore(user.translateList("menu.statuses.toggle_self_view." + path + ".desc"));

            if (user.hasSelfViewStatus()) {
                itemBuilder.setGlintOverride(true);
            }

            setItem(itemBuilder.getSlot(), itemBuilder.getItemStack(), event -> {
                if (Cooldowns.has(player, "status")) {
                    return;
                }
                Cooldowns.set(player, "status", COOLDOWN);

                boolean toggle = !user.hasSelfViewStatus();
                user.setSelfViewStatus(toggle, true);
                plugin.getStorage().setSelfViewStatusAsync(user, toggle);

                Sounds.playFor(player, XSound.ENTITY_CHICKEN_EGG, 0.5f, 1.0f);
                refresh();
            });
        }
    }
}
