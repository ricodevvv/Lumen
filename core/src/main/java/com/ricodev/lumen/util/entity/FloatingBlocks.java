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
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.compat.ServerVersion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Entry point for {@link FloatingBlock}s. Picks block displays when the server has them and falling
 * blocks otherwise, and keeps track of everything so the plugin can clean up when it shuts down.
 */
public final class FloatingBlocks {

    private static final Set<FloatingBlock> BLOCKS = ConcurrentHashMap.newKeySet();
    private static final Set<Entity> SEATS = ConcurrentHashMap.newKeySet();
    private static FloatingBlockFactory factory;

    private FloatingBlocks() {
    }

    private static FloatingBlockFactory factory() {
        if (factory == null) {
            factory = createFactory();
        }
        return factory;
    }

    private static FloatingBlockFactory createFactory() {
        if (ServerVersion.atLeast(19, 4)) {
            try {
                return (FloatingBlockFactory) Class.forName("com.ricodev.lumen.display.DisplayBlocks").getConstructor().newInstance();
            } catch (ReflectiveOperationException | LinkageError e) {
                LumenPlugin.getPlugin().getLogger().log(Level.WARNING, "Couldn't load display entities, using falling blocks", e);
            }
        }
        return new LegacyFloatingBlocks();
    }

    public static FloatingBlock block(Location location, StructureBlock block) {
        return block(location, block, 1.0f, null);
    }

    public static FloatingBlock block(Location location, StructureBlock block, float scale, @Nullable Entity parent) {
        return track(factory().block(location, block, scale, parent));
    }

    public static FloatingBlock cube(Location location, StructureBlock block, float scale) {
        return track(factory().cube(location, block, scale));
    }

    public static FloatingBlock item(Location location, ItemStack item, float scale) {
        return track(factory().item(location, item, scale));
    }

    public static Entity seat(Location location) {
        Entity seat = factory().seat(location);
        SEATS.add(seat);
        return seat;
    }

    /**
     * How big a block of the given scale really ends up, since falling blocks are always full size.
     */
    public static float size(float scale) {
        return factory().canScale() ? scale : 1.0f;
    }

    public static void remove(@Nullable FloatingBlock block) {
        if (block != null) {
            BLOCKS.remove(block);
            block.remove();
        }
    }

    public static void removeSeat(@Nullable Entity seat) {
        if (seat != null) {
            SEATS.remove(seat);
            seat.eject();
            seat.remove();
        }
    }

    /**
     * Removes everything that's still around, for when the plugin shuts down.
     */
    public static void removeAll() {
        for (FloatingBlock block : new ArrayList<>(BLOCKS)) {
            block.remove();
        }
        for (Entity seat : new ArrayList<>(SEATS)) {
            seat.remove();
        }
        BLOCKS.clear();
        SEATS.clear();
    }

    private static FloatingBlock track(FloatingBlock block) {
        BLOCKS.add(block);
        return block;
    }
}
