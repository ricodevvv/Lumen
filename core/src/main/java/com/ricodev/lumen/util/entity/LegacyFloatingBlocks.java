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

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.ServerVersion;
import com.ricodev.lumen.util.MetadataUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Falling blocks for servers without display entities. From 1.10 they just have no gravity. On 1.8
 * and 1.9 there's no such thing, so they ride an invisible armor stand (or the client drops them to
 * the floor) and get noclip (or the server places them as a block once they touch the ground).
 */
final class LegacyFloatingBlocks implements FloatingBlockFactory {

    private static final double HEAD_CENTER = 1.75d;
    private static final double SMALL_HEAD_CENTER = 0.875d;
    private static final boolean RIDE = !ServerVersion.atLeast(10);

    private final Set<Falling> blocks = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    @Override
    public FloatingBlock block(Location location, StructureBlock block, float scale, @Nullable Entity parent) {
        Falling floating = new Falling(location, block, parent);
        blocks.add(floating);
        startTask();
        return floating;
    }

    @Override
    public FloatingBlock cube(Location location, StructureBlock block, float scale) {
        return new Held(location, Compats.get().blockItem(block), scale < 0.45f);
    }

    @Override
    public FloatingBlock item(Location location, ItemStack item, float scale) {
        return new Held(location, item, scale < 0.45f);
    }

    @Override
    public Entity seat(Location location) {
        return stand(location);
    }

    @Override
    public boolean canScale() {
        return false;
    }

    private void startTask() {
        if (task == null) {
            LumenPlugin plugin = LumenPlugin.getPlugin();
            task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                for (Falling block : new ArrayList<>(blocks)) {
                    block.keepAlive();
                }
            }, 10L, 10L);
        }
    }

    private static ArmorStand stand(Location location) {
        return Entities.spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setBasePlate(false);
            Entities.makeCosmetic(stand);
            Compats.get().setPersistent(stand, false);
        });
    }

    private static void setNoClip(Entity entity) {
        try {
            Object handle = entity.getClass().getMethod("getHandle").invoke(entity);
            Field noclip = field(handle.getClass(), "noclip");

            if (noclip != null) {
                noclip.setBoolean(handle, true);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static void resetFallTime(FallingBlock fallingBlock) {
        if (!RIDE) {
            fallingBlock.setTicksLived(1);
            return;
        }
        try {
            Object handle = fallingBlock.getClass().getMethod("getHandle").invoke(fallingBlock);
            Field ticksLived = handle.getClass().getDeclaredField("ticksLived");
            ticksLived.setAccessible(true);
            ticksLived.setInt(handle, 1);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private static @Nullable Field field(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private final class Falling implements FloatingBlock {

        private final StructureBlock data;
        private final Entity parent;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private Location location;
        private FallingBlock fallingBlock;
        private ArmorStand stand;
        private boolean removed;

        Falling(Location location, StructureBlock data, @Nullable Entity parent) {
            this.location = location.clone();
            this.data = data;
            this.parent = parent;

            if (parent != null) {
                Location parentLocation = parent.getLocation();
                offsetX = location.getX() - parentLocation.getX();
                offsetY = location.getY() - parentLocation.getY();
                offsetZ = location.getZ() - parentLocation.getZ();
            } else {
                offsetX = offsetY = offsetZ = 0.0d;
            }
            spawn();
        }

        private void spawn() {
            Location at = location.clone();
            at.setYaw(0.0f);
            at.setPitch(0.0f);
            Entities.loadChunk(at);

            fallingBlock = Compats.get().spawnFallingBlock(at, data);
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);
            MetadataUtil.setCustomEntity(fallingBlock);
            Compats.get().setPersistent(fallingBlock, false);
            Compats.get().setGravity(fallingBlock, false);
            fallingBlock.teleport(at);

            if (RIDE) {
                setNoClip(fallingBlock);
                stand = stand(at);
                Compats.get().addPassenger(stand, fallingBlock);
            }
        }

        void keepAlive() {
            if (removed) {
                return;
            }
            if (!fallingBlock.isValid() || (stand != null && !stand.isValid())) {
                despawn();
                spawn();
                return;
            }
            resetFallTime(fallingBlock);

            if (stand != null && !ServerVersion.atLeast(9)) {
                WrapperPlayServerAttachEntity attach = new WrapperPlayServerAttachEntity(fallingBlock.getEntityId(), stand.getEntityId(), false);

                for (Player player : stand.getWorld().getPlayers()) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, attach);
                }
            }
        }

        @Override
        public void teleport(Location location) {
            this.location = location.clone();
            Location at = location.clone();
            at.setYaw(0.0f);
            at.setPitch(0.0f);

            if (stand != null) {
                Compats.get().teleportWithPassengers(stand, at);
            } else {
                fallingBlock.teleport(at);
            }
        }

        @Override
        public void follow() {
            if (parent != null) {
                teleport(parent.getLocation().add(offsetX, offsetY, offsetZ));
            }
        }

        @Override
        public void setRotation(float yaw, float pitch) {
        }

        @Override
        public void glow(@Nullable Color color) {
        }

        @Override
        public void setItem(ItemStack item) {
        }

        @Override
        public Location getLocation() {
            return location.clone();
        }

        @Override
        public @Nullable Entity getEntity() {
            return fallingBlock;
        }

        @Override
        public void remove() {
            removed = true;
            blocks.remove(this);
            despawn();
        }

        private void despawn() {
            if (stand != null) {
                stand.eject();
                stand.remove();
                stand = null;
            }
            fallingBlock.remove();
        }
    }

    private static final class Held implements FloatingBlock {

        private final ArmorStand stand;
        private final double headCenter;
        private Location location;

        Held(Location location, ItemStack item, boolean small) {
            this.location = location.clone();
            this.headCenter = small ? SMALL_HEAD_CENTER : HEAD_CENTER;
            this.stand = stand(standLocation(location));
            stand.setSmall(small);
            stand.setHelmet(item);
        }

        private Location standLocation(Location location) {
            return location.clone().subtract(0.0d, headCenter, 0.0d);
        }

        @Override
        public void teleport(Location location) {
            this.location = location.clone();
            stand.teleport(standLocation(location));
        }

        @Override
        public void follow() {
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            Location rotated = stand.getLocation();
            rotated.setYaw(yaw);
            stand.teleport(rotated);
        }

        @Override
        public void glow(@Nullable Color color) {
            Compats.get().setGlowing(stand, color != null);
        }

        @Override
        public void setItem(ItemStack item) {
            stand.setHelmet(item);
        }

        @Override
        public Location getLocation() {
            return location.clone();
        }

        @Override
        public Entity getEntity() {
            return stand;
        }

        @Override
        public void remove() {
            stand.remove();
        }
    }
}
