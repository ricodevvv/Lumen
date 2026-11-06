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

import com.ricodev.lumen.util.Collects;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.entity.EntityTracker;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.util.structure.type.BlockStructure;
import com.ricodev.lumen.util.entity.EntityTrackerImpl;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.RGBFade;
import com.ricodev.lumen.util.entity.FloatingBlocks;
import com.ricodev.lumen.util.structure.type.BlockStructureImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MerryGoRound implements GadgetBehavior, Listener {

    private static final ItemStack SADDLE_ITEM = XMaterial.SADDLE.parseItem();
    private static final List<Horse.Color> HORSE_COLORS = Collects.listOf(Horse.Color.values());
    private static final int HORSES = 7;
    private static final double RANGE = 4.4d;
    private static final double ANGLE_PER_HORSE = 360.0d / HORSES;
    private static final double LEASH_Y_OFFSET = 6.0d;
    private static final float MAX_SPEED = 4.0f;
    private static final float ACCELERATION = 0.02f;

    private BlockStructure structure;
    private float tick;
    private final EntityTracker tracker = new EntityTrackerImpl();
    private final Map<Horse, CoasterHorse> coasterHorses = new LinkedHashMap<>();
    private final RGBFade rgbFade = new RGBFade();
    private Location center;
    private float speed;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (structure == null) {
            structure = new BlockStructureImpl(context.getType().getStructure());
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        center = player.getLocation().getBlock().getLocation().add(0.5d, 0.0d, 0.5d);
        World world = player.getWorld();
        speed = 0.0f;

        structure.spawn(center);

        for (int i = 0; i < HORSES; i++) {
            Location location = getHorseLocation(i);
            Location leashLoc = location.clone().add(0.0d, LEASH_Y_OFFSET, 0.0d);
            location.setY(getYOffset(i));

            int finalI = i;
            Horse horse = Entities.spawn(location, Horse.class, entity -> {
                entity.setColor(HORSE_COLORS.get(finalI % HORSE_COLORS.size()));
                entity.getInventory().setSaddle(SADDLE_ITEM);
                Entities.makeCosmetic(entity);
                Compats.get().setInvulnerable(entity, true);
                Compats.get().setPersistent(entity, false);
            });
            Entity seat = FloatingBlocks.seat(location);
            Compats.get().addPassenger(horse, seat);

            CosmeticEntity leash = CosmeticEntities.create(world, EntityType.BAT, tracker);
            leash.setPositionRotation(leashLoc);
            leash.setLeashHolder(horse);

            if (leash.getBukkitEntity() instanceof LivingEntity) {
                Compats.get().setInvisible((LivingEntity) leash.getBukkitEntity(), true);
            }
            coasterHorses.put(horse, new CoasterHorse(horse, seat, leash));
        }
        tracker.startTracking();

        List<Player> nearbyPlayers = MathUtil.getClosestPlayersFromLocation(center, 6.0d);

        if (!nearbyPlayers.isEmpty()) {
            Location teleport = center.clone().add(4.5d, 1.5d, 0.0d);
            teleport.setDirection(player.getLocation().getDirection());

            for (Player closePlayer : nearbyPlayers) {
                closePlayer.teleport(teleport);
            }
        }
        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    private Location getHorseLocation(float angle) {
        Location location = MathUtil.getLocationAroundCircle(center, RANGE, angle);
        location.setYaw(location.getYaw() - 90.0f);
        return location;
    }

    private double getYOffset(float angle) {
        double y = FastMathUtil.sin(angle * 2.0f) + 2.0d;
        return center.getY() + y;
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (center == null) {
            return;
        }
        int i = 0;

        for (CoasterHorse coasterHorse : coasterHorses.values()) {
            float angle = FastMathUtil.toRadians(ANGLE_PER_HORSE * i + tick);
            Location location = getHorseLocation(angle);

            location.add(0.0d, LEASH_Y_OFFSET, 0.0d);
            coasterHorse.leash.sendPositionRotationPacket(location);
            location.subtract(0.0d, LEASH_Y_OFFSET, 0.0d);

            location.setY(getYOffset(angle));
            Compats.get().teleportWithPassengers(coasterHorse.horse, location);

            Particles.spawn(location.getWorld(), LumenParticle.DUST, location.add(0.0d, 1.0d, 0.0d), 5, 0, 0, 0, 0.0d,
                    new Particles.Dust(org.bukkit.Color.fromRGB(rgbFade.getR(), rgbFade.getG(), rgbFade.getB()), 1)
            );
            i++;
        }

        if (speed < MAX_SPEED) {
            speed += ACCELERATION;
        }
        rgbFade.nextRGB();

        tick += speed;

        if (tick > 360) {
            tick = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();

        tracker.destroy();

        for (CoasterHorse coasterHorse : coasterHorses.values()) {
            FloatingBlocks.removeSeat(coasterHorse.seat);
            coasterHorse.horse.remove();
        }
        coasterHorses.clear();
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
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse) {
            CoasterHorse coasterHorse = coasterHorses.get((Horse) event.getRightClicked());

            if (coasterHorse != null) {
                event.setCancelled(true);

                if (Compats.get().getPassengers(coasterHorse.seat).isEmpty()) {
                    Compats.get().addPassenger(coasterHorse.seat, event.getPlayer());
                }
            }
        }
    }

    private static final class CoasterHorse {

        private final Horse horse;
        private final Entity seat;
        private final CosmeticEntity leash;

        CoasterHorse(Horse horse, Entity seat, CosmeticEntity leash) {
            this.horse = horse;
            this.seat = seat;
            this.leash = leash;
        }
    }
}
