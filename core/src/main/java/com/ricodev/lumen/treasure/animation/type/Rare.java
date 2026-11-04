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
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.MathUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Rare extends TreasureChestAnimationImpl {

    private static final int ANGLE_OFFSET = 110;
    private static final double SPEED = 6.0d;
    private static final double RADIUS = 3.2d;
    private static final double Y_OFFSET = 6.0d;
    private static final double Y_DECREASE_PER_TICK = 0.1d;

    private static class MovingChest {

        public CosmeticEntity nmsEntity;
        public int index;
        public boolean reachedGround = false;

        public MovingChest(CosmeticEntity nmsEntity, int index) {
            this.nmsEntity = nmsEntity;
            this.index = index;
        }
    }

    private final double ANGLE_PER_CHEST = 360.0d / platform.getChestLocations().size();
    private final List<MovingChest> movingChests = new ArrayList<>();

    private int chests;

    public Rare(Lumen plugin, TreasureChestPlatform platform, TreasureChest treasure, User user) {
        super(plugin, platform, treasure, user);
    }

    @Override
    public void onSecondUpdate() {
        if (animationState == AnimationState.SPAWNING_CHESTS) {
            if (chests < platform.getChestLocations().size()) {
                float angle = FastMathUtil.toRadians(chests * ANGLE_PER_CHEST + (ticks - 60 - 20 * chests) * SPEED - ANGLE_OFFSET);
                Location location = getLocation(angle);
                location.setY(platform.getCenter().getY() + Y_OFFSET);

                CosmeticEntity nmsEntity = spawnChest(Material.CHEST, location);
                movingChests.add(new MovingChest(nmsEntity, chests));

                chests++;
            } else {
                animationState = AnimationState.BUILT;
            }
        }
    }

    @Override
    public void onTickUpdate() {
        if (animationState == AnimationState.SPAWNING_CHESTS || animationState == AnimationState.BUILT) {
            Iterator<MovingChest> iterator = movingChests.iterator();
            while (iterator.hasNext()) {
                MovingChest movingChest = iterator.next();
                CosmeticEntity nmsEntity = movingChest.nmsEntity;
                int index = movingChest.index;

                float angle = FastMathUtil.toRadians(index * ANGLE_PER_CHEST + (ticks - 60 - 20 * index) * SPEED - ANGLE_OFFSET);

                location = getLocation(angle);
                location.setY(nmsEntity.getPreviousLocation().getY() - Y_DECREASE_PER_TICK);

                nmsEntity.sendPositionRotationPacket(location);

                if (location.getY() < platform.getCenter().getY()) {
                    if (!movingChest.reachedGround) {
                        setChestBlock(Material.CHEST, platform.getChestLocations().get(index));
                        nmsEntity.getTracker().destroy();
                        movingChest.reachedGround = true;
                        iterator.remove();

                        if (!iterator.hasNext()) {
                            animationState = AnimationState.DONE;
                        }
                    }
                }
                Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location.add(0.0d, 0.6d, 0.0d), 0);
            }
        }
    }

    private Location getLocation(double angle) {
        return MathUtil.getLocationAroundCircle(platform.getCenter(), RADIUS, angle);
    }
}
