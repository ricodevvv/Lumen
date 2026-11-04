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

import org.bukkit.Location;
import org.bukkit.block.Block;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.api.util.structure.type.FakeBlockStructure;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.structure.StructureImpl;

import java.util.Map;

public class FakeBlockStructureImpl extends StructureImpl<Block> implements FakeBlockStructure {

    private static final double RANGE = 64.0d * 64.0d;

    public FakeBlockStructureImpl(StructureData data) {
        super(data, block -> Materials.isAir(block.getType()));
    }

    @Override
    public double spawn(Location location) {
        double angle = calculateAngle(location);

        for (Map.Entry<Vector, StructureBlock> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);
            StructureBlock blockData = entry.getValue().rotated(location.getYaw());

            location.add(vector);
            Block block = location.getBlock();
            location.subtract(vector);

            for (Player player : block.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(location) < RANGE) {
                    Compats.get().sendBlockChange(player, location, blockData);
                }
            }
            MetadataUtil.setCustomBlock(block);
            placedEntries.add(block);
        }
        return angle;
    }

    @Override
    public void remove() {
        for (Block block : placedEntries) {
            Location location = block.getLocation();
            for (Player player : block.getWorld().getPlayers()) {
                Compats.get().resetBlockChange(player, block);
            }
            MetadataUtil.removeCustomBlock(block);
        }
        placedEntries.clear();
    }
}
