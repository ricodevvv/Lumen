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

import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.util.MetadataUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

/**
 * Entity helpers that replace what ProCosmetics did through NMS: spawning with setup,
 * mobs without AI that walk after their owner, and items nobody can pick up.
 */
public final class Entities {

    private Entities() {
    }

    /**
     * Spawns the entity and runs the setup right after. The consumer overload of
     * {@code World#spawn} doesn't exist before 1.11.
     */
    public static <T extends Entity> T spawn(Location location, Class<T> type, Consumer<T> setup) {
        T entity = location.getWorld().spawn(location, type);
        setup.accept(entity);
        return entity;
    }

    public static Item dropItem(Location location, ItemStack itemStack, Consumer<Item> setup) {
        Item item = location.getWorld().dropItem(location, itemStack);
        setup.accept(item);
        return item;
    }

    /**
     * Turns a real mob into a cosmetic one: no AI, no sounds, can't be hurt and isn't saved.
     */
    public static void makeCosmetic(Entity entity) {
        MetadataUtil.setCustomEntity(entity);
        Compats.get().setSilent(entity, true);

        if (entity instanceof LivingEntity) {
            Compats.get().setAI((LivingEntity) entity, false);
            ((LivingEntity) entity).setRemoveWhenFarAway(false);
        }
    }

    /**
     * Moves a mob without AI one step closer to the target, stopping a couple of blocks away.
     * Called every tick, so the client animates it walking.
     *
     * @return true if the entity moved
     */
    public static boolean stepTowards(Entity entity, Location target, double stopDistance, double speed) {
        Location current = entity.getLocation();

        if (!current.getWorld().equals(target.getWorld()) || current.distanceSquared(target) > 32.0d * 32.0d) {
            entity.teleport(target);
            return true;
        }
        Vector direction = target.toVector().subtract(current.toVector());
        direction.setY(0.0d);
        double distance = direction.length();

        if (distance <= stopDistance) {
            face(entity, target);
            return false;
        }
        direction.multiply(Math.min(speed, distance - stopDistance) / distance);
        Location next = current.clone().add(direction);
        next.setY(groundY(next, target.getY()));
        next.setYaw(yawTowards(current, target));
        next.setPitch(0.0f);
        entity.teleport(next);
        return true;
    }

    private static void face(Entity entity, Location target) {
        Location current = entity.getLocation();
        float yaw = yawTowards(current, target);

        if (Math.abs(current.getYaw() - yaw) > 10.0f) {
            current.setYaw(yaw);
            entity.teleport(current);
        }
    }

    private static float yawTowards(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /**
     * Finds the floor near the given height so pets walk up and down steps instead of floating.
     */
    private static double groundY(Location location, double preferredY) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int start = (int) Math.floor(preferredY) + 1;

        for (int y = start; y >= start - 4 && y > 0; y--) {
            Block below = world.getBlockAt(x, y - 1, z);

            if (below.getType().isSolid() && !world.getBlockAt(x, y, z).getType().isSolid()) {
                return y;
            }
        }
        return preferredY;
    }

    /**
     * Drops an item that can't be picked up and goes away after a few seconds.
     */
    public static Item dropDecorativeItem(Plugin plugin, Location location, ItemStack itemStack, Vector velocity, int ticks) {
        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setVelocity(velocity);
        MetadataUtil.setCustomEntity(item);
        plugin.getServer().getScheduler().runTaskLater(plugin, item::remove, ticks);
        return item;
    }
}
