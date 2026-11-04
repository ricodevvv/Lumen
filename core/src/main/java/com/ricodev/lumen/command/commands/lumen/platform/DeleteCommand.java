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
package com.ricodev.lumen.command.commands.lumen.platform;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.treasure.TreasureChestPlatform;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.command.SubCommand;

public class DeleteCommand extends SubCommand<CommandSender> {

    public DeleteCommand(LumenPlugin plugin) {
        super(plugin, "lumen.command.platform.delete", false);
        addFlats("platform", "delete");
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        User user = plugin.getUserManager().getConnected((Player) sender);

        if (user == null) {
            audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
            return;
        }
        Location location = player.getLocation();
        Location treasureChestLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TreasureChestPlatform platform = plugin.getTreasureChestManager().getPlatform(treasureChestLocation);

        if (platform == null) {
            user.sendMessage(user.translate("command.platform.delete.not_found"));
            return;
        }

        if (platform.isInUse()) {
            user.sendMessage(user.translate("command.platform.delete.in_use"));
            return;
        }
        plugin.getTreasureChestManager().deletePlatform(platform);
        user.sendMessage(user.translate("command.platform.delete.success"));

        Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, treasureChestLocation, 0);
        Sounds.playAt(location, XSound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.0f);
    }
}
