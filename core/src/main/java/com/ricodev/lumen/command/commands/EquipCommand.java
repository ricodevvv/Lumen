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
package com.ricodev.lumen.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.Cosmetic;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.command.SimpleCommand;
import com.ricodev.lumen.command.SubCommand;

import java.util.stream.Collectors;

public class EquipCommand extends SimpleCommand<CommandSender> {

    public EquipCommand(LumenPlugin plugin) {
        super(plugin, "equip", "lumen.command.equip", false);
        setSubCommands(new ArgumentSubCommand(plugin));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        audience(sender).sendMessage(Component.text("/equip <category> <cosmetic>", NamedTextColor.RED));
    }

    private static class ArgumentSubCommand extends SubCommand<CommandSender> {

        public ArgumentSubCommand(LumenPlugin plugin) {
            super(plugin, "lumen.command.equip", false);
            addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream().filter(CosmeticCategory::isEnabled).map(CosmeticCategory::getKey).collect(Collectors.toList()));
            addArgument(String.class, "cosmetic");
        }

        @Override
        public void onExecute(CommandSender sender, String[] args) {
            User user = plugin.getUserManager().getConnected((Player) sender);

            if (user == null) {
                audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
                return;
            }
            String type = parseArgument(args, 0);
            CosmeticCategory<?, ?, ?> category = plugin.getCategoryRegistries().getCategoryRaw(type);

            if (category == null || !category.isEnabled()) {
                user.sendMessage(user.translate("category.not_found"));
                return;
            }
            String cosmeticName = parseArgument(args, 1);
            CosmeticType<?, ?> cosmeticType = category.getCosmeticRegistry().getEnabledType(cosmeticName);

            if (cosmeticType == null) {
                user.sendMessage(user.translate("cosmetic.not_found"));
                return;
            }
            Cosmetic<?, ?> cosmetic = user.getCosmetic(category);

            if (cosmetic != null && cosmetic.getType().equals(cosmeticType)) {
                user.sendMessage(user.translate("cosmetic.already_equipped"));
                return;
            }
            cosmeticType.equip(user, false, true);
        }
    }
}
