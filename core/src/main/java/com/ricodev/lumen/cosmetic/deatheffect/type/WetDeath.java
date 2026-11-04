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
package com.ricodev.lumen.cosmetic.deatheffect.type;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectBehavior;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectType;
import com.ricodev.lumen.util.MathUtil;

public class WetDeath implements DeathEffectBehavior {

    private static final ItemStack ITEM_STACK = XMaterial.LAPIS_BLOCK.parseItem();

    private static final int AMOUNT = 50;
    private static final double SPREAD = 5.0d;

    @Override
    public void onEquip(CosmeticContext<DeathEffectType> context) {
    }

    @Override
    public void playEffect(CosmeticContext<DeathEffectType> context, Location location) {
        location.add(0.0d, 1.0d, 0.0d);

        for (int i = 0; i < AMOUNT; i++) {
            Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                    location,
                    0,
                    MathUtil.randomRange(-SPREAD, SPREAD),
                    MathUtil.randomRange(-SPREAD, SPREAD),
                    MathUtil.randomRange(-SPREAD, SPREAD),
                    0.1d,
                    ITEM_STACK
            );
        }
    }

    @Override
    public void onUnequip(CosmeticContext<DeathEffectType> context) {
    }
}
