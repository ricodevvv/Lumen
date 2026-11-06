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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.structure.type.BlockStructureImpl;

import java.util.List;

public class Slide implements GadgetBehavior, Listener {

    private BlockStructureImpl structure;
    private Location center;
    private double angle;
    private Location plate;
    private ArmorStand seat;
    private int ticks;
    private final Vector vector = new Vector();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (structure == null) {
            structure = new BlockStructureImpl(context.getType().getStructure());
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        center = context.getPlayer().getLocation();

        angle = structure.spawn(center);

        for (Block block : structure.getPlacedEntries()) {
            StructureBlock data = structure.getPlacedData(block);

            if (data != null && data.getMaterial().endsWith("_PRESSURE_PLATE")) {
                plate = block.getLocation();
                break;
            }
        }
        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (seat != null) {
            List<Entity> passengers = Compats.get().getPassengers(seat);

            if (passengers.isEmpty()) {
                seat.remove();
                seat = null;
                return;
            }
            double movementAngle = FastMathUtil.toRadians(ticks) * 4;
            double forward = -Math.abs(Math.sin(movementAngle)) * 4.5d + 6.0;
            double height = -Math.abs(Math.cos(movementAngle)) * 4.2d + 3.0d;

            vector.setX(0.0d).setY(height).setZ(forward);
            Compats.get().teleportWithPassengers(seat, center.clone().add(MathUtil.rotateAroundAxisY(vector, angle)));

            ticks++;

            if (ticks == 28) {
                Player player = context.getPlayer();
                Sounds.playAt(player, XSound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f);
            }

            if (ticks == 44) {
                Entity passenger = passengers.get(0);
                seat.eject();
                seat.remove();

                Vector force = center.getDirection().multiply(0.4d);
                force.setY(force.getY() + 0.4d);
                passenger.setVelocity(passenger.getVelocity().add(force));
                seat = null;
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();

        if (seat != null) {
            seat.eject();
            seat.remove();
            seat = null;
        }
        plate = null;
        center = null;
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
    public void onPlayerPlateStep(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();

            if (block != null && block.getLocation().equals(plate)) {
                Player player = event.getPlayer();

                if (seat == null && !player.isSneaking()) {
                    seat = Entities.spawn(center.clone().add(0.0d, 4.0d, 0.0d), ArmorStand.class, entity -> {
                        entity.setBasePlate(false);
                        entity.setVisible(false);
                        entity.setGravity(false);
                        Entities.makeCosmetic(entity);
                    });
                    Compats.get().addPassenger(seat, player);
                    ticks = 22;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPlateStep(EntityInteractEvent event) {
        if (event.getBlock().getLocation().equals(plate)) {
            event.setCancelled(true);
        }
    }
}
