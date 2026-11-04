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
package com.ricodev.lumen.util.structure;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.util.structure.Structure;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class StructureImpl<T> implements Structure<T> {

    protected final StructureData data;
    protected final List<T> placedEntries = new ArrayList<>();
    protected Predicate<Block> blockCheckPredicate;

    public StructureImpl(StructureData data, Predicate<Block> blockCheckPredicate) {
        this.data = data;
        this.blockCheckPredicate = blockCheckPredicate;
    }

    @Override
    public boolean isEnoughSpace(Location location) {
        double angle = calculateAngle(location);

        for (Map.Entry<Vector, StructureBlock> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);

            location.add(vector);
            Block block = location.getBlock();
            location.subtract(vector);

            if (!blockCheckPredicate.test(block)) {
                return false;
            }
        }
        return true;
    }

    protected double calculateAngle(Location location) {
        LocationUtil.center(location);

        float yaw = 90.0f * (Math.round(location.getYaw() / 90.0f) & 0x3);
        location.setYaw(yaw);
        location.setPitch(0.0f);

        return -FastMathUtil.toRadians(location.getYaw());
    }



    @Override
    public StructureData getData() {
        return data;
    }

    @Override
    public List<T> getPlacedEntries() {
        return placedEntries;
    }
}
