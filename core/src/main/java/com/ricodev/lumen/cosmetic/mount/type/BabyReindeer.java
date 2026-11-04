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

import com.ricodev.lumen.util.entity.Horses;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.cosmetic.mount.BlockTrailBehavior;

import java.util.List;

public class BabyReindeer extends BlockTrailBehavior {

    private static final List<ItemStack> BLOCKS = Collects.listOf(XMaterial.SNOW_BLOCK.parseItem());

    private static final ItemStack SADDLE_ITEM = XMaterial.SADDLE.parseItem();

    private int ticks;

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.setupEntity(context, entity, nmsEntity);

        Horses.setup(entity, 1.0d, true, SADDLE_ITEM, false, null);
        Horses.setColor(entity, "BROWN", "NONE");
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.onUpdate(context, entity, nmsEntity);

        if (ticks % 5 == 0) {
            entity.getLocation(location).add(0.0d, 1.0d, 0.0d);
            Particles.spawn(location.getWorld(), LumenParticle.SNOWFLAKE, location, 3, 1.0d, 0.5d, 1.0d, 0.0d);
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
