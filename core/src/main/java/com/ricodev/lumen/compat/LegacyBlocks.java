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

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.api.util.structure.StructureBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns modern block data strings into something a pre-1.13 server can place: the closest material
 * that exists, plus the data value for the facing, half and open states.
 */
public final class LegacyBlocks {

    private static final Pattern STATE = Pattern.compile("([a-z_]+)=([a-z_0-9]+)");
    private static final Map<String, String> REPLACEMENTS = new LinkedHashMap<>();

    static {
        REPLACEMENTS.put("_CONCRETE_POWDER", "_WOOL");
        REPLACEMENTS.put("_CONCRETE", "_TERRACOTTA");
        REPLACEMENTS.put("POLISHED_BLACKSTONE_BUTTON", "STONE_BUTTON");
        REPLACEMENTS.put("BLACKSTONE_STAIRS", "NETHER_BRICK_STAIRS");
        REPLACEMENTS.put("BLACKSTONE", "COAL_BLOCK");
        REPLACEMENTS.put("BASALT", "COAL_BLOCK");
        REPLACEMENTS.put("CAMPFIRE", "NETHERRACK");
        REPLACEMENTS.put("MANGROVE_", "ACACIA_");
        REPLACEMENTS.put("STRIPPED_", "");
    }

    private LegacyBlocks() {
    }

    /**
     * The material to use for this block, falling back to the closest one the server has. Waterlogged
     * trapdoors turn into water, since that's what they're there for.
     */
    public static XMaterial material(StructureBlock block) {
        String name = block.getMaterial();

        if (name.endsWith("_TRAPDOOR") && "true".equals(states(block).get("waterlogged"))) {
            return XMaterial.WATER;
        }
        XMaterial material = supported(name);

        if (material != null) {
            return material;
        }
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            if (name.contains(entry.getKey())) {
                name = name.replace(entry.getKey(), entry.getValue());
                material = supported(name);

                if (material != null) {
                    return material;
                }
            }
        }
        if (name.endsWith("_WOOD")) {
            material = supported(name.replace("_WOOD", "_LOG"));
        } else if (name.endsWith("_WALL")) {
            material = XMaterial.COBBLESTONE_WALL;
        } else if (name.endsWith("_PRESSURE_PLATE")) {
            material = XMaterial.OAK_PRESSURE_PLATE;
        } else if (name.endsWith("_BUTTON")) {
            material = XMaterial.OAK_BUTTON;
        } else if (name.endsWith("_TRAPDOOR")) {
            material = XMaterial.OAK_TRAPDOOR;
        } else if (name.endsWith("_SLAB")) {
            material = XMaterial.OAK_SLAB;
        } else if (name.endsWith("_STAIRS")) {
            material = XMaterial.OAK_STAIRS;
        } else if (name.endsWith("_FENCE")) {
            material = XMaterial.OAK_FENCE;
        }
        return material != null && material.isSupported() ? material : XMaterial.STONE;
    }

    /**
     * Block data strings to try in order on 1.13+: the real one, then the closest material that
     * exists keeping the same states, then that material alone.
     */
    public static List<String> candidates(StructureBlock block) {
        List<String> candidates = new ArrayList<>();
        candidates.add(block.getData());
        XMaterial material = material(block);
        String name = "minecraft:" + material.name().toLowerCase(Locale.ROOT);
        String data = block.getData();
        int bracket = data.indexOf('[');

        if (bracket != -1) {
            candidates.add(name + data.substring(bracket));
        }
        candidates.add(name);
        return candidates;
    }

    /**
     * The legacy data value for the block, the base one of the material with the states added on top.
     */
    public static byte data(StructureBlock block, XMaterial material) {
        Map<String, String> states = states(block);
        String name = material.name();
        int data = material.getData();

        if (name.endsWith("_STAIRS")) {
            data = stairsFacing(states.get("facing")) | ("top".equals(states.get("half")) ? 4 : 0);
        } else if (name.equals("LADDER")) {
            data = ladderFacing(states.get("facing"));
        } else if (name.endsWith("_TRAPDOOR")) {
            data = trapdoorFacing(states.get("facing"))
                    | ("true".equals(states.get("open")) ? 4 : 0)
                    | ("top".equals(states.get("half")) ? 8 : 0);
        } else if (name.endsWith("_SLAB")) {
            if ("top".equals(states.get("type"))) {
                data |= 8;
            }
        } else if (name.endsWith("_BUTTON") || name.equals("LEVER")) {
            data = attachedFacing(states.get("face"), states.get("facing"));
        }
        return (byte) data;
    }

    private static Map<String, String> states(StructureBlock block) {
        Map<String, String> states = new LinkedHashMap<>();
        String data = block.getData();
        int bracket = data.indexOf('[');

        if (bracket != -1) {
            Matcher matcher = STATE.matcher(data.substring(bracket));

            while (matcher.find()) {
                states.put(matcher.group(1), matcher.group(2));
            }
        }
        return states;
    }

    private static int stairsFacing(String facing) {
        if (facing == null) {
            return 0;
        }
        switch (facing) {
            case "west":
                return 1;
            case "south":
                return 2;
            case "north":
                return 3;
            default:
                return 0;
        }
    }

    private static int ladderFacing(String facing) {
        if (facing == null) {
            return 2;
        }
        switch (facing) {
            case "south":
                return 3;
            case "west":
                return 4;
            case "east":
                return 5;
            default:
                return 2;
        }
    }

    private static int trapdoorFacing(String facing) {
        if (facing == null) {
            return 0;
        }
        switch (facing) {
            case "south":
                return 1;
            case "west":
                return 2;
            case "east":
                return 3;
            default:
                return 0;
        }
    }

    private static int attachedFacing(String face, String facing) {
        if ("floor".equals(face)) {
            return 5;
        }
        if ("ceiling".equals(face)) {
            return 0;
        }
        if (facing == null) {
            return 4;
        }
        switch (facing) {
            case "east":
                return 1;
            case "west":
                return 2;
            case "south":
                return 3;
            default:
                return 4;
        }
    }

    private static XMaterial supported(String name) {
        XMaterial material = XMaterial.matchXMaterial(name.toUpperCase(Locale.ROOT)).orElse(null);
        return material != null && material.isSupported() ? material : null;
    }
}
