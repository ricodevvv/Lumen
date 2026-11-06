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


import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.entity.FloatingBlocks;
import com.ricodev.lumen.util.material.Materials;
import com.ricodev.lumen.util.structure.type.ParentBlockDisplayStructureImpl;

public class Rocket implements GadgetBehavior {

    private static final double Y_COLLISION_CHECK = 10.0d;

    private static final double MAX_SPEED = 0.5d;
    private static final double ACCELERATION = 0.01d;
    private static final float HEIGHT_OFFSET = 3.5f;

    private ParentBlockDisplayStructureImpl structure;
    private Entity seat;
    private boolean launching;
    private int tick;
    private double speed;
    private Location seatLocation;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (structure == null) {
            structure = new ParentBlockDisplayStructureImpl(context.getType().getStructure());
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        Location center = player.getLocation();

        seatLocation = center.clone().add(0.0d, HEIGHT_OFFSET, 0.0d);

        seat = FloatingBlocks.seat(seatLocation);
        Compats.get().addPassenger(seat, player);
        structure.spawn(center, seat, HEIGHT_OFFSET);

        context.getUser().setFallDamageProtection((int) (context.getType().getDuration() + 6));

        speed = 0.0d;
        tick = 0;
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (seat == null) {
            return;
        }
        World world = seatLocation.getWorld();

        if (launching) {
            if (speed < MAX_SPEED) {
                speed += ACCELERATION;
            }
            Compats.get().teleportWithPassengers(seat, seatLocation.add(0.0d, speed, 0.0d));
            structure.follow();
            seatLocation.subtract(0.0d, HEIGHT_OFFSET, 0.0d);
            Particles.spawn(world, LumenParticle.FLAME, seatLocation, 10, 0.2f, 0.2f, 0.2f, 0.0d);
            Particles.spawn(world, LumenParticle.CLOUD, seatLocation, 10, 0.2f, 0.2f, 0.2f, 0.0d);
            Particles.spawn(world, LumenParticle.EXPLOSION, seatLocation, 1, 0.2f, 0.2f, 0.2f, 0.0d);
            seatLocation.add(0.0d, HEIGHT_OFFSET, 0.0d);
            Sounds.playAt(seatLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 0.0f);

            if (tick > 220 && tick < 240) {
                Sounds.playAt(seatLocation, XSound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.4f, 0.0f);
                Sounds.playAt(seatLocation, XSound.BLOCK_ANVIL_LAND, 0.4f, 0.0f);
            } else if (tick == 240) {
                explode(context, seatLocation);
            }
            seatLocation.add(0.0d, Y_COLLISION_CHECK, 0.0d);
            if (!Materials.isAir(seatLocation.getBlock().getType())) {
                explode(context, seatLocation);
                return;
            }
            seatLocation.subtract(0.0d, Y_COLLISION_CHECK, 0.0d);
        } else {
            Sounds.playAt(seatLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.1f, 0.0f);

            if (tick % 20 == 0) {
                seatLocation.subtract(0.0d, HEIGHT_OFFSET, 0.0d);
                Particles.spawn(world, LumenParticle.CLOUD, seatLocation, 10, 0.2f, 0.2f, 0.2f, 0.0d);
                seatLocation.add(0.0d, HEIGHT_OFFSET, 0.0d);

                if (tick == 100) {
                    launching = true;
                    Sounds.playAt(seatLocation, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.0f);
                    Sounds.playAt(seatLocation, XSound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.0f);
                }
            }
        }
        tick++;
    }

    private void explode(CosmeticContext<GadgetType> context, Location location) {
        onUnequip(context);
        Sounds.playAt(location, XSound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.0f);
        Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION_EMITTER, location, 0);
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();
        launching = false;

        FloatingBlocks.removeSeat(seat);
        seat = null;
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
}
