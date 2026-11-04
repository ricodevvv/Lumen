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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.command.SimpleCommand;
import com.ricodev.lumen.command.argument.Argument;
import com.ricodev.lumen.command.commands.lumen.*;
import com.ricodev.lumen.command.commands.lumen.EquipCommand;
import com.ricodev.lumen.command.commands.lumen.UnequipCommand;
import com.ricodev.lumen.command.commands.lumen.add.AddAmmoCommand;
import com.ricodev.lumen.command.commands.lumen.add.AddCoinsCommand;
import com.ricodev.lumen.command.commands.lumen.add.AddCosmeticCommand;
import com.ricodev.lumen.command.commands.lumen.addall.AddAllAmmoCommand;
import com.ricodev.lumen.command.commands.lumen.addall.AddAllCoinsCommand;
import com.ricodev.lumen.command.commands.lumen.addall.AddAllCosmeticCommand;
import com.ricodev.lumen.command.commands.lumen.get.GetAmmoCommand;
import com.ricodev.lumen.command.commands.lumen.get.GetCoinsCommand;
import com.ricodev.lumen.command.commands.lumen.remove.RemoveAmmoCommand;
import com.ricodev.lumen.command.commands.lumen.remove.RemoveCoinsCommand;
import com.ricodev.lumen.command.commands.lumen.set.SetAmmoCommand;
import com.ricodev.lumen.command.commands.lumen.set.SetCoinsCommand;

public class LumenCommand extends SimpleCommand<CommandSender> {

    public LumenCommand(LumenPlugin plugin) {
        super(plugin, "lumen", "lumen.command");
        setSubCommands(
                new AddAmmoCommand(plugin),
                new AddCoinsCommand(plugin),
                new AddCosmeticCommand(plugin),

                new GetAmmoCommand(plugin),
                new GetCoinsCommand(plugin),

                new AddAllAmmoCommand(plugin),
                new AddAllCoinsCommand(plugin),
                new AddAllCosmeticCommand(plugin),

                new SetAmmoCommand(plugin),
                new SetCoinsCommand(plugin),

                new RemoveAmmoCommand(plugin),
                new RemoveCoinsCommand(plugin),

                new EquipCommand(plugin),
                new InfoCommand(plugin),
                new ItemCommand(plugin),
                new MenuCommand(plugin),
                new PermissionCommand(plugin),
                new ReloadCommand(plugin),
                new UnequipAllPlayersCommand(plugin),
                new UnequipCommand(plugin)
        );
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(plugin.getDescription().getName() + " Commands", NamedTextColor.AQUA));

        getSubCommands().forEach(subCommand -> {
            builder.append(Component.newline(),
                    Component.text("/lumen ", NamedTextColor.YELLOW)
            );

            for (Argument<?> argument : subCommand.getArguments()) {
                builder.append(Component.text(argument.getType() == null ? argument.getArgument() + " " : "<" + argument.getArgument() + "> ", NamedTextColor.YELLOW));
            }
        });
        audience(sender).sendMessage(builder);
    }
}
