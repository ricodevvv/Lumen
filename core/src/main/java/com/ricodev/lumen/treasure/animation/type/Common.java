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
import com.ricodev.lumen.util.MathUtil;

import java.util.Iterator;

public class Common extends TreasureChestAnimationImpl {

    private int chests;

    public Common(Lumen plugin, TreasureChestPlatform platform, TreasureChest treasure, User user) {
        super(plugin, platform, treasure, user);
    }

    @Override
    public void onSecondUpdate() {
        if (animationState == AnimationState.SPAWNING_CHESTS) {
            if (chests < platform.getChestLocations().size()) {
                Location location = platform.getChestLocations().get(chests++).clone().add(0.0d, 3.0d, 0.0d);
                location.setDirection(platform.getCenter().toVector().subtract(location.toVector()));
                MathUtil.snapToCardinalDirection(location);
                location.setPitch(0.0f);

                spawnChest(Material.CHEST, location);
            } else {
                animationState = AnimationState.BUILT;
            }
        }
    }

    @Override
    public void onTickUpdate() {
        if (animationState == AnimationState.SPAWNING_CHESTS || animationState == AnimationState.BUILT) {
            Iterator<CosmeticEntity> iterator = blockDisplayChests.iterator();

            while (iterator.hasNext()) {
                CosmeticEntity nmsEntity = iterator.next();
                nmsEntity.getPreviousLocation(location);

                Particles.spawn(location.getWorld(), LumenParticle.LARGE_SMOKE, location.add(0.0d, 1.0d, 0.0d), 0);
                location.subtract(0.0d, 1.3d, 0.0d);

                nmsEntity.sendPositionRotationPacket(location);

                if (location.getY() < platform.getCenter().getY()) {
                    setChestBlock(Material.CHEST, location.add(0.0d, 1.0d, 0.0d));

                    nmsEntity.getTracker().destroy();
                    iterator.remove();
                }
            }
        }
    }
}
