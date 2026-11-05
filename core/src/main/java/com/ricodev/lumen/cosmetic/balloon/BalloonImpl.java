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
package com.ricodev.lumen.cosmetic.balloon;

import org.bukkit.inventory.ItemStack;

import com.ricodev.lumen.util.item.Heads;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.ServerVersion;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.entity.Entities;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.balloon.Balloon;
import com.ricodev.lumen.api.cosmetic.balloon.BalloonBehavior;
import com.ricodev.lumen.api.cosmetic.balloon.BalloonType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticImpl;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MathUtil;

import java.util.Random;

public class BalloonImpl extends CosmeticImpl<BalloonType, BalloonBehavior> implements Balloon {

    private static final Random RANDOM = new Random();
    private static final float DEGREES_180 = FastMathUtil.toRadians(180.0f);

    private static final Vector OFFSET_VECTOR = new Vector(-0.8d, 0.0d, 0.2d);
    private static final double LEASH_LENGTH = 2.5d;
    private static final double Y_INERTIA = 0.1d;
    private static final double INERTIA_DECAY = 0.95d;
    private static final float ROTATION_DECAY = 0.98f;
    private static final float MIN_ROTATION_SPEED = 1.0f;
    private static final float ROTATION_MULTIPLIER = 1.0f;

    private static final double HEAD_PIVOT_HEIGHT = 1.5d;
    private static final double LEASH_KNOT_HEIGHT = ServerVersion.atLeast(13) ? 0.26d : 0.75d;

    private static final double WIND_STRENGTH = 0.01d;
    private static final double TURBULENCE_FREQUENCY = 0.08d;

    private Entity entity;
    private ArmorStand itemHolder;
    private LivingEntity leashEntity;

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private final Location targetLocation = new Location(null, 0.0d, 0.0d, 0.0d);
    private final Vector offsetVector = OFFSET_VECTOR.clone();
    private final Vector inertia = new Vector(0.0d, 0.2d, 0.0d);
    private final Vector temp = new Vector(0.0d, 0.0d, 0.0d);
    private final Vector windDirection = new Vector(1.0d, 0.0d, 0.3d);
    private final Vector windForce = new Vector();

    private float rotationSpeed = RANDOM.nextFloat();
    private double windPhase = RANDOM.nextDouble() * Math.PI * 2;
    private double angle = 0.0;

    public BalloonImpl(LumenPlugin plugin, User user, BalloonType type, BalloonBehavior behavior) {
        super(plugin, user, type, behavior);
    }

    @Override
    protected void onEquip() {
        updateLocation();
        LocationUtil.copy(targetLocation, location);
        spawnEntities();
        runTaskTimer(plugin, 5L, 1L);
    }

    @Override
    protected void onUpdate() {
        if (entity == null || !entity.isValid() || !entity.getWorld().equals(player.getWorld())) {
            removeEntities();
            updateLocation();
            LocationUtil.copy(targetLocation, location);
            spawnEntities();
            return;
        }
        updatePhysics();

        if (itemHolder != null) {
            itemHolder.teleport(holderLocation());
        } else {
            entity.teleport(location);
        }
        if (leashEntity != null) {
            leashEntity.teleport(leashLocation());
        }
        behavior.onUpdate(this, location);
    }

    @Override
    protected void onUnequip() {
        spawnPopEffects();
        removeEntities();
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Entity getLeashEntity() {
        return leashEntity;
    }

    /**
     * Mob balloons float the mob itself on a leash. Item and block balloons (display entities on
     * newer versions) become an invisible armor stand wearing the item, plus an invisible baby
     * rabbit that holds the leash, which is how it's always been done on 1.8.
     */
    private void spawnEntities() {
        Class<? extends Entity> type = cosmeticType.getEntityType() == null ? null : cosmeticType.getEntityType().getEntityClass();

        if (type != null && LivingEntity.class.isAssignableFrom(type) && !ArmorStand.class.isAssignableFrom(type)) {
            LivingEntity mob = (LivingEntity) Entities.spawn(location, type, spawned -> {
                Entities.makeCosmetic(spawned);
                Compats.get().setGravity(spawned, false);
            });
            Compats.get().setScale(mob, cosmeticType.getScale());

            if (cosmeticType.isBaby() && mob instanceof Ageable) {
                ((Ageable) mob).setBaby();
                ((Ageable) mob).setAgeLock(true);
            } else if (cosmeticType.isBaby() && mob instanceof Zombie) {
                ((Zombie) mob).setBaby(true);
            }
            mob.setLeashHolder(player);
            entity = mob;
            return;
        }
        itemHolder = Entities.spawn(holderLocation(), ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.setHelmet(headItem());
            MetadataUtil.setCustomEntity(stand);
        });
        Compats.get().setScale(itemHolder, cosmeticType.getScale());
        entity = itemHolder;

        leashEntity = Entities.spawn(leashLocation(), Rabbit.class, rabbit -> {
            Entities.makeCosmetic(rabbit);
            Compats.get().setGravity(rabbit, false);
            rabbit.setBaby();
            rabbit.setAgeLock(true);
            rabbit.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        });
        leashEntity.setLeashHolder(player);
    }

    /**
     * Where the armor stand has to stand so the head it wears starts right at the balloon's
     * location. An armor stand's head starts {@link #HEAD_PIVOT_HEIGHT} above its feet, scaled with it on 1.20.5+.
     */
    private Location holderLocation() {
        double scale = ServerVersion.atLeast(20, 5) ? cosmeticType.getScale() : 1.0d;
        return location.clone().add(0.0d, -HEAD_PIVOT_HEIGHT * scale, 0.0d);
    }

    /**
     * The client draws the leash a bit above the rabbit holding it, so the rabbit goes that much
     * lower and the rope ends right under the balloon. Old clients tie it about 0.75 up, new ones
     * at the baby rabbit's eyes.
     */
    private Location leashLocation() {
        return location.clone().add(0.0d, -LEASH_KNOT_HEIGHT, 0.0d);
    }

    /**
     * The cake doesn't work as a hat: before 1.13 an armor stand doesn't draw it at all, and after
     * that it floats off the head like any other item. A cake head sits where it should everywhere.
     */
    private ItemStack headItem() {
        ItemStack itemStack = getType().getItemStack();
        return XMaterial.CAKE.isSimilar(itemStack) ? Heads.CAKE.getSkull() : itemStack;
    }

    private void removeEntities() {
        if (leashEntity != null) {
            leashEntity.remove();
            leashEntity = null;
        }
        if (entity != null) {
            entity.remove();
            entity = null;
        }
        itemHolder = null;
    }

    private void updateLocation() {
        double xOffset = Compats.get().isLeftHanded(player) ? -OFFSET_VECTOR.getX() : OFFSET_VECTOR.getX();
        offsetVector.setX(xOffset);
        offsetVector.setY(OFFSET_VECTOR.getY());
        offsetVector.setZ(OFFSET_VECTOR.getZ());

        player.getLocation(targetLocation)
                .add(MathUtil.rotateAroundAxisY(offsetVector, -FastMathUtil.toRadians(targetLocation.getYaw())));
        targetLocation.setPitch(0.0f);

        location.setWorld(targetLocation.getWorld());
    }

    private void updatePhysics() {
        updateLocation();

        updateRotation();
        storePreviousPosition();
        applyInertia();
        applyWindAndTurbulence();
        updatePosition();
        constrainToLeashLength(targetLocation);
        calculateNewInertia();
        updateRotationFromMovement();
    }

    private void updateRotation() {
        rotationSpeed *= ROTATION_DECAY;

        if (Math.abs(rotationSpeed) < MIN_ROTATION_SPEED) {
            rotationSpeed = rotationSpeed >= 0.0f ? MIN_ROTATION_SPEED : -MIN_ROTATION_SPEED;
        }
        angle += rotationSpeed;
    }

    private void storePreviousPosition() {
        temp.setX(location.getX());
        temp.setY(location.getY());
        temp.setZ(location.getZ());
    }

    private void applyInertia() {
        inertia.multiply(INERTIA_DECAY);

        if (inertia.getY() < Y_INERTIA) {
            inertia.setY(Math.min(Y_INERTIA, inertia.getY() + Y_INERTIA));
        }
    }

    private void applyWindAndTurbulence() {
        windPhase += TURBULENCE_FREQUENCY;
        double windMagnitude = Math.sin(windPhase) * WIND_STRENGTH;

        windForce.setX(windDirection.getX() * windMagnitude);
        windForce.setY(windDirection.getY() * windMagnitude);
        windForce.setZ(windDirection.getZ() * windMagnitude);

        add(windForce,
                (RANDOM.nextDouble() - 0.5) * WIND_STRENGTH,
                (RANDOM.nextDouble() - 0.5) * WIND_STRENGTH,
                (RANDOM.nextDouble() - 0.5) * WIND_STRENGTH
        );
        inertia.add(windForce);
    }

    private void updatePosition() {
        location.add(inertia.getX(), inertia.getY(), inertia.getZ());
        location.setYaw((float) angle);
    }

    private void constrainToLeashLength(Location targetLocation) {
        double distance = targetLocation.distance(location);

        if (distance > LEASH_LENGTH) {
            double percentage = LEASH_LENGTH / distance;
            location.setX(linearInterpolate(targetLocation.getX(), location.getX(), percentage));
            location.setY(linearInterpolate(targetLocation.getY(), location.getY(), percentage));
            location.setZ(linearInterpolate(targetLocation.getZ(), location.getZ(), percentage));
        }
    }

    private void calculateNewInertia() {
        inertia.setX(location.getX() - temp.getX());
        inertia.setY(location.getY() - temp.getY());
        inertia.setZ(location.getZ() - temp.getZ());
    }

    private void updateRotationFromMovement() {
        rotationSpeed += (float) ((inertia.getX() - inertia.getZ()) * ROTATION_MULTIPLIER);
    }

    private void spawnPopEffects() {
        if (entity == null) {
            return;
        }
        Location effectLocation = location.clone().add(0.0d, 0.5d, 0.0d);

        Sounds.playAt(effectLocation, XSound.ENTITY_CHICKEN_EGG, 0.5f, 0.0f);
        Particles.spawn(effectLocation.getWorld(), LumenParticle.CLOUD, effectLocation, 10, 0.15f, 0.15f, 0.15f, 0.05f);
    }

    private static Vector add(Vector vector, double x, double y, double z) {
        vector.setX(vector.getX() + x);
        vector.setY(vector.getY() + y);
        vector.setZ(vector.getZ() + z);
        return vector;
    }

    private static Vector sub(Vector vector, double x, double y, double z) {
        return add(vector, -x, -y, -z);
    }

    private static double linearInterpolate(double y1, double y2, double mu) {
        return y1 == y2 ? y1 : y1 * (1 - mu) + y2 * mu;
    }
}
