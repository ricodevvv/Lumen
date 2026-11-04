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
package com.ricodev.lumen.api.cosmetic.status;

import com.ricodev.lumen.api.cosmetic.Cosmetic;
import org.bukkit.entity.Entity;

/**
 * Represents a status cosmetic instance associated with a user.
 * Status cosmetics display dynamic text above the player,
 * such as custom titles, statistics, or other information that can update in real-time.
 */
public interface Status extends Cosmetic<StatusType, StatusBehavior> {

    /**
     * The armor stand carrying the text, or null while it isn't spawned.
     */
    Entity getEntity();
}
