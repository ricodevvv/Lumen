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

import org.bukkit.Location;
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
import com.ricodev.lumen.util.structure.type.ParentBlockDisplayStructureImpl;

public class HotAirBalloon implements GadgetBehavior {

    private static final double MAX_SPEED = 0.075d;
    private static final double ACCELERATION = 0.001d;
    private static final float HEIGHT_OFFSET = 1.3f;

    private ParentBlockDisplayStructureImpl structure;
    private Location seatLocation;
    private Entity seat;
    private double speed;

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

        Sounds.playAt(seatLocation, XSound.ENTITY_WITHER_SHOOT, 0.3f, 0.5f);

        context.getUser().setFallDamageProtection((int) context.getType().getDuration());

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        context.getUser().setFallDamageProtection((int) (context.getType().getDuration() + 6));
        speed = 0.0d;
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (seat == null) {
            return;
        }
        if (speed < MAX_SPEED) {
            speed += ACCELERATION;
        }
        Compats.get().teleportWithPassengers(seat, seatLocation.add(0.0d, speed, 0.0d));
        structure.follow();
        seatLocation.add(0.0d, 3.0d, 0.0d);
        Particles.spawn(seatLocation.getWorld(), LumenParticle.LARGE_SMOKE,
                seatLocation,
                0,
                0.0d,
                speed,
                0.0d,
                2.0d
        );
        seatLocation.subtract(0.0d, 3.0d, 0.0d);
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();

        if (seatLocation != null) {
            Sounds.playAt(seatLocation, XSound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.3f, 1.0f);
            Particles.spawn(seatLocation.getWorld(), LumenParticle.EXPLOSION_EMITTER, seatLocation.add(0.0d, 5.0d, 0.0d), 1);
            seatLocation = null;
        }

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
