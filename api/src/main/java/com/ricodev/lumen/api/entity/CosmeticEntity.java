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
package com.ricodev.lumen.api.entity;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * An entity that belongs to a cosmetic. It's a real entity without AI under the hood, so it
 * behaves the same from 1.8 up. Settings made before it spawns are kept and applied on spawn.
 */
public interface CosmeticEntity {

    /**
     * Moves the entity, spawning it there if it isn't spawned yet.
     */
    void setPositionRotation(Location location);

    void sendPositionRotationPacket(Location location);

    @Nullable
    Location getPreviousLocation();

    /**
     * Copies the last known location into {@code location} and returns it.
     */
    Location getPreviousLocation(Location location);

    void setYaw(float yaw);

    void setPitch(float pitch);

    void setVelocity(double x, double y, double z);

    default void setVelocity(Vector velocity) {
        setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
    }

    void move(Vector vector);

    /**
     * Walks one step towards the player, call it every tick.
     */
    void follow(Player player);

    /**
     * Moves the entity based on what its rider is pressing, call it every tick while ridden.
     */
    void moveRide(Player player);

    float getRideSpeed();

    void setRideSpeed(float speed);

    void setMainHand(@Nullable ItemStack itemStack);

    void setOffHand(@Nullable ItemStack itemStack);

    void setHelmet(@Nullable ItemStack itemStack);

    void setChestplate(@Nullable ItemStack itemStack);

    void setLeggings(@Nullable ItemStack itemStack);

    void setBoots(@Nullable ItemStack itemStack);

    void setHeadPose(float x, float y, float z);

    void setBodyPose(float x, float y, float z);

    void setLeftArmPose(float x, float y, float z);

    void setRightArmPose(float x, float y, float z);

    void setLeftLegPose(float x, float y, float z);

    void setRightLegPose(float x, float y, float z);

    void setCustomName(@Nullable Component component);

    void setLeashHolder(@Nullable Entity holder);

    void setCreeperPowered(boolean powered);

    void setCreeperIgnited(boolean ignited);

    void setHurtTicks(int hurtTicks);

    void setNoClip(boolean noClip);

    void setEntityItemStack(ItemStack itemStack);

    void setHorseStanding(boolean standing);

    void setGuardianTarget(int targetId);

    void sendEntityMetadataPacket();

    void sendEntityEquipmentPacket();

    void sendEntityEventPacket(byte eventId);

    void sendSwingAnimationPacket(boolean mainHand);

    void addCollision(Player player);

    void removeCollision(Player player);

    void removePathfinder();

    int getId();

    @Nullable
    Entity getBukkitEntity();

    EntityTracker getTracker();
}
