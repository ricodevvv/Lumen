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
package com.ricodev.lumen.api.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A group of cosmetic entities that spawn and go away together.
 */
public interface EntityTracker {

    void addEntity(CosmeticEntity entity);

    void removeEntity(CosmeticEntity entity);

    Collection<CosmeticEntity> getEntities();

    void startTracking();

    boolean isTracking();

    /**
     * Removes every entity in the group.
     */
    void destroy();

    void destroyAfter(int ticks);

    void setOwner(@Nullable Player owner);

    @Nullable
    Player getOwner();

    /**
     * Hides the group from that player, used when someone turns off seeing their own cosmetic.
     */
    void addAntiViewer(Player player);

    void removeAntiViewer(Player player);

    boolean isAntiViewer(Player player);

    void respawnAt(Location location);
}
