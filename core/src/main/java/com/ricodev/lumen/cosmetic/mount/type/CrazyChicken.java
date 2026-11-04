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

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

public class CrazyChicken implements MountBehavior {

    private static final ItemStack ITEM_STACK = XMaterial.FEATHER.parseItem();
    private static final double ITEM_SPREAD = 0.5d;
    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);

    private int ticks;

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        nmsEntity.setRideSpeed(0.3f);
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (context.getPlayer().getVehicle() == entity) {
            nmsEntity.moveRide(context.getPlayer());
        }

        if (ticks % 5 == 0) {
            if (isTossItemsEnabled(context)) {
                entity.getLocation(location);

                CosmeticEntity itemEntity = CosmeticEntities.create(location.getWorld(), Entities.itemType());
                itemEntity.setPositionRotation(location);
                itemEntity.setEntityItemStack(ITEM_STACK);
                itemEntity.setVelocity(
                        MathUtil.randomRange(-ITEM_SPREAD, ITEM_SPREAD),
                        MathUtil.randomRange(-0.1d, ITEM_SPREAD),
                        MathUtil.randomRange(-ITEM_SPREAD, ITEM_SPREAD)
                );
                itemEntity.getTracker().startTracking();
                itemEntity.getTracker().destroyAfter(30);
            }
            Particles.spawn(location.getWorld(), LumenParticle.ENTITY_EFFECT, location.add(
                            MathUtil.randomRange(-0.5d, 1.0d),
                            MathUtil.randomRange(-0.2d, 1.0d),
                            MathUtil.randomRange(-0.5d, 1.0d)
                    ), 5, Color.fromBGR(MathUtil.randomRangeInt(0, 255),
                            MathUtil.randomRangeInt(0, 255),
                            MathUtil.randomRangeInt(0, 255))
            );
        }

        if (++ticks > 360) {
            ticks = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
    }
}
