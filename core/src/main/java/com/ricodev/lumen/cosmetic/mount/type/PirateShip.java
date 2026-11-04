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
package com.ricodev.lumen.cosmetic.mount.type;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.Entities;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class PirateShip implements MountBehavior, Listener {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private TNTPrimed tnt;
    private Entity ship;

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (tnt != null) {
            tnt.remove();
            tnt = null;
        }
        Compats.get().setGravity(entity, false);
        entity.setPassenger(context.getPlayer());
        ship = entity;
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (player.getVehicle() == entity) {
            entity.setVelocity(player.getLocation(location).getDirection().multiply(context.getType().getMovementSpeed()));
        }
        if (tnt != null) {
            tnt.getLocation(location).add(0.0d, 0.7d, 0.0d);
            Particles.spawn(tnt.getWorld(), LumenParticle.LARGE_SMOKE,
                    location,
                    1,
                    0.2d,
                    0.0d,
                    0.2d,
                    0.01d
            );
        }
        entity.getLocation(location).add(0.0d, 0.4d, 0.0d);

        Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location, 0);
        Particles.spawn(location.getWorld(), LumenParticle.DRIPPING_WATER,
                location,
                2,
                0.4d,
                0.3d,
                0.4d,
                0.01d
        );
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        context.getUser().setFallDamageProtection(10);

        if (tnt != null) {
            tnt.remove();
            tnt = null;
        }
        ship = null;
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && tnt == null && player.getVehicle() == ship) {
            player.getLocation(location).add(0.0d, 1.8d, 0.0d);

            tnt = Entities.spawn(location, TNTPrimed.class, entity -> {
                entity.setFuseTicks(80);
                entity.setYield(0.0f);
                MetadataUtil.setCustomEntity(entity);
            });
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity == tnt) {
            entity.getLocation(location);

            Sounds.playAt(location, XSound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.0f);
            Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION_EMITTER, location, 0);

            for (Player player : MathUtil.getClosestPlayersFromLocation(location, 4.0d)) {
                MathUtil.pushEntity(player, location, MathUtil.randomRange(1.0d, 3.0d), MathUtil.randomRange(1.0d, 2.0d));
                User otherUser = PLUGIN.getUserManager().getConnected(player);

                if (otherUser != null) {
                    otherUser.setFallDamageProtection(5);
                }
            }
            event.setCancelled(true);
            tnt = null;
        }
    }
}
