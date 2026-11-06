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

import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.api.util.structure.type.ParentBlockDisplayStructure;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.entity.FloatingBlock;
import com.ricodev.lumen.util.entity.FloatingBlocks;
import com.ricodev.lumen.util.structure.StructureImpl;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A structure that moves along with a parent entity, like the rocket and the hot air balloon. On
 * display entities the blocks ride the parent, on falling blocks {@link #follow()} has to be called
 * every time the parent moves.
 */
public class ParentBlockDisplayStructureImpl extends StructureImpl<Entity> implements ParentBlockDisplayStructure {

    private final List<FloatingBlock> blocks = new ArrayList<>();

    public ParentBlockDisplayStructureImpl(StructureData data) {
        super(data, block -> !block.getType().isSolid() && !block.isLiquid());
    }

    @Override
    public double spawn(Location location, Entity parent, float heightOffset) {
        double angle = calculateAngle(location);
        Location base = parent.getLocation().subtract(0.0d, heightOffset, 0.0d);

        for (Map.Entry<Vector, StructureBlock> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);
            StructureBlock blockData = entry.getValue().rotated(location.getYaw());
            FloatingBlock block = FloatingBlocks.block(base.clone().add(vector), blockData, 1.0f, parent);

            blocks.add(block);

            if (block.getEntity() != null) {
                placedEntries.add(block.getEntity());
            }
        }
        return angle;
    }

    @Override
    public double spawn(Location location) {
        return 0;
    }

    /**
     * Moves every block back next to the parent.
     */
    public void follow() {
        for (FloatingBlock block : blocks) {
            block.follow();
        }
    }

    @Override
    public void remove() {
        for (FloatingBlock block : blocks) {
            FloatingBlocks.remove(block);
        }
        blocks.clear();
        placedEntries.clear();
    }
}
