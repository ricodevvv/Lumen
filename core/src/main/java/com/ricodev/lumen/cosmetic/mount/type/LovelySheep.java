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

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.cosmetic.mount.BlockTrailBehavior;

import java.util.List;

public class LovelySheep extends BlockTrailBehavior {

    private static final List<ItemStack> BLOCKS = Collects.listOf(
            XMaterial.PINK_TERRACOTTA.parseItem(),
            XMaterial.RED_TERRACOTTA.parseItem(),
            XMaterial.MAGENTA_TERRACOTTA.parseItem()
    );

    private int ticks;

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.setupEntity(context, entity, nmsEntity);

        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep) entity;
            sheep.setColor(DyeColor.RED);
        }
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.onUpdate(context, entity, nmsEntity);

        if (context.getPlayer().getVehicle() == entity) {
            nmsEntity.moveRide(context.getPlayer());
        }

        if (ticks % 10 == 0) {
            entity.getLocation(location);

            for (int i = 0; i < 3; i++) {
                Particles.spawn(location.getWorld(), LumenParticle.HEART, location, 1, 0.5d, 1.0d, 0.5d, 0.3d);
                Particles.spawn(location.getWorld(), LumenParticle.FIREWORK, location, 1, 0.8d, 2.0d, 0.8d, 0.2d);
            }
            if (entity instanceof Sheep) {
                Sheep sheep = (Sheep) entity;
                if (sheep.getColor() == DyeColor.RED) {
                    sheep.setColor(DyeColor.PURPLE);
                } else {
                    sheep.setColor(DyeColor.RED);
                }
            }
        }

        if (++ticks > 360) {
            ticks = 0;
        }
    }

    @Override
    public int getBlockTrailRadius() {
        return 1;
    }

    @Override
    public List<ItemStack> getTrailBlocks() {
        return BLOCKS;
    }
}
