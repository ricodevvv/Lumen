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
package com.ricodev.lumen.treasure.animation.type;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.Material;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.treasure.TreasureChestPlatform;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.treasure.animation.TreasureChestAnimationImpl;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.MathUtil;

public class Legendary extends TreasureChestAnimationImpl {

    private static final double SPEED = 15.0d;
    private static final float RADIUS_INCREASE_PER_TICK = 0.03f;
    private static final double GOAL_RADIUS = 3.3d;

    private final double anglePerChest = 360.0d / platform.getChestLocations().size();
    private float radius;

    public Legendary(Lumen plugin, TreasureChestPlatform platform, TreasureChest treasure, User user) {
        super(plugin, platform, treasure, user);
    }

    @Override
    public void onSecondUpdate() {
        if (animationState == AnimationState.SPAWNING_CHESTS) {
            for (int i = 0; i < platform.getChestLocations().size(); i++) {
                spawnChest(Material.ENDER_CHEST, platform.getCenter());
            }
            animationState = AnimationState.BUILT;
        }
    }

    @Override
    public void onTickUpdate() {
        if (animationState == AnimationState.BUILT) {
            Location location = platform.getCenter();
            radius += RADIUS_INCREASE_PER_TICK;

            Sounds.playAt(location, XSound.ENTITY_HORSE_LAND, 0.2f, (radius / 2));

            for (int i = 0; i < blockDisplayChests.size(); i++) {
                CosmeticEntity nmsEntity = blockDisplayChests.get(i);
                float angle = FastMathUtil.toRadians(i * anglePerChest + ticks * SPEED);
                Location loc = MathUtil.getLocationAroundCircle(platform.getCenter(), radius, angle);

                nmsEntity.sendPositionRotationPacket(loc);
                Particles.spawn(loc.getWorld(), LumenParticle.WITCH, loc.add(0.0d, 0.7d, 0.0d), 0);
            }

            if (radius > GOAL_RADIUS) {
                for (Location chestLocation : platform.getChestLocations()) {
                    setChestBlock(Material.ENDER_CHEST, chestLocation);
                }
                despawnChest();
                animationState = AnimationState.DONE;
            }
        }
    }
}
