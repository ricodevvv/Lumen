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

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.compat.Compats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.command.SubCommand;

public class ItemCommand extends SubCommand<CommandSender> {

    public ItemCommand(LumenPlugin plugin) {
        super(plugin, "lumen.command.item", false);
        addFlat("item");
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack itemStack = Compats.get().getItemInHand(player);

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            audience(player).sendMessage(Component.text("You are not holding in any item in your main hand!", NamedTextColor.RED));
            return;
        }
        String input = "minecraft:" + XMaterial.matchXMaterial(itemStack).name().toLowerCase(java.util.Locale.ROOT);

        audience(player).sendMessage(Component.text().append(
                Component.text("Selected item:", NamedTextColor.GREEN),
                Component.newline(),
                Component.text(input)
                        .clickEvent(ClickEvent.copyToClipboard(input))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy!", NamedTextColor.YELLOW)))
        ));
    }
}
