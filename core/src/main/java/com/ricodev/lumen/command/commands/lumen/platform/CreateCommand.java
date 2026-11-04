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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.command.SubCommand;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.structure.NamedStructureData;
import com.ricodev.lumen.util.structure.StructureDataImpl;
import com.ricodev.lumen.util.structure.StructureReader;

public class CreateCommand extends SubCommand<CommandSender> {

    public CreateCommand(LumenPlugin plugin) {
        super(plugin, "lumen.command.platform.create", false);
        addFlats("platform", "create");
        addArgument(String.class, "layout");
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        User user = plugin.getUserManager().getConnected(player);

        if (user == null) {
            audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
            return;
        }
        Location location = player.getLocation();
        location.setY(location.getBlockY());
        LocationUtil.center(location);

        if (location.getBlock().getType() != Material.CHEST) {
            user.sendMessage(user.translate("command.platform.create.not_on_chest"));
            return;
        }

        if (plugin.getTreasureChestManager().getPlatform(location) != null) {
            user.sendMessage(user.translate("command.platform.create.already_exists"));
            return;
        }

        if (MathUtil.getIn3DRadius(location, 5).stream().anyMatch(MetadataUtil::isCustomBlock)) {
            user.sendMessage(user.translate("command.platform.create.too_close"));
            return;
        }
        String layout = parseArgument(args, 2);
        StructureDataImpl structureData = StructureReader.loadStructure(layout);

        if (structureData == null) {
            user.sendMessage(user.translate("command.platform.create.layout_not_found"));
            return;
        }
        NamedStructureData namedStructureData = new NamedStructureData(layout, structureData);
        plugin.getTreasureChestManager().createPlatform(location, namedStructureData);

        user.sendMessage(user.translate("command.platform.create.success"));
        Sounds.playFor(player, location, XSound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}
