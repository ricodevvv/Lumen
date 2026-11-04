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
package com.ricodev.lumen.compat;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleBlockStateData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends particles as packets through PacketEvents, so the same call works from 1.8 to 26.x.
 * The overloads mirror {@code World#spawnParticle}, the world argument only picks who sees it.
 */
public final class Particles {

    private static final double VIEW_DISTANCE_SQUARED = 32.0d * 32.0d;
    private static final Map<LumenParticle, ParticleType<?>> TYPES = new EnumMap<>(LumenParticle.class);

    private Particles() {
    }

    /**
     * Colored dust, the modern {@code Particle.DustOptions}.
     */
    public static final class Dust {

        private final Color color;
        private final float size;

        public Dust(Color color, float size) {
            this.color = color;
            this.size = size;
        }

        public Color getColor() {
            return color;
        }

        public float getSize() {
            return size;
        }
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, 0, 0, 0, 0, null);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count, @Nullable Object data) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, 0, 0, 0, 0, data);
    }

    public static void spawn(World world, LumenParticle particle, double x, double y, double z, int count) {
        spawn(world, particle, x, y, z, count, 0, 0, 0, 0, null);
    }

    public static void spawn(World world, LumenParticle particle, double x, double y, double z, int count, @Nullable Object data) {
        spawn(world, particle, x, y, z, count, 0, 0, 0, 0, data);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, 0, null);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ, @Nullable Object data) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, 0, data);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ, double extra) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, null);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ, double extra, @Nullable Object data) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    public static void spawn(World world, LumenParticle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ, double extra, @Nullable Object data, boolean force) {
        spawn(world, particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    public static void spawn(World world, LumenParticle particle, double x, double y, double z, int count,
                             double offsetX, double offsetY, double offsetZ) {
        spawn(world, particle, x, y, z, count, offsetX, offsetY, offsetZ, 0, null);
    }

    public static void spawn(World world, LumenParticle particle, double x, double y, double z, int count,
                             double offsetX, double offsetY, double offsetZ, double extra) {
        spawn(world, particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    public static void spawn(World world, LumenParticle particle, double x, double y, double z, int count,
                             double offsetX, double offsetY, double offsetZ, double extra, @Nullable Object data) {
        if (world == null || world.getPlayers().isEmpty()) {
            return;
        }
        LumenParticle resolved = particle.resolve();

        if (resolved == LumenParticle.DUST && !ServerVersion.atLeast(13)) {
            spawnLegacyDust(world, x, y, z, count, offsetX, offsetY, offsetZ, data);
            return;
        }
        Particle<?> packetParticle = toPacketParticle(resolved, data);

        if (packetParticle == null) {
            return;
        }
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(packetParticle, true,
                new Vector3d(x, y, z),
                new Vector3f((float) offsetX, (float) offsetY, (float) offsetZ),
                (float) extra,
                count);
        send(world, x, y, z, packet);
    }

    /**
     * Before 1.13 dust can't carry a color, so the offsets are the color and each particle goes out on its own.
     */
    private static void spawnLegacyDust(World world, double x, double y, double z, int count,
                                        double offsetX, double offsetY, double offsetZ, @Nullable Object data) {
        Color color = data instanceof Dust ? ((Dust) data).getColor() : Color.RED;
        float red = Math.max(color.getRed() / 255.0f, 0.001f);
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        Particle<?> packetParticle = toPacketParticle(LumenParticle.DUST, null);

        if (packetParticle == null) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int amount = Math.max(1, count);

        for (int i = 0; i < amount; i++) {
            double px = x + (offsetX == 0 ? 0 : random.nextGaussian() * offsetX);
            double py = y + (offsetY == 0 ? 0 : random.nextGaussian() * offsetY);
            double pz = z + (offsetZ == 0 ? 0 : random.nextGaussian() * offsetZ);

            WrapperPlayServerParticle packet = new WrapperPlayServerParticle(packetParticle, true,
                    new Vector3d(px, py, pz),
                    new Vector3f(red, green, blue),
                    1.0f,
                    0);
            send(world, px, py, pz, packet);
        }
    }

    private static void send(World world, double x, double y, double z, WrapperPlayServerParticle packet) {
        for (Player player : world.getPlayers()) {
            Location location = player.getLocation();
            double dx = location.getX() - x;
            double dy = location.getY() - y;
            double dz = location.getZ() - z;

            if (dx * dx + dy * dy + dz * dz <= VIEW_DISTANCE_SQUARED) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Particle<?> toPacketParticle(LumenParticle particle, @Nullable Object data) {
        ParticleType<?> type = type(particle);

        if (type == null) {
            return null;
        }
        ParticleData particleData = null;

        if (particle == LumenParticle.DUST) {
            Dust dust = data instanceof Dust ? (Dust) data : new Dust(Color.RED, 1.0f);
            Color color = dust.getColor();
            particleData = new ParticleDustData(dust.getSize(), color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
        } else if (particle == LumenParticle.ITEM) {
            ItemStack itemStack = data instanceof ItemStack ? (ItemStack) data : XMaterial.STONE.parseItem();
            particleData = new ParticleItemStackData(SpigotConversionUtil.fromBukkitItemStack(itemStack));
        } else if (particle.needsBlock()) {
            XMaterial material = data instanceof XMaterial ? (XMaterial) data : XMaterial.STONE;
            WrappedBlockState state = Compats.get().blockState(material);

            if (state == null) {
                return null;
            }
            particleData = new ParticleBlockStateData(state);
        }
        return particleData == null ? new Particle(type) : new Particle(type, particleData);
    }

    private static @Nullable ParticleType<?> type(LumenParticle particle) {
        ParticleType<?> type = TYPES.get(particle);

        if (type == null && !TYPES.containsKey(particle)) {
            type = ParticleTypes.getByName("minecraft:" + particle.name().toLowerCase(java.util.Locale.ROOT));
            TYPES.put(particle, type);
        }
        return type;
    }
}
