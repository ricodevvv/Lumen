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
package com.ricodev.lumen.api.util.structure;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One block of a structure, kept as the modern block data string like
 * {@code minecraft:oak_stairs[facing=east,half=bottom]}. 1.13+ places it with every state,
 * older servers only use the material.
 */
public final class StructureBlock {

    private static final String[] FACES = {"north", "east", "south", "west"};
    private static final Pattern FACING = Pattern.compile("facing=(north|east|south|west)");

    private final String data;

    public StructureBlock(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    /**
     * The material name without namespace or states, like {@code OAK_STAIRS}.
     */
    public String getMaterial() {
        String name = data;
        int bracket = name.indexOf('[');

        if (bracket != -1) {
            name = name.substring(0, bracket);
        }
        if (name.contains(":")) {
            name = name.substring(name.indexOf(':') + 1);
        }
        return name.toUpperCase(Locale.ROOT);
    }

    /**
     * Turns horizontal facings by the given yaw, snapped to 90 degree steps.
     */
    public StructureBlock rotated(float yaw) {
        int turns = ((Math.round(yaw / 90.0f) % 4) + 4) % 4;

        if (turns == 0 || data.indexOf('[') == -1) {
            return this;
        }
        String states = data.substring(data.indexOf('['));
        String prefix = data.substring(0, data.indexOf('['));
        Matcher matcher = FACING.matcher(states);
        StringBuffer rotated = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(rotated, "facing=" + turn(matcher.group(1), turns));
        }
        matcher.appendTail(rotated);
        String result = rotated.toString();

        boolean[] sides = new boolean[4];
        boolean multipleFacing = false;

        for (int i = 0; i < FACES.length; i++) {
            Matcher side = Pattern.compile("(?<![a-z_])" + FACES[i] + "=(true|false)").matcher(result);

            if (side.find()) {
                multipleFacing = true;
                sides[i] = Boolean.parseBoolean(side.group(1));
            }
        }
        if (multipleFacing) {
            for (int i = 0; i < FACES.length; i++) {
                result = result.replaceAll("(?<![a-z_])" + FACES[i] + "=(true|false)", FACES[i] + "=#" + i);
            }
            for (int i = 0; i < FACES.length; i++) {
                result = result.replace(FACES[i] + "=#" + i, FACES[i] + "=" + sides[(i - turns + 4) % 4]);
            }
        }
        return new StructureBlock(prefix + result);
    }

    private static String turn(String face, int turns) {
        for (int i = 0; i < FACES.length; i++) {
            if (FACES[i].equals(face)) {
                return FACES[(i + turns) % 4];
            }
        }
        return face;
    }

    @Override
    public String toString() {
        return data;
    }
}
