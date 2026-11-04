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
package com.ricodev.lumen.util.entity;

import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.entity.EntityTracker;
import com.ricodev.lumen.compat.Compats;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityTrackerImpl implements EntityTracker {

    private final List<CosmeticEntity> entities = new ArrayList<>();
    private final Set<UUID> antiViewers = new HashSet<>();
    private Player owner;
    private boolean tracking;

    @Override
    public void addEntity(CosmeticEntity entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
        }
    }

    @Override
    public void removeEntity(CosmeticEntity entity) {
        entities.remove(entity);
    }

    @Override
    public Collection<CosmeticEntity> getEntities() {
        return new ArrayList<>(entities);
    }

    @Override
    public void startTracking() {
        tracking = true;

        for (CosmeticEntity entity : getEntities()) {
            ((CosmeticEntityImpl) entity).spawnIfNeeded();
        }
    }

    @Override
    public boolean isTracking() {
        return tracking;
    }

    @Override
    public void destroy() {
        tracking = false;

        for (CosmeticEntity entity : getEntities()) {
            ((CosmeticEntityImpl) entity).despawn();
        }
        entities.clear();
    }

    /**
     * Despawns everything but keeps the group, so {@link #startTracking()} brings it back.
     */
    public void stopTracking() {
        tracking = false;

        for (CosmeticEntity entity : getEntities()) {
            ((CosmeticEntityImpl) entity).despawn();
        }
    }

    @Override
    public void destroyAfter(int ticks) {
        LumenPlugin plugin = LumenPlugin.getPlugin();
        plugin.getServer().getScheduler().runTaskLater(plugin, this::destroy, ticks);
    }

    @Override
    public void setOwner(@Nullable Player owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable Player getOwner() {
        return owner;
    }

    @Override
    public void addAntiViewer(Player player) {
        antiViewers.add(player.getUniqueId());

        for (CosmeticEntity entity : entities) {
            if (entity.getBukkitEntity() != null) {
                Compats.get().hideEntity(LumenPlugin.getPlugin(), player, entity.getBukkitEntity());
            }
        }
    }

    @Override
    public void removeAntiViewer(Player player) {
        antiViewers.remove(player.getUniqueId());

        for (CosmeticEntity entity : getEntities()) {
            ((CosmeticEntityImpl) entity).respawn();
        }
    }

    @Override
    public boolean isAntiViewer(Player player) {
        return antiViewers.contains(player.getUniqueId());
    }

    Set<UUID> getAntiViewers() {
        return antiViewers;
    }

    @Override
    public void respawnAt(Location location) {
        for (CosmeticEntity entity : getEntities()) {
            ((CosmeticEntityImpl) entity).despawn();
            entity.setPositionRotation(location);
        }
    }
}
