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

import com.ricodev.lumen.util.MetadataUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

/**
 * An item stuck to something that moves, like a horn on a horse. It's an invisible small armor
 * stand wearing the item, the old school stand-in for item display entities.
 */
public final class AttachedItem {

    private static final double HEAD_CENTER = 1.75d;
    private static final double SMALL_HEAD_CENTER = 0.875d;

    private final ArmorStand stand;
    private final double headCenter;
    private final double up;
    private final double forward;
    private final double side;

    /**
     * @param up      blocks above the base location, where the middle of the item ends up
     * @param forward blocks in front of it, along its yaw
     * @param side    blocks to its right
     * @param small   a small stand draws the item at half size (about 0.3 blocks instead of 0.6)
     */
    public AttachedItem(Location base, ItemStack itemStack, double up, double forward, double side, boolean small) {
        this.headCenter = small ? SMALL_HEAD_CENTER : HEAD_CENTER;
        this.up = up;
        this.forward = forward;
        this.side = side;
        this.stand = Entities.spawn(target(base), ArmorStand.class, spawned -> {
            spawned.setVisible(false);
            spawned.setSmall(small);
            spawned.setMarker(true);
            spawned.setGravity(false);
            spawned.setBasePlate(false);
            spawned.setHelmet(itemStack);
            MetadataUtil.setCustomEntity(spawned);
        });
    }

    public void setHeadPose(double pitchDegrees, double yawDegrees, double rollDegrees) {
        stand.setHeadPose(new EulerAngle(Math.toRadians(pitchDegrees), Math.toRadians(yawDegrees), Math.toRadians(rollDegrees)));
    }

    public void update(Location base) {
        stand.teleport(target(base));
    }

    public void remove() {
        stand.remove();
    }

    private Location target(Location base) {
        double yaw = Math.toRadians(base.getYaw());
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);
        Location target = base.clone().add(dirX * forward - dirZ * side, up - headCenter, dirZ * forward + dirX * side);
        target.setPitch(0.0f);
        return target;
    }
}
