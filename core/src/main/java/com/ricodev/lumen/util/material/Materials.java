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
package com.ricodev.lumen.util.material;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.util.MathUtil;

import java.util.Arrays;
import java.util.List;

public class Materials {

    public static final List<XMaterial> TERRACOTTA = Arrays.asList(
            XMaterial.BLACK_TERRACOTTA,
            XMaterial.BLUE_TERRACOTTA,
            XMaterial.BROWN_TERRACOTTA,
            XMaterial.CYAN_TERRACOTTA,
            XMaterial.GRAY_TERRACOTTA,
            XMaterial.GREEN_TERRACOTTA,
            XMaterial.LIGHT_BLUE_TERRACOTTA,
            XMaterial.LIGHT_GRAY_TERRACOTTA,
            XMaterial.LIME_TERRACOTTA,
            XMaterial.MAGENTA_TERRACOTTA,
            XMaterial.ORANGE_TERRACOTTA,
            XMaterial.PINK_TERRACOTTA,
            XMaterial.PURPLE_TERRACOTTA,
            XMaterial.RED_TERRACOTTA,
            XMaterial.WHITE_TERRACOTTA
    );

    public static final List<XMaterial> WOOL = Arrays.asList(
            XMaterial.BLACK_WOOL,
            XMaterial.BLUE_WOOL,
            XMaterial.BROWN_WOOL,
            XMaterial.CYAN_WOOL,
            XMaterial.GRAY_WOOL, XMaterial.GREEN_WOOL,
            XMaterial.LIGHT_BLUE_WOOL,
            XMaterial.LIGHT_GRAY_WOOL,
            XMaterial.LIME_WOOL,
            XMaterial.MAGENTA_WOOL,
            XMaterial.ORANGE_WOOL,
            XMaterial.PINK_WOOL,
            XMaterial.PURPLE_WOOL,
            XMaterial.RED_WOOL,
            XMaterial.WHITE_WOOL
    );

    public static final List<XMaterial> STAINED_GLASS = Arrays.asList(
            XMaterial.BLACK_STAINED_GLASS,
            XMaterial.BLUE_STAINED_GLASS,
            XMaterial.BROWN_STAINED_GLASS,
            XMaterial.CYAN_STAINED_GLASS,
            XMaterial.GRAY_STAINED_GLASS,
            XMaterial.GREEN_STAINED_GLASS,
            XMaterial.LIGHT_BLUE_STAINED_GLASS,
            XMaterial.LIGHT_GRAY_STAINED_GLASS,
            XMaterial.LIME_STAINED_GLASS,
            XMaterial.MAGENTA_STAINED_GLASS,
            XMaterial.PINK_STAINED_GLASS,
            XMaterial.PURPLE_STAINED_GLASS,
            XMaterial.RED_STAINED_GLASS,
            XMaterial.WHITE_STAINED_GLASS
    );

    public static final List<XMaterial> INK = Arrays.asList(
            XMaterial.RED_DYE,
            XMaterial.GREEN_DYE,
            XMaterial.COCOA_BEANS,
            XMaterial.LAPIS_LAZULI,
            XMaterial.PURPLE_DYE,
            XMaterial.CYAN_DYE,
            XMaterial.LIGHT_GRAY_DYE,
            XMaterial.GRAY_DYE,
            XMaterial.PINK_DYE,
            XMaterial.YELLOW_DYE,
            XMaterial.LIGHT_BLUE_DYE,
            XMaterial.MAGENTA_DYE,
            XMaterial.ORANGE_DYE,
            XMaterial.BONE_MEAL
    );

    public static final List<XMaterial> CONCRETE = Arrays.asList(
            XMaterial.BLACK_CONCRETE,
            XMaterial.BLUE_CONCRETE,
            XMaterial.BROWN_CONCRETE,
            XMaterial.CYAN_CONCRETE,
            XMaterial.GRAY_CONCRETE,
            XMaterial.GREEN_CONCRETE,
            XMaterial.LIGHT_BLUE_CONCRETE,
            XMaterial.LIGHT_GRAY_CONCRETE,
            XMaterial.LIME_CONCRETE,
            XMaterial.MAGENTA_CONCRETE,
            XMaterial.PINK_CONCRETE,
            XMaterial.PURPLE_CONCRETE,
            XMaterial.RED_CONCRETE,
            XMaterial.WHITE_CONCRETE
    );

    public static XMaterial getRandomTerracotta() {
        return TERRACOTTA.get(MathUtil.THREAD_LOCAL_RANDOM.nextInt(TERRACOTTA.size()));
    }

    public static ItemStack getRandomTerracottaItem() {
        return item(getRandomTerracotta());
    }

    public static XMaterial getRandomWool() {
        return WOOL.get(MathUtil.THREAD_LOCAL_RANDOM.nextInt(WOOL.size()));
    }

    public static ItemStack getRandomWoolItem() {
        return item(getRandomWool());
    }

    public static XMaterial getRandomStainedGlass() {
        return STAINED_GLASS.get(MathUtil.THREAD_LOCAL_RANDOM.nextInt(STAINED_GLASS.size()));
    }

    public static ItemStack getRandomStainedGlassItem() {
        return item(getRandomStainedGlass());
    }

    public static XMaterial getRandomInk() {
        return INK.get(MathUtil.THREAD_LOCAL_RANDOM.nextInt(INK.size()));
    }

    public static ItemStack getRandomInkItem() {
        return item(getRandomInk());
    }

    public static XMaterial getRandomConcrete() {
        return CONCRETE.get(MathUtil.THREAD_LOCAL_RANDOM.nextInt(CONCRETE.size()));
    }

    public static ItemStack getRandomConcreteItem() {
        return item(getRandomConcreteOrTerracotta());
    }

    /**
     * Concrete only exists on 1.12+, older servers get the matching terracotta instead.
     */
    public static XMaterial getRandomConcreteOrTerracotta() {
        XMaterial concrete = getRandomConcrete();
        return concrete.isSupported() ? concrete : getRandomTerracotta();
    }

    /**
     * Covers AIR plus the CAVE_AIR and VOID_AIR that 1.13 added.
     */
    public static boolean isAir(org.bukkit.Material material) {
        return material == null || material.name().endsWith("AIR");
    }

    private static ItemStack item(XMaterial material) {
        ItemStack itemStack = material.parseItem();
        return itemStack != null ? itemStack : XMaterial.WHITE_WOOL.parseItem();
    }
}
