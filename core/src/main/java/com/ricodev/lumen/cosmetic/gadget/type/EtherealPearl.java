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

import org.bukkit.entity.Entity;

import org.bukkit.event.Event;

import com.ricodev.lumen.compat.Events;

import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class EtherealPearl implements GadgetBehavior, Listener {

    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
            .trail(true)
            .flicker(false)
            .withColor(Color.PURPLE)
            .withFade(Color.AQUA)
            .with(Type.BALL_LARGE)
            .build();

    private Player player;
    private EnderPearl enderPearl;
    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        player = context.getPlayer();
        Events.listenFirst(context.getPlugin().getJavaPlugin(), this, this::onDismount,
                "org.bukkit.event.entity.EntityDismountEvent", "org.spigotmc.event.entity.EntityDismountEvent");
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        location = player.getEyeLocation();

        enderPearl = Entities.spawn(location, EnderPearl.class, entity -> {
            entity.setVelocity(location.getDirection().multiply(2.0d));
            MetadataUtil.setCustomEntity(entity);
        });
        Compats.get().addPassenger(enderPearl, player);
        context.getUser().setFallDamageProtection(15);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (enderPearl != null && enderPearl.isValid()) {
            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, enderPearl.getLocation(location), 1, 0, 0, 0, 0.0d);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        despawn();
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
    public void onPearlHit(ProjectileHitEvent event) {
        if (event.getEntity() == enderPearl) {
            enderPearl.getLocation(location);
            lunchFirework(location);

            LocationUtil.center(location);
            location.setY(location.getBlockY() + 1.5d);

            Location playerLocation = player.getLocation();
            location.setPitch(playerLocation.getPitch());
            location.setYaw(playerLocation.getYaw());
            player.teleport(location);

            Events.cancel(event);
        }
    }

    private void onDismount(Event event) {
        Entity dismounted = Events.entity(event, "getDismounted");
        Entity rider = Events.entity(event, "getEntity");

        if (dismounted != null && dismounted == enderPearl && rider != null) {
            lunchFirework(rider.getLocation(location));
            despawn();
        }
    }

    private void despawn() {
        if (enderPearl != null) {
            enderPearl.remove();
            enderPearl = null;
        }
    }

    private void lunchFirework(Location location) {
        Entities.spawn(location, Firework.class, entity -> {
            FireworkMeta meta = entity.getFireworkMeta();
            meta.addEffect(FIREWORK_EFFECT);
            entity.setFireworkMeta(meta);

            MetadataUtil.setCustomEntity(entity);
        }).detonate();
    }
}
