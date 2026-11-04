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
package com.ricodev.lumen.command.commands.lumen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.locale.Translator;
import com.ricodev.lumen.command.SubCommand;

import java.util.stream.Collectors;

public class InfoCommand extends SubCommand<CommandSender> {

    public InfoCommand(LumenPlugin plugin) {
        super(plugin, "lumen.command.info", true);
        addFlat("info");
        addArgument(String.class, "target", sender -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Translator translator = translator(sender);
        String name = parseArgument(args, 1);

        plugin.getUserManager().getAsync(name).thenAccept(user -> {
            if (user == null) {
                audience(sender).sendMessage(translator.translate("generic.error.player_data.target"));
                return;
            }
            TextComponent.Builder messageBuilder = Component.text();

            messageBuilder.append(translator.translate(
                    "command.info.header",
                    Placeholder.unparsed("player", user.getName())
            ), Component.newline());

            messageBuilder.append(translator.translate(
                    "command.info.id",
                    Placeholder.parsed("id", String.valueOf(user.getDatabaseId()))
            ), Component.newline());

            messageBuilder.append(translator.translate(
                    "command.info.uuid",
                    Placeholder.parsed("uuid", user.getUniqueId().toString())
            ), Component.newline());

            messageBuilder.append(translator.translate(
                    "command.info.coins",
                    Placeholder.unparsed("amount", String.valueOf(plugin.getEconomyManager().getEconomyProvider().getCoins(user)))
            ), Component.newline());

            String morphStatus = user.hasSelfViewMorph() ? "command.info.status.enable" : "command.info.status.disable";
            messageBuilder.append(translator.translate(
                    "command.info.self_view.morph",
                    Placeholder.component("status", translator.translate(morphStatus))
            ), Component.newline());

            String statusStatus = user.hasSelfViewStatus() ? "command.info.status.enable" : "command.info.status.disable";
            messageBuilder.append(translator.translate(
                    "command.info.self_view.status",
                    Placeholder.component("status", translator.translate(statusStatus))
            ), Component.newline());

            if (!user.getCosmetics().isEmpty()) {
                messageBuilder.append(translator.translate("command.info.equipped_cosmetics.header"),
                        Component.newline(),
                        Component.join(JoinConfiguration.newlines(), user.getCosmetics().entrySet().stream()
                                .map(entry -> translator.translate(
                                        "command.info.equipped_cosmetics.entry",
                                        Placeholder.unparsed("category", entry.getKey().getKey()),
                                        Placeholder.unparsed("cosmetic", entry.getValue().getType().getName(translator))
                                ))
                                .collect(Collectors.toList())
                        ),
                        Component.newline()
                );
            }

            if (!user.getAmmo().isEmpty()) {
                messageBuilder.append(translator.translate("command.info.ammo.header"),
                        Component.newline(),
                        Component.join(JoinConfiguration.newlines(),
                                user.getAmmo().entrySet().stream()
                                        .map(entry -> translator.translate(
                                                "command.info.ammo.entry",
                                                Placeholder.unparsed("gadget", entry.getKey().getName(translator)),
                                                Placeholder.unparsed("amount", String.valueOf(entry.getValue()))
                                        ))
                                        .collect(Collectors.toList())
                        ));
            }
            audience(sender).sendMessage(messageBuilder.build());
        });
    }
}
