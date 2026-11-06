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
package com.ricodev.lumen.display;

import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.LegacyBlocks;
import com.ricodev.lumen.compat.ServerVersion;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.util.entity.FloatingBlock;
import com.ricodev.lumen.util.entity.FloatingBlockFactory;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Block and item displays for 1.19.4+, the same entities ProCosmetics uses. Blocks with a parent
 * ride it with their offset baked into the transformation, so they move with it on the client.
 */
public final class DisplayBlocks implements FloatingBlockFactory {

    private static final int TELEPORT_DURATION = 2;

    @Override
    public FloatingBlock block(Location location, StructureBlock block, float scale, @Nullable Entity parent) {
        Location at = flat(parent != null ? parent.getLocation() : location);
        Vector3f translation = new Vector3f(-scale / 2.0f, 0.0f, -scale / 2.0f);

        if (parent != null) {
            translation.add((float) (location.getX() - at.getX()), (float) (location.getY() - at.getY()), (float) (location.getZ() - at.getZ()));
        }
        BlockDisplay display = Entities.spawn(at, BlockDisplay.class, entity -> {
            entity.setBlock(blockData(block));
            entity.setTransformation(new Transformation(translation, new AxisAngle4f(), new Vector3f(scale, scale, scale), new AxisAngle4f()));
            setup(entity, TELEPORT_DURATION);
        });
        if (parent != null) {
            Compats.get().addPassenger(parent, display);
        }
        return new Floating(display, parent != null);
    }

    @Override
    public FloatingBlock cube(Location location, StructureBlock block, float scale) {
        Vector3f translation = new Vector3f(-scale / 2.0f, -scale / 2.0f, -scale / 2.0f);
        BlockDisplay display = Entities.spawn(flat(location), BlockDisplay.class, entity -> {
            entity.setBlock(blockData(block));
            entity.setTransformation(new Transformation(translation, new AxisAngle4f(), new Vector3f(scale, scale, scale), new AxisAngle4f()));
            setup(entity, TELEPORT_DURATION);
        });
        return new Floating(display, false);
    }

    @Override
    public FloatingBlock item(Location location, ItemStack item, float scale) {
        ItemDisplay display = Entities.spawn(location, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(scale, scale, scale), new AxisAngle4f()));
            setup(entity, 1);
        });
        return new Floating(display, false);
    }

    @Override
    public Entity seat(Location location) {
        return Entities.spawn(flat(location), BlockDisplay.class, entity -> setup(entity, TELEPORT_DURATION));
    }

    @Override
    public boolean canScale() {
        return true;
    }

    private static void setup(Display display, int teleportDuration) {
        if (ServerVersion.atLeast(20, 2)) {
            display.setTeleportDuration(teleportDuration);
        }
        display.setPersistent(false);
        MetadataUtil.setCustomEntity(display);
    }

    private static Location flat(Location location) {
        Location flat = location.clone();
        flat.setYaw(0.0f);
        flat.setPitch(0.0f);
        return flat;
    }

    private static BlockData blockData(StructureBlock block) {
        for (String candidate : LegacyBlocks.candidates(block)) {
            try {
                return Bukkit.createBlockData(candidate);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Material.STONE.createBlockData();
    }

    private static final class Floating implements FloatingBlock {

        private final Display display;
        private final boolean riding;

        Floating(Display display, boolean riding) {
            this.display = display;
            this.riding = riding;
        }

        @Override
        public void teleport(Location location) {
            display.teleport(display instanceof BlockDisplay ? flat(location) : location);
        }

        @Override
        public void follow() {
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            display.setRotation(yaw, pitch);
        }

        @Override
        public void glow(@Nullable Color color) {
            display.setGlowing(color != null);

            if (color != null) {
                display.setGlowColorOverride(color);
            }
        }

        @Override
        public void setItem(ItemStack item) {
            if (display instanceof ItemDisplay) {
                ((ItemDisplay) display).setItemStack(item);
            } else if (item.getType().isBlock()) {
                ((BlockDisplay) display).setBlock(item.getType().createBlockData());
            }
        }

        @Override
        public Location getLocation() {
            return display.getLocation();
        }

        @Override
        public Entity getEntity() {
            return display;
        }

        @Override
        public void remove() {
            if (riding && display.getVehicle() != null) {
                display.getVehicle().removePassenger(display);
            }
            display.remove();
        }
    }
}
