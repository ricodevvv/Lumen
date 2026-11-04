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

import com.ricodev.lumen.util.entity.Entities;

import org.bukkit.entity.Chicken;
import java.util.ArrayList;
import java.util.List;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.util.material.Materials;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;

public class ChickenParachute implements GadgetBehavior {

    private static final XMaterial BLOCK_DATA = XMaterial.GRAVEL;
    private static final Vector LEAP_FORCE = new Vector(0.0d, 0.3d, 0.0d);
    private static final Vector UP_FORCE = new Vector(0.0d, 0.1d, 0.0d);

    private static final double PARACHUTE_OFFSET = 7.0d;
    private static final int PARACHUTE_START_HEIGHT = 30;
    private static final double PARACHUTE_SPREAD = 1.5d;
    private static final int CHICKENS = 15;

    private final List<Chicken> chickens = new ArrayList<>();
    private final List<Vector> offsets = new ArrayList<>();
    private boolean parachuting;
    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        User user = context.getUser();

        user.setFallDamageProtection(15);

        Location location = player.getLocation().add(0.0d, PARACHUTE_START_HEIGHT, 0.0d);
        player.teleport(location);
        player.setVelocity(new Vector());
        location.add(0.0d, PARACHUTE_OFFSET, 0.0d);
        location.setPitch(0.0f);
        this.location = location.clone();

        for (int i = 0; i < CHICKENS; i++) {
            Vector offset = new Vector(
                    MathUtil.randomRange(-PARACHUTE_SPREAD, PARACHUTE_SPREAD),
                    0.0d,
                    MathUtil.randomRange(-PARACHUTE_SPREAD, PARACHUTE_SPREAD)
            );
            Chicken chicken = Entities.spawn(location.clone().add(offset), Chicken.class, Entities::makeCosmetic);
            chicken.setLeashHolder(player);
            chickens.add(chicken);
            offsets.add(offset);
        }

        if (player.isFlying()) {
            player.setFlying(false);
        }
        Sounds.playAt(location, XSound.ENTITY_HORSE_ARMOR, 1.0f, 1.0f);

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> parachuting = true,
                5L
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (parachuting) {
            Player player = context.getPlayer();

            player.getLocation(location);

            if (!isInAir(location) || player.isFlying()) {
                location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.GRAVEL);
                Particles.spawn(location.getWorld(), LumenParticle.BLOCK, location.add(0.0d, -0.1d, 0.0d), 0, 0, 0, 0, 0.5d, BLOCK_DATA);
                player.setVelocity(LEAP_FORCE);

                onUnequip(context);
                return;
            }
            if (player.getVelocity().getY() < -0.2d) {
                player.setVelocity(player.getVelocity().add(UP_FORCE));
            }
            Particles.spawn(location.getWorld(), LumenParticle.BLOCK, location, 0, 0, 0, 0, 0.5d, BLOCK_DATA);
            for (int i = 0; i < chickens.size(); i++) {
                Chicken chicken = chickens.get(i);
                Location chickenLocation = location.clone().add(offsets.get(i)).add(0.0d, PARACHUTE_OFFSET, 0.0d);
                chickenLocation.setYaw(chicken.getLocation().getYaw());
                chicken.teleport(chickenLocation);
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        for (Chicken chicken : chickens) {
            chicken.setLeashHolder(null);
            chicken.remove();
        }
        chickens.clear();
        offsets.clear();
        parachuting = false;
    }

    @Override
    public boolean requiresGroundOnUse() {
        return false;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        for (int i = 0; i < PARACHUTE_START_HEIGHT; i++) {
            location.add(0.0d, 1.0d, 0.0d);

            if (!Materials.isAir(location.getBlock().getType())) {
                return false;
            }
        }
        location.subtract(0.0d, PARACHUTE_START_HEIGHT * 1.0d, 0.0d);
        return true;
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }

    private static boolean isInAir(Location location) {
        return location.getBlock().getRelative(BlockFace.DOWN).getType().name().endsWith("AIR");
    }
}
