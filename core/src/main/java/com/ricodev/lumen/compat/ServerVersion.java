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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The server's Minecraft version, flattened so 1.8.8 reads as 8.8 and 26.3 reads as 26.3.
 * That way 26.x still sorts above 1.21.x after the version scheme change.
 */
public final class ServerVersion {

    private static final int FEATURE;
    private static final int PATCH;

    static {
        Matcher matcher = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(Bukkit.getBukkitVersion());
        int feature = 8;
        int patch = 0;

        if (matcher.find()) {
            int first = Integer.parseInt(matcher.group(1));
            int second = Integer.parseInt(matcher.group(2));
            int third = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));

            if (first == 1) {
                feature = second;
                patch = third;
            } else {
                feature = first;
                patch = second;
            }
        }
        FEATURE = feature;
        PATCH = patch;
    }

    private ServerVersion() {
    }

    public static int feature() {
        return FEATURE;
    }

    public static int patch() {
        return PATCH;
    }

    /**
     * True when the server runs at least that version, e.g. {@code atLeast(13)} for 1.13+.
     */
    public static boolean atLeast(int feature) {
        return FEATURE >= feature;
    }

    public static boolean atLeast(int feature, int patch) {
        return FEATURE > feature || FEATURE == feature && PATCH >= patch;
    }

    public static boolean isLegacy() {
        return FEATURE < 13;
    }

    public static String describe() {
        return FEATURE >= 26 ? FEATURE + "." + PATCH : "1." + FEATURE + (PATCH > 0 ? "." + PATCH : "");
    }
}
