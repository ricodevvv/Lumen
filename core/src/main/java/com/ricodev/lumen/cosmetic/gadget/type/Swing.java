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

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.Entities;

import com.cryptomorin.xseries.XMaterial;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.util.structure.type.BlockDisplayStructure;
import com.ricodev.lumen.util.entity.EntityTrackerImpl;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.LocationUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.structure.type.BlockDisplayStructureImpl;

public class Swing implements GadgetBehavior, Listener {

    private static final ItemStack SEAT_ITEM = XMaterial.SPRUCE_SLAB.parseItem();

    private static final double ACCELERATION = 0.01d;
    private static final double MAX_ACCELERATION = 1.0d;

    private BlockDisplayStructure structure;
    private Location center;
    private double angle;
    private ArmorStand seat;
    private int ticks;
    private double amplifier;

    private final EntityTrackerImpl tracker = new EntityTrackerImpl();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (structure == null) {
            structure = new BlockDisplayStructureImpl(context.getType().getStructure());
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        center = LocationUtil.center(player.getLocation());
        angle = structure.spawn(center);

        seat = Entities.spawn(center, ArmorStand.class, entity -> {
            entity.setHelmet(SEAT_ITEM);
            entity.setBasePlate(false);
            entity.setVisible(false);
            entity.setGravity(false);
            Entities.makeCosmetic(entity);
        });
        Compats.get().addPassenger(seat, player);

        for (int i = 0; i < 2; i++) {
            Vector vector = MathUtil.rotateAroundAxisY(new Vector(-0.8d + i * 1.6d, 4.0d, 0.0d), angle);
            Location location = center.clone().add(vector);

            CosmeticEntity rabbit = CosmeticEntities.create(center.getWorld(), EntityType.RABBIT, tracker);
            rabbit.setPositionRotation(location);
            rabbit.setLeashHolder(seat);

            if (rabbit.getBukkitEntity() instanceof Ageable) {
                Ageable ageable = (Ageable) rabbit.getBukkitEntity();
                ageable.setBaby();
                Compats.get().setInvisible(ageable, true);
            }
        }
        tracker.startTracking();

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (seat != null) {
            if (Compats.get().getPassengers(seat).isEmpty()) {
                if (amplifier > 0.0f) {
                    amplifier -= ACCELERATION;
                }
            } else {
                if (amplifier < MAX_ACCELERATION) {
                    amplifier += ACCELERATION;
                }
            }
            if (amplifier < 0.0f) {
                return;
            }
            float tickRadians = FastMathUtil.toRadians(ticks * 8.0f);
            float cosValue = FastMathUtil.cos(tickRadians);

            double forward = 3.0f * amplifier * FastMathUtil.sin(tickRadians);
            double height = (2.5f - 2.5f * Math.abs(cosValue)) * amplifier - 1.0f;

            Vector vector = MathUtil.rotateAroundAxisY(new Vector(0.0f, height, forward), angle);
            Compats.get().teleportWithPassengers(seat, center.clone().add(vector));
            seat.setHeadPose(new EulerAngle(-forward * 0.1d, 0.0d, 0.0d));

            ticks++;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();
        amplifier = 0.0f;

        tracker.destroy();

        if (seat != null) {
            seat.eject();
            seat.remove();
            seat = null;
        }
    }

    @Override
    public boolean requiresGroundOnUse() {
        return true;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return structure.isEnoughSpace(location);
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }

    @EventHandler
    public void onClickSeat(PlayerInteractAtEntityEvent event) {
        if (seat != null && event.getRightClicked() == seat && Compats.get().getPassengers(seat).isEmpty()) {
            Compats.get().addPassenger(seat, event.getPlayer());
        }
    }
}
