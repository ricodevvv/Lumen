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
package com.ricodev.lumen.cosmetic.gadget.type;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class SoccerBall implements GadgetBehavior {

    private static final double DECREASE_MOVEMENT_MULTIPLIER = 0.8d;
    private static final double COLLISION = 0.3d;

    private Location location;
    private Slime slimeBall;
    private Vector ballVector = new Vector();
    private int kickTicks;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        if (slimeBall != null) {
            slimeBall.remove();
        }
        location = context.getPlayer().getLocation();
        location.setPitch(0.0f);
        location.add(location.getDirection().multiply(2.0d));

        Slime ball = Entities.spawn(location, Slime.class, entity -> {
            entity.setRemoveWhenFarAway(false);
            entity.setSize(2);

            Compats.get().setAI(entity, false);

            MetadataUtil.setCustomEntity(entity);
        });
        ballVector.zero();
        slimeBall = ball;

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (slimeBall == null) {
            return;
        }

        if (kickTicks-- < 0) {
            slimeBall.getLocation(location);
            Player kickPlayer = MathUtil.getClosestVisiblePlayerFeetFromLocation(context.getPlayer(), location, 1.3d);

            if (kickPlayer != null) {
                Sounds.playAt(location, XSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 1.5f);
                Particles.spawn(location.getWorld(), LumenParticle.ITEM_SLIME, location, 10, 0.5f, 0.5f, 0.5f, 0.0f);

                location = kickPlayer.getLocation(location);
                location.setPitch(0.0f);
                Vector vector = location.getDirection().multiply(1.5d);
                vector.setY(0.5d);

                ballVector = vector;
                slimeBall.setVelocity(vector);
                kickTicks = 8;
                return;
            }
        }
        Vector newVelocity = slimeBall.getVelocity();
        boolean collide = false;

        if (newVelocity.getX() == 0.0d) {
            newVelocity.setX(-ballVector.getX() * DECREASE_MOVEMENT_MULTIPLIER);

            if (Math.abs(ballVector.getX()) > COLLISION) {
                collide = true;
            }
        } else if (Math.abs(ballVector.getX() - newVelocity.getX()) < 0.1d) {
            newVelocity.setX(ballVector.getX() * 0.98d);
        }

        if (newVelocity.getZ() == 0.0d) {
            newVelocity.setZ(-ballVector.getZ() * DECREASE_MOVEMENT_MULTIPLIER);
            if (Math.abs(ballVector.getZ()) > COLLISION) {
                collide = true;
            }
        } else if (Math.abs(ballVector.getZ() - newVelocity.getZ()) < 0.1d) {
            newVelocity.setZ(ballVector.getZ() * 0.98d);
        }

        if (newVelocity.getX() != 0.0d && newVelocity.getY() != 0.0d && newVelocity.getZ() != 0.0d) {
            slimeBall.setVelocity(newVelocity);
        }
        ballVector = newVelocity;

        if (collide) {
            Sounds.playAt(slimeBall.getLocation(location), XSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 1.5f);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        if (slimeBall != null) {
            Sounds.playAt(location, XSound.ENTITY_CHICKEN_EGG, 0.5f, 0.0f);
            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location.add(0.0d, 1.0d, 0.0d), 10, 0.15f, 0.15f, 0.15f, 0.05f);

            slimeBall.remove();
            slimeBall = null;
        }
    }

    @Override
    public boolean requiresGroundOnUse() {
        return true;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return true;
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }
}
