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

import com.ricodev.lumen.compat.ServerVersion;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Horse setup that works on both sides of 1.11. Before that zombie, skeleton, donkey and mule
 * were all a Horse with a variant; after that they're separate entity types.
 */
public final class Horses {

    private Horses() {
    }

    /**
     * @param legacyVariant the 1.8 {@code Horse.Variant} name, used only before 1.11
     */
    public static void setup(Entity entity, double jumpStrength, boolean baby, @Nullable ItemStack saddle,
                             boolean chest, @Nullable String legacyVariant) {
        if (entity instanceof Ageable) {
            if (baby) {
                ((Ageable) entity).setBaby();
            } else {
                ((Ageable) entity).setAdult();
            }
        }
        if (entity instanceof Tameable) {
            ((Tameable) entity).setTamed(true);
        }
        if (legacyVariant != null && !ServerVersion.atLeast(11) && entity instanceof Horse) {
            setLegacyVariant((Horse) entity, legacyVariant);
        }
        invoke(entity, "setJumpStrength", double.class, jumpStrength);

        if (chest) {
            invoke(entity, "setCarryingChest", boolean.class, true);
        }
        if (saddle != null) {
            try {
                Object inventory = entity.getClass().getMethod("getInventory").invoke(entity);
                inventory.getClass().getMethod("setSaddle", ItemStack.class).invoke(inventory, saddle);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }
    }

    public static void setColor(Entity entity, String color, String style) {
        if (entity instanceof Horse) {
            Horse horse = (Horse) entity;
            horse.setColor(Horse.Color.valueOf(color));
            horse.setStyle(Horse.Style.valueOf(style));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setLegacyVariant(Horse horse, String variant) {
        try {
            Class<? extends Enum> variantClass = (Class<? extends Enum>) Class.forName("org.bukkit.entity.Horse$Variant");
            Horse.class.getMethod("setVariant", variantClass).invoke(horse, Enum.valueOf(variantClass, variant));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static void invoke(Object target, String method, Class<?> type, Object value) {
        try {
            target.getClass().getMethod(method, type).invoke(target, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
}
