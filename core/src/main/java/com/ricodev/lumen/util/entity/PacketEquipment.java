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
package com.ricodev.lumen.util.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.util.AbstractRunnable;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shows a fake helmet on a player without touching their real inventory. Other players get an
 * equipment packet, the owner gets the helmet slot of their own inventory.
 */
public class PacketEquipment extends AbstractRunnable {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();
    private static final double RANGE = 24.0d * 24.0d;
    private static final int HELMET_SLOT = 5;

    private final UUID uuid;
    private final int id;
    private final Set<Player> viewers = new HashSet<>();
    private ItemStack itemStack;

    public PacketEquipment(Player player, boolean tracker) {
        uuid = player.getUniqueId();
        id = player.getEntityId();

        if (tracker) {
            runTaskTimer(PLUGIN, 2L, 60L);
        }
    }

    @Override
    public void run() {
        viewers.removeIf(player -> !player.isOnline());
        Player holder = PLUGIN.getServer().getPlayer(uuid);

        if (holder == null) {
            return;
        }
        World world = holder.getWorld();
        Location holderLocation = holder.getLocation();

        for (Player player : world.getPlayers()) {
            if (player.isValid() && player.getLocation().distanceSquared(holderLocation) <= RANGE) {
                if (viewers.add(player)) {
                    sendUpdateToPlayer(player);
                }
            } else if (viewers.remove(player)) {
                sendRemoveUpdate(player, holder.getInventory().getHelmet());
            }
        }
    }

    public void sendUpdateToViewers() {
        for (Player player : viewers) {
            sendUpdateToPlayer(player);
        }
        Player holder = PLUGIN.getServer().getPlayer(uuid);

        if (holder != null && !viewers.contains(holder)) {
            sendUpdateToPlayer(holder);
        }
    }

    public void sendUpdateToPlayer(@Nullable Player player) {
        if (player == null || itemStack == null) {
            return;
        }
        send(player, itemStack);
    }

    public void sendRemoveUpdate(Player player, @Nullable ItemStack realHelmet) {
        send(player, realHelmet);
    }

    /**
     * Stops faking the helmet and shows everyone the real one again, or an empty head if there isn't one.
     */
    public void restore() {
        itemStack = null;
        Player holder = PLUGIN.getServer().getPlayer(uuid);

        if (holder == null) {
            return;
        }
        ItemStack realHelmet = holder.getInventory().getHelmet();

        for (Player player : viewers) {
            if (player.isOnline()) {
                send(player, realHelmet);
            }
        }
        viewers.clear();
        send(holder, realHelmet);
    }

    public void setItemStack(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    private void send(Player player, @Nullable ItemStack item) {
        com.github.retrooper.packetevents.protocol.item.ItemStack packetItem = item == null
                ? com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY
                : SpigotConversionUtil.fromBukkitItemStack(item);
        PacketWrapper<?> packet = player.getUniqueId().equals(uuid)
                ? new WrapperPlayServerSetSlot(0, 0, HELMET_SLOT, packetItem)
                : new WrapperPlayServerEntityEquipment(id, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, packetItem)));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
