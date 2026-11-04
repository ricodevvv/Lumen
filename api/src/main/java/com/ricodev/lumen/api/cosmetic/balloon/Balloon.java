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
package com.ricodev.lumen.api.cosmetic.balloon;

import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.Cosmetic;
import org.bukkit.entity.Entity;

/**
 * Represents a balloon cosmetic instance associated with a user.
 * Balloons are floating entities that follow the player, connected
 * by a leash, providing a decorative companion effect.
 */
public interface Balloon extends Cosmetic<BalloonType, BalloonBehavior> {

    /**
     * What floats: the mob itself, or the armor stand wearing the balloon item.
     */
    Entity getEntity();

    /**
     * Gets the NMS entity used as the leash anchor point for non-living-entities.
     *
     * @return the leash entity, or null if not used
     */
    @Nullable
        /**
     * The invisible rabbit that holds the leash, or null when the floating mob holds it itself.
     */
    Entity getLeashEntity();
}
