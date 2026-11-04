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
package com.ricodev.lumen.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Provides static access to the Lumen API instance.
 * This class serves as a singleton provider for accessing the main plugin API
 * from external plugins or internal components.
 *
 * <p>Example usage:
 * <pre>{@code
 * Lumen api = LumenProvider.get();
 * UserManager userManager = api.getUserManager();
 * }</pre>
 */
public final class LumenProvider {

    private static Lumen plugin;

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws UnsupportedOperationException always, as this class cannot be instantiated
     */
    private LumenProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Gets the Lumen API instance.
     *
     * @return the Lumen instance
     * @throws IllegalStateException if Lumen has not been registered yet
     */
    public static Lumen get() {
        if (plugin == null) {
            throw new IllegalStateException("Lumen is not loaded yet!");
        }
        return plugin;
    }

    /**
     * Registers the active Lumen API instance.
     *
     * @param plugin the active {@link Lumen} instance
     */
    @ApiStatus.Internal
    public static void register(Lumen plugin) {
        LumenProvider.plugin = plugin;
    }

    /**
     * Unregisters the Lumen API instance.
     */
    @ApiStatus.Internal
    public static void unregister() {
        plugin = null;
    }
}
