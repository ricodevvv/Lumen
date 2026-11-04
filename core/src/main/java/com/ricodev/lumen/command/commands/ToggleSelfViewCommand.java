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
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.command.SimpleCommand;
import com.ricodev.lumen.command.SubCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ToggleSelfViewCommand extends SimpleCommand<CommandSender> {

    public ToggleSelfViewCommand(LumenPlugin plugin) {
        super(plugin, "toggleselfview", "lumen.command.toggleselfview", false);
        setSubCommands(new ArgumentSubCommand(plugin));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        audience(sender).sendMessage(Component.text("/toggleselfview <type>", NamedTextColor.RED));
    }

    private static class ArgumentSubCommand extends SubCommand<CommandSender> {

        private final Map<String, Consumer<User>> TYPES = new HashMap<>();

        public ArgumentSubCommand(LumenPlugin plugin) {
            super(plugin, "lumen.command.toggleselfview", false);

            addArgument(String.class, "category", sender -> new ArrayList<>(TYPES.keySet()));
        }

        @Override
        public void onExecute(CommandSender sender, String[] args) {
            User user = plugin.getUserManager().getConnected((Player) sender);

            if (user == null) {
                audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
                return;
            }
            String inputType = parseArgument(args, 0).toString().toLowerCase();
            Optional<Map.Entry<String, Consumer<User>>> found = TYPES.entrySet().stream().filter(type -> type.getKey().equals(inputType)).findAny();

            if (found.isPresent()) {
                found.get().getValue().accept(user);
                user.sendMessage(user.translate("command.toggleselfview.success"));
            } else {
                user.sendMessage(user.translate("command.toggleselfview.not_equipped"));
            }
        }
    }
}
