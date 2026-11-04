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
package com.ricodev.lumen.cosmetic.miniature;

import com.ricodev.lumen.compat.Compats;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import com.ricodev.lumen.util.entity.CosmeticEntities;

import org.bukkit.Location;
import org.bukkit.entity.*;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.miniature.Miniature;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureBehavior;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticImpl;
import com.ricodev.lumen.util.FastMathUtil;

public class MiniatureImpl extends CosmeticImpl<MiniatureType, MiniatureBehavior> implements Miniature {

    private static final double MAX_DISTANCE_SQUARED_BEFORE_TELEPORT = 256.0d;
    private static final double DISTANCE_SQUARED_BEFORE_MOVE_TOWARDS_PLAYER = 3.5d;
    private static final double ACCELERATION = 0.15d;

    protected CosmeticEntity entity;
    protected int tick;
    private double currentSpeedX;
    private double currentSpeedZ;

    private final Location playerLocation = new Location(null, 0.0d, 0.0d, 0.0d);
    private final Location entityLocation = new Location(null, 0.0d, 0.0d, 0.0d);

    public MiniatureImpl(LumenPlugin plugin, User user, MiniatureType type, MiniatureBehavior behavior) {
        super(plugin, user, type, behavior);
    }

    @Override
    protected void onEquip() {
        spawnEntity();
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void onUpdate() {
        player.getLocation(playerLocation);
        entity.getPreviousLocation(entityLocation);

        if (entityLocation.getWorld() != playerLocation.getWorld()) {
            return;
        }
        double distanceSquared = playerLocation.distanceSquared(entityLocation);

        if (distanceSquared > MAX_DISTANCE_SQUARED_BEFORE_TELEPORT) {
            entity.sendPositionRotationPacket(playerLocation);
            return;
        }
        double yOffset = 0.3d * FastMathUtil.cos(FastMathUtil.toRadians(tick * 6));
        double y = playerLocation.getY() + 0.8d + yOffset;
        entityLocation.setY(y);

        entityLocation.setDirection(playerLocation.add(0.0d, 0.8d, 0.0d)
                .subtract(entityLocation.getX(), entityLocation.getY(), entityLocation.getZ())
                .toVector()
                .normalize());

        if (distanceSquared > DISTANCE_SQUARED_BEFORE_MOVE_TOWARDS_PLAYER) {
            float angle = FastMathUtil.toRadians(entityLocation.getYaw());
            double targetSpeed = distanceSquared * 0.02d;
            double targetX = -FastMathUtil.sin(angle) * targetSpeed;
            double targetZ = FastMathUtil.cos(angle) * targetSpeed;

            currentSpeedX += (targetX - currentSpeedX) * ACCELERATION;
            currentSpeedZ += (targetZ - currentSpeedZ) * ACCELERATION;
        } else {
            currentSpeedX *= (1.0d - ACCELERATION);
            currentSpeedZ *= (1.0d - ACCELERATION);
        }
        entityLocation.add(currentSpeedX, 0.0d, currentSpeedZ);
        entity.sendPositionRotationPacket(entityLocation);
        entity.setHeadPose(entityLocation.getPitch(), 0.0f, 0.0f);
        entity.sendEntityMetadataPacket();

        if (++tick >= 360) {
            tick = 0;
        }
        behavior.onUpdate(this, entity, tick);
    }

    @Override
    protected void onUnequip() {
        if (entity != null) {
            entity.getTracker().destroy();
            entity = null;
        }
    }

    /**
     * Item display miniatures become an invisible armor stand wearing the item, since display
     * entities only exist on 1.19.4+.
     */
    private void spawnEntity() {
        EntityType type = cosmeticType.getEntityType();
        boolean asItem = type == null || !LivingEntity.class.isAssignableFrom(type.getEntityClass());

        entity = CosmeticEntities.create(player.getWorld(), asItem ? EntityType.ARMOR_STAND : type);
        entity.setPositionRotation(getSpawnLocation());
        Entity bukkitEntity = entity.getBukkitEntity();

        if (bukkitEntity instanceof LivingEntity) {
            Compats.get().setScale((LivingEntity) bukkitEntity, cosmeticType.getScale());
        }
        if (cosmeticType.isBaby() && bukkitEntity instanceof Ageable) {
            ((Ageable) bukkitEntity).setBaby();
        }
        if (bukkitEntity instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) bukkitEntity;
            armorStand.setBasePlate(false);
            armorStand.setSmall(cosmeticType.isBaby() || asItem);
            armorStand.setArms(!asItem);
            armorStand.setVisible(!asItem);
            entity.setHelmet(cosmeticType.getItemStack());
        }
        behavior.setupEntity(this, entity);
        entity.getTracker().setOwner(player);
        entity.getTracker().startTracking();
    }

    private Location getSpawnLocation() {
        player.getLocation(playerLocation).add(0.0d, 0.5d, 0.0d).add(playerLocation.getDirection().multiply(1.5d));
        playerLocation.setDirection(player.getLocation()
                .subtract(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ())
                .toVector()
                .normalize());
        return playerLocation;
    }

    @Override
    public CosmeticEntity getCosmeticEntity() {
        return entity;
    }
}
