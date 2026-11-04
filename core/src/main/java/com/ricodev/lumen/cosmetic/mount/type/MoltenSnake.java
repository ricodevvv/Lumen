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

import com.cryptomorin.xseries.XMaterial;

import org.bukkit.inventory.ItemStack;

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.entity.EntityTrackerImpl;

public class MoltenSnake implements MountBehavior {

    private static final int TAILS = 20;
    private static final ItemStack BLOCK_ITEM = XMaterial.NETHERRACK.parseItem();

    private final EntityTrackerImpl tracker = new EntityTrackerImpl();
    private final Location reusableEntityLocation = new Location(null, 0.0d, 0.0d, 0.0d);

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (entity instanceof MagmaCube) {
            MagmaCube cube = (MagmaCube) entity;
            cube.setSize(2);
        }
        Location location = context.getPlayer().getLocation();
        Vector vector = location.getDirection().multiply(-0.5d);

        for (int i = 0; i < TAILS; i++) {
            CosmeticEntity tailEntity = CosmeticEntities.createItemHolder(location.getWorld(), BLOCK_ITEM, tracker);
            tailEntity.setPositionRotation(location.add(vector).clone().add(0.0d, -0.4d, 0.0d));
        }
        tracker.startTracking();
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        entity.getLocation(reusableEntityLocation);
        Player player = context.getPlayer();

        if (player.getVehicle() == entity) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Compats.get().setAI(livingEntity, true);
            }
            Location location = player.getLocation();
            nmsEntity.setYaw(location.getYaw());
            entity.setVelocity(location.getDirection().multiply(context.getType().getMovementSpeed()));
        }
        Location location = reusableEntityLocation;

        for (CosmeticEntity tailEntity : tracker.getEntities()) {
            Location temp = tailEntity.getPreviousLocation().clone();

            if (temp != reusableEntityLocation) {
                tailEntity.sendPositionRotationPacket(location.clone().add(0.0d, -0.4d, 0.0d));
                temp.add(0.0d, 0.4d, 0.0d);
                location = temp;
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        context.getUser().setFallDamageProtection(10);
        tracker.destroy();
    }
}
