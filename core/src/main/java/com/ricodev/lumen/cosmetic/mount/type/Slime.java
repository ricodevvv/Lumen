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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

public class Slime implements MountBehavior {

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private int ticks;

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
        context.getPlayer().getLocation(location);
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (entity instanceof org.bukkit.entity.Slime) {
            org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) entity;
            slime.setSize(3);
        }
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (player.getVehicle() == entity) {
            nmsEntity.moveRide(player);
        }

        if (context.getUser().isMoving() && player.getVehicle() == entity && entity.isOnGround()) {
            if (ticks % 30 == 0) {
                Vector vector = entity.getVelocity();
                vector.setY(vector.getY() + 0.8d);
                entity.setVelocity(vector);
            }

            if (ticks % 20 == 0) {
                for (Player hitPlayer : MathUtil.getClosestPlayersFromLocation(entity.getLocation(location), 2.5d)) {
                    if (hitPlayer != player && hitPlayer.isValid() && hitPlayer.isOnGround()) {
                        Vector vector = hitPlayer.getVelocity();
                        vector.setY(vector.getY() + 0.6d);
                        hitPlayer.setVelocity(vector);

                        Sounds.playAt(entity.getLocation(), XSound.ENTITY_SLIME_HURT, 0.5f, 1.0f);
                        Particles.spawn(hitPlayer.getWorld(), LumenParticle.ITEM_SLIME, hitPlayer.getLocation(), 30, 0.0d, 0.0d, 0.0d, 0.0d);
                    }
                }
            }
        }

        if (ticks % 10 == 0) {
            entity.getLocation(location);
            Particles.spawn(location.getWorld(), LumenParticle.HAPPY_VILLAGER, location, 5, 1.0d, 1.2d, 1.0d, 0.1d);
        }

        if (++ticks > 360) {
            ticks = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
    }
}
