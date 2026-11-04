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
package com.ricodev.lumen.util.structure.type;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.material.Materials;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.api.util.structure.type.BlockStructure;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.structure.StructureImpl;

import java.util.Map;

public class BlockStructureImpl extends StructureImpl<Block> implements BlockStructure {

    public BlockStructureImpl(StructureData data) {
        super(data, block -> Materials.isAir(block.getType()));
    }

    @Override
    public double spawn(Location location) {
        return spawn(location, false);
    }

    public double spawn(Location location, boolean blockBreakEffect) {
        double angle = calculateAngle(location);

        for (Map.Entry<Vector, StructureBlock> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);
            StructureBlock blockData = entry.getValue().rotated(location.getYaw());

            location.add(vector);
            Block block = location.getBlock();
            Compats.get().setBlock(block, blockData);

            if (blockBreakEffect) {
                location.getWorld().playEffect(location, Effect.STEP_SOUND, block.getType());
            }
            location.subtract(vector);
            MetadataUtil.setCustomBlock(block);
            placedEntries.add(block);
        }
        return angle;
    }

    @Override
    public void remove() {
        for (Block block : placedEntries) {
            block.setType(Material.AIR);
            MetadataUtil.removeCustomBlock(block);
        }
        placedEntries.clear();
    }
}
