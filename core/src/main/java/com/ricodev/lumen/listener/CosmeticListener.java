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
package com.ricodev.lumen.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.Cosmetic;
import com.ricodev.lumen.api.cosmetic.gadget.Gadget;
import com.ricodev.lumen.api.cosmetic.pet.Pet;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;

public class CosmeticListener implements Listener {

    private static final int MAX_DISTANCE_SQUARED = 128 * 128;

    private final LumenPlugin plugin;

    public CosmeticListener(LumenPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        User user = plugin.getUserManager().getConnected(event.getEntity());

        if (user != null) {
            for (Cosmetic<?, ?> cosmetic : user.getCosmetics().values()) {
                CosmeticCategory<?, ?, ?> category = cosmetic.getType().getCategory();

                if (category.equals(plugin.getCategoryRegistries().arrowEffects())
                        || category.equals(plugin.getCategoryRegistries().deathEffects())) {
                    continue;
                }

                if (category.equals(plugin.getCategoryRegistries().music())) {
                    user.removeCosmetic(category, true, false);
                } else {
                    user.unequipCosmetic(category, true, false);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getWorld().equals(to.getWorld()) && from.distanceSquared(to) < MAX_DISTANCE_SQUARED) {
            return;
        }
        to = to.clone();

        Player player = event.getPlayer();
        User user = plugin.getUserManager().getConnected(player);

        if (user == null) {
            return;
        }
        Gadget gadget = (Gadget) user.getCosmetic(plugin.getCategoryRegistries().gadgets());
        Pet pet = (Pet) user.getCosmetic(plugin.getCategoryRegistries().pets());

        if (gadget != null && gadget.isEquipped() && gadget.getBehavior().shouldUnequipOnTeleport()) {
            gadget.unequip(false, true);
        }

        if (pet != null && pet.isEquipped()) {
            pet.spawn(to);
        }

    }
}
