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
package com.ricodev.lumen.cosmetic.arroweffect.type;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectBehavior;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectType;
import com.ricodev.lumen.util.MathUtil;

import java.util.List;

public class CandyCaneArrows implements ArrowEffectBehavior {

    private static final List<ItemStack> ITEMS = Collects.listOf(
            XMaterial.RED_DYE.parseItem(),
            XMaterial.GREEN_DYE.parseItem(),
            XMaterial.BONE_MEAL.parseItem()
    );

    private static final double UPDATE_SPREAD = 0.1d;
    private static final int HIT_AMOUNT = 40;
    private static final double HIT_SPREAD = 5.0d;

    @Override
    public void onEquip(CosmeticContext<ArrowEffectType> context) {
    }

    @Override
    public void onUpdate(CosmeticContext<ArrowEffectType> context, Location location) {
        for (ItemStack itemStack : ITEMS) {
            Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                    location,
                    1,
                    UPDATE_SPREAD,
                    UPDATE_SPREAD,
                    UPDATE_SPREAD,
                    0.0d,
                    itemStack
            );
        }
    }

    @Override
    public void onArrowHit(CosmeticContext<ArrowEffectType> context, Location location) {
        for (int i = 0; i < HIT_AMOUNT; i++) {
            for (ItemStack itemStack : ITEMS) {
                Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                        location,
                        0,
                        MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                        MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                        MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                        0.1d, itemStack
                );
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ArrowEffectType> context) {
    }
}
