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
package com.ricodev.lumen.cosmetic.mount;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

import java.util.List;

public abstract class BlockTrailBehavior implements MountBehavior {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    protected final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private boolean blockTrailEnabled;

    public BlockTrailBehavior() {
    }

    public abstract int getBlockTrailRadius();

    public abstract List<ItemStack> getTrailBlocks();

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
        blockTrailEnabled = context.getType().getCategory().getConfig().getBoolean("block_trail");
        context.getPlayer().getLocation(location);
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (blockTrailEnabled
                && context.getPlayer().getVehicle() == entity
                && context.getUser().isMoving()
                && entity.isOnGround()) {
            for (Block block : MathUtil.getIn3DRadius(entity.getLocation(location).subtract(0.0d, 1.0d, 0.0d), getBlockTrailRadius())) {
                ItemStack itemStack = MathUtil.getRandomElement(getTrailBlocks());

                if (itemStack != null) {
                    PLUGIN.getFakeBlockManager().setFakeBlock(block, itemStack, false, 40);
                }
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
    }
}
