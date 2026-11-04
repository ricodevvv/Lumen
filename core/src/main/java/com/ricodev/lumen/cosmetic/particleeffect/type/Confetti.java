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
package com.ricodev.lumen.cosmetic.particleeffect.type;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.util.material.Materials;

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;

import org.bukkit.Location;
import org.bukkit.Material;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectBehavior;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectType;

import java.util.List;
import java.util.Random;

public class Confetti implements ParticleEffectBehavior {

    private static final Random RANDOM = new Random();

    private static final double MOVING_HEIGHT_OFFSET = 0.5d;
    private static final int MOVING_PARTICLE_COUNT = 4;
    private static final float MOVING_SPREAD_X = 0.1f;
    private static final float MOVING_SPREAD_Y = 0.0f;
    private static final float MOVING_SPREAD_Z = 0.1f;

    private static final double STATIC_HEIGHT_OFFSET = 2.0d;
    private static final int STATIC_PARTICLE_COUNT = 4;
    private static final float STATIC_SPREAD_X = 0.4f;
    private static final float STATIC_SPREAD_Y = 1.0f;
    private static final float STATIC_SPREAD_Z = 0.4f;

    @Override
    public void onEquip(CosmeticContext<ParticleEffectType> context) {
    }

    @Override
    public void onUpdate(CosmeticContext<ParticleEffectType> context, Location location) {
        XMaterial randomColor = Materials.getRandomConcreteOrTerracotta();

        if (context.getUser().isMoving()) {
            spawnMovingEffect(location, randomColor);
        } else {
            spawnStaticEffect(location, randomColor);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ParticleEffectType> context) {
    }

    private void spawnMovingEffect(Location location, XMaterial blockData) {
        location.add(0.0d, MOVING_HEIGHT_OFFSET, 0.0d);
        Particles.spawn(location.getWorld(),
                LumenParticle.FALLING_DUST,
                location,
                MOVING_PARTICLE_COUNT,
                MOVING_SPREAD_X,
                MOVING_SPREAD_Y,
                MOVING_SPREAD_Z,
                blockData
        );
    }

    private void spawnStaticEffect(Location location, XMaterial blockData) {
        location.add(0.0d, STATIC_HEIGHT_OFFSET, 0.0d);
        Particles.spawn(location.getWorld(),
                LumenParticle.FALLING_DUST,
                location,
                STATIC_PARTICLE_COUNT,
                STATIC_SPREAD_X,
                STATIC_SPREAD_Y,
                STATIC_SPREAD_Z,
                blockData
        );
    }
}
