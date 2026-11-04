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
package com.ricodev.lumen.compat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Listens to events by class name, so handlers for events added after 1.8 can live in the
 * same code and just don't get registered when the server doesn't have them.
 */
public final class Events {

    private static final Listener HOLDER = new Listener() {
    };

    private Events() {
    }

    public static boolean listen(Plugin plugin, String eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<Event> handler) {
        return listen(plugin, HOLDER, eventClass, priority, ignoreCancelled, handler);
    }

    /**
     * Same as the other one, but tied to {@code owner} so {@code HandlerList.unregisterAll(owner)} removes it too.
     */
    @SuppressWarnings("unchecked")
    public static boolean listen(Plugin plugin, Listener owner, String eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<Event> handler) {
        Class<? extends Event> type;

        try {
            type = (Class<? extends Event>) Class.forName(eventClass);
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
        Bukkit.getPluginManager().registerEvent(type, owner, priority, (listener, event) -> {
            if (type.isInstance(event)) {
                handler.accept(event);
            }
        }, plugin, ignoreCancelled);
        return true;
    }

    public static boolean listen(Plugin plugin, String eventClass, Consumer<Event> handler) {
        return listen(plugin, eventClass, EventPriority.NORMAL, true, handler);
    }

    /**
     * Listens to the first event class that exists, for events that moved packages between versions.
     */
    public static boolean listenFirst(Plugin plugin, Listener owner, Consumer<Event> handler, String... eventClasses) {
        for (String eventClass : eventClasses) {
            if (listen(plugin, owner, eventClass, EventPriority.NORMAL, false, handler)) {
                return true;
            }
        }
        return false;
    }

    public static void cancel(Event event) {
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }

    /**
     * Calls a no-argument getter on the event, for events that don't exist in the 1.8 API.
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T get(Object event, String getter) {
        try {
            return (T) event.getClass().getMethod(getter).invoke(event);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static @Nullable Entity entity(Object event, String getter) {
        Object value = get(event, getter);
        return value instanceof Entity ? (Entity) value : null;
    }
}
