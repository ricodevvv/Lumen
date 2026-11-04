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

import com.ricodev.lumen.util.entity.Horses;

import com.ricodev.lumen.util.entity.AttachedItem;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.ricodev.lumen.compat.Compats;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.cosmetic.mount.BlockTrailBehavior;
import com.ricodev.lumen.util.MathUtil;

import java.util.List;

public class Unicorn extends BlockTrailBehavior {

    private static final ItemStack SADDLE_ITEM = XMaterial.SADDLE.parseItem();
    private static final ItemStack HORN_ITEM = XMaterial.END_ROD.parseItem();
    private static final List<Color> RAINBOW_COLORS = Collects.listOf(
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.LIME,
            Color.GREEN,
            Color.AQUA,
            Color.BLUE,
            Color.PURPLE
    );
    private static final List<ItemStack> TRAIL_BLOCKS = Collects.listOf(
            XMaterial.PINK_CONCRETE.parseItem(),
            XMaterial.MAGENTA_CONCRETE.parseItem()
    );



    private static final double RAINBOW_BEHIND_OFFSET = -0.7d;
    private static final double RAINBOW_HEIGHT_OFFSET = 1.4d;
    private static final double RAINBOW_COLOR_SPACING = 0.08d;
    private static final int RAINBOW_PARTICLE_COUNT = 2;
    private static final float RAINBOW_PARTICLE_SIZE = 1.0f;

    private static final float PITCH_FOLLOW_FACTOR = 0.5f;
    private static final int FIREWORK_INTERVAL_TICKS = 10;
    private static final int FIREWORK_PARTICLE_COUNT = 5;
    private static final double FIREWORK_HEIGHT_OFFSET = 1.0d;
    private static final double FIREWORK_SPREAD_HORIZONTAL = 1.0d;
    private static final double FIREWORK_SPREAD_VERTICAL = 1.5d;

    private int ticks;
    private AttachedItem horn;

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.setupEntity(context, entity, nmsEntity);

        Horses.setup(entity, 1.0d, false, SADDLE_ITEM, false, null);
        Horses.setColor(entity, "WHITE", "WHITE");
        horn = new AttachedItem(entity.getLocation(), HORN_ITEM, 1.9d, 1.1d, 0.0d);
        horn.setHeadPose(-30.0d, 0.0d, 0.0d);
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        super.onUpdate(context, entity, nmsEntity);

        entity.getLocation(location);

        location.setPitch(location.getPitch() * PITCH_FOLLOW_FACTOR);

        horn.update(location);

        if (context.getUser().isMoving() && context.getPlayer().getVehicle() == entity) {
            Location rainbowLocation = MathUtil.getDirectionalLocation(location, 0.0d, RAINBOW_BEHIND_OFFSET);
            rainbowLocation.add(0.0d, RAINBOW_HEIGHT_OFFSET, 0.0d);

            for (Color color : RAINBOW_COLORS) {
                Particles.spawn(rainbowLocation.getWorld(), LumenParticle.DUST,
                        rainbowLocation,
                        RAINBOW_PARTICLE_COUNT,
                        0.0d,
                        0.0d,
                        0.0d,
                        0.0d,
                        new Particles.Dust(color, RAINBOW_PARTICLE_SIZE)
                );
                rainbowLocation.setY(rainbowLocation.getY() - RAINBOW_COLOR_SPACING);
            }
        }

        if (ticks % FIREWORK_INTERVAL_TICKS == 0) {
            Location fireworkLocation = entity.getLocation().add(0.0d, FIREWORK_HEIGHT_OFFSET, 0.0d);
            Particles.spawn(fireworkLocation.getWorld(), LumenParticle.FIREWORK,
                    fireworkLocation,
                    FIREWORK_PARTICLE_COUNT,
                    FIREWORK_SPREAD_HORIZONTAL,
                    FIREWORK_SPREAD_VERTICAL,
                    FIREWORK_SPREAD_HORIZONTAL,
                    0.0d
            );
        }

        if (++ticks > 360) {
            ticks = 0;
        }
    }


    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        super.onUnequip(context);

        if (horn != null) {
            horn.remove();
            horn = null;
        }
    }

    @Override
    public int getBlockTrailRadius() {
        return 1;
    }

    @Override
    public List<ItemStack> getTrailBlocks() {
        return TRAIL_BLOCKS;
    }
}
