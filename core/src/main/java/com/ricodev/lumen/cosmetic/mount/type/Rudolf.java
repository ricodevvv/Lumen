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
package com.ricodev.lumen.cosmetic.mount.type;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.util.entity.AttachedItem;
import com.ricodev.lumen.util.entity.Horses;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Rudolf implements MountBehavior {

    private static final ItemStack SADDLE_ITEM = XMaterial.SADDLE.parseItem();
    private static final ItemStack HORN_ITEM = XMaterial.DEAD_BUSH.parseItem();
    private static final ItemStack NOSE_ITEM = (XMaterial.RED_CONCRETE.isSupported() ? XMaterial.RED_CONCRETE : XMaterial.RED_WOOL).parseItem();

    private static final double HORN_WIDTH_OFFSET = 0.3d;
    private static final double HORN_FORWARD_OFFSET = 1.0d;
    private static final double HORN_HEIGHT_OFFSET = 2.1d;
    private static final double NOSE_FORWARD_OFFSET = 1.45d;
    private static final double NOSE_HEIGHT_OFFSET = 1.55d;

    private static final int PARTICLE_INTERVAL_TICKS = 5;
    private static final int PARTICLE_COUNT = 6;
    private static final double PARTICLE_SPREAD_HORIZONTAL = 1.0d;
    private static final double PARTICLE_SPREAD_VERTICAL = 1.2d;

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private final List<AttachedItem> decorations = new ArrayList<>();
    private int ticks;

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Horses.setup(entity, 1.0d, false, SADDLE_ITEM, false, null);
        Horses.setColor(entity, "BROWN", "NONE");

        Location base = entity.getLocation();

        for (int i = 0; i < 2; i++) {
            AttachedItem horn = new AttachedItem(base, HORN_ITEM, HORN_HEIGHT_OFFSET, HORN_FORWARD_OFFSET, i == 0 ? -HORN_WIDTH_OFFSET : HORN_WIDTH_OFFSET);
            horn.setHeadPose(-30.0d, i == 0 ? -20.0d : 20.0d, 0.0d);
            decorations.add(horn);
        }
        AttachedItem nose = new AttachedItem(base, NOSE_ITEM, NOSE_HEIGHT_OFFSET, NOSE_FORWARD_OFFSET, 0.0d);
        decorations.add(nose);
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        entity.getLocation(location);

        for (AttachedItem decoration : decorations) {
            decoration.update(location);
        }
        if (ticks % PARTICLE_INTERVAL_TICKS == 0) {
            Particles.spawn(location.getWorld(), LumenParticle.SNOWFLAKE,
                    location,
                    PARTICLE_COUNT,
                    PARTICLE_SPREAD_HORIZONTAL,
                    PARTICLE_SPREAD_VERTICAL,
                    PARTICLE_SPREAD_HORIZONTAL,
                    0.0d
            );
        }

        if (++ticks > 360) {
            ticks = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        for (AttachedItem decoration : decorations) {
            decoration.remove();
        }
        decorations.clear();
    }
}
