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
package com.ricodev.lumen.cosmetic.gadget.type;

import org.bukkit.event.Event;

import com.ricodev.lumen.compat.Events;

import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MetadataUtil;

import java.util.UUID;

public class Cowboy implements GadgetBehavior, Listener {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private Entity armorStand;
    private UUID lastClicked;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        Events.listenFirst(context.getPlugin().getJavaPlugin(), this, this::onDismount,
                "org.bukkit.event.entity.EntityDismountEvent", "org.spigotmc.event.entity.EntityDismountEvent");
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        Player clickedPlayer = null;

        if (lastClicked != null) {
            clickedPlayer = context.getPlugin().getJavaPlugin().getServer().getPlayer(lastClicked);
        }

        if (clickedPlayer == null) {
            context.getUser().sendMessage(context.getUser().translate("cosmetic.gadgets.click_player"));
            Sounds.playFor(player, XSound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            return InteractionResult.fail();
        }
        Location location = clickedPlayer.getLocation().add(0.0d, 2.0d, 0.0d);
        armorStand = Entities.spawn(clickedPlayer.getLocation(), ArmorStand.class, entity -> {
            entity.setSmall(true);
            entity.setVisible(false);
            entity.setGravity(false);
            Compats.get().setCollidable(entity, false);

            MetadataUtil.setCustomEntity(entity);
        });
        Compats.get().addPassenger(clickedPlayer, armorStand);
        Compats.get().addPassenger(armorStand, player);
        Sounds.playFor(player, location, XSound.ENTITY_HORSE_ARMOR, 0.5f, 1.2f);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        if (armorStand != null) {
            despawnArmorStand();
        }
    }

    @Override
    public boolean requiresGroundOnUse() {
        return false;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return true;
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return false;
    }

    @EventHandler
    public void onClickedPlayer(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = (Player) event.getRightClicked();
            lastClicked = player.getUniqueId();
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && armorStand != null) {
            Player player = event.getPlayer();
            Location location = player.getLocation();

            if (Compats.get().getPassengers(player).contains(armorStand)) {
                for (Entity passenger : Compats.get().getPassengers(armorStand)) {
                    passenger.eject();
                    despawnArmorStand();

                    passenger.setVelocity(location.getDirection().multiply(2.0d));

                    if (passenger instanceof Player) {
                        Player passengerPlayer = (Player) passenger;
                        User user = PLUGIN.getUserManager().getConnected(passengerPlayer);

                        if (user != null) {
                            user.setFallDamageProtection(8);
                        }
                    }
                    Sounds.playAt(player, XSound.ENTITY_WITHER_SHOOT, 0.4f, 1.5f);
                    break;
                }
            }
        }
    }

    private void onDismount(Event event) {
        Entity dismounted = Events.entity(event, "getDismounted");

        if (dismounted != null && dismounted == armorStand) {
            despawnArmorStand();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (armorStand != null && Compats.get().getPassengers(armorStand).contains(event.getPlayer())) {
            despawnArmorStand();
        }
    }

    private void despawnArmorStand() {
        armorStand.remove();
        armorStand = null;
    }
}
