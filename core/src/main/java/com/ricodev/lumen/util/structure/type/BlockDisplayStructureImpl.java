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

import com.ricodev.lumen.util.entity.CosmeticEntities;

import org.bukkit.Location;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.entity.EntityTracker;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.api.util.structure.type.BlockDisplayStructure;
import com.ricodev.lumen.util.entity.EntityTrackerImpl;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.structure.StructureImpl;

import java.util.Map;

public class BlockDisplayStructureImpl extends StructureImpl<CosmeticEntity> implements BlockDisplayStructure {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();
    private final EntityTracker tracker = new EntityTrackerImpl();

    public BlockDisplayStructureImpl(StructureData data) {
        super(data, block -> !block.getType().isSolid() && !block.isLiquid());
    }

    @Override
    public double spawn(Location location) {
        double angle = calculateAngle(location);

        for (Map.Entry<Vector, StructureBlock> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);
            StructureBlock blockData = entry.getValue().rotated(location.getYaw());
            CosmeticEntity nmsFallingBlock = CosmeticEntities.createItemHolder(location.getWorld(), Compats.get().blockItem(blockData), tracker);
            location.add(vector);
            Location clone = location.clone();
            clone.setYaw(0.0f);
            clone.setPitch(0.0f);
            nmsFallingBlock.setPositionRotation(clone);
            location.subtract(vector);

            placedEntries.add(nmsFallingBlock);
        }
        tracker.startTracking();
        return angle;
    }

    @Override
    public void remove() {
        tracker.destroy();
        placedEntries.clear();
    }
}
