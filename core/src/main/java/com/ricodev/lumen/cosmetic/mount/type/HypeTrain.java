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
package com.ricodev.lumen.cosmetic.mount.type;

import com.cryptomorin.xseries.XSound;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.Sounds;
import com.ricodev.lumen.util.entity.FloatingBlock;
import com.ricodev.lumen.util.entity.FloatingBlocks;
import com.ricodev.lumen.util.material.Materials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class HypeTrain implements MountBehavior, Listener {

    private static final StructureBlock ENGINE_BLOCK = new StructureBlock("minecraft:diamond_block");
    private static final float SCALE = 0.6f;

    private final List<Carriage> carriages = new ArrayList<>();
    private int ticks;

    private static Vector getTrajectory2d(Location from, Location to) {
        return to.clone().subtract(from).toVector().normalize();
    }

    private Player player;

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
        player = context.getPlayer();
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setBasePlate(false);
            stand.setCustomNameVisible(false);
        }
        entity.teleport(entity.getLocation().add(0.0d, 1.5d, 0.0d));
        carriages.add(new Carriage(entity, ENGINE_BLOCK, context.getPlayer()));
    }

    @Override
    public void postSetupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Compats.get().addPassenger(entity, context.getPlayer());
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();
        Location location = entity.getLocation().subtract(0.0d, 0.5d, 0.0d);

        if (location.getBlock().getType().isSolid()) {
            entity.eject();
            Vector vector = player.getLocation(location).getDirection().multiply(0.3d);
            player.teleport(location.subtract(vector));
            Sounds.playAt(location, XSound.ENTITY_CHICKEN_EGG, 0.8f, 1.0f);
            dismountDriver(context);
            return;
        }
        if (Compats.get().getPassengers(entity).isEmpty()) {
            dismountDriver(context);
            return;
        }
        Location before = null;
        Iterator<Carriage> iterator = carriages.iterator();

        while (iterator.hasNext()) {
            Carriage carriage = iterator.next();
            Location tail = carriage.seat.getLocation();

            if (before != null) {
                if (!carriage.rider.isOnline() || Compats.get().getPassengers(carriage.seat).isEmpty()) {
                    carriage.remove();
                    iterator.remove();
                    continue;
                }
                Location tp = before.clone().add(getTrajectory2d(before, tail));
                tp.setDirection(before.getDirection());
                carriage.moveTo(tp);
            } else {
                Vector forward = carriage.rider.getLocation().getDirection();
                tail.setDirection(forward);
                carriage.moveTo(tail.add(forward.multiply(context.getType().getMovementSpeed())));
            }
            before = carriage.seat.getLocation();
            before.setDirection(tail.getDirection());

            if (ticks % 20 == 0 && carriage.rider.isOnline()) {
                User user = context.getPlugin().getUserManager().getConnected(carriage.rider);

                if (user != null) {
                    user.sendActionBar(Component.text(context.getType().getName(user) + ": " + carriages.size(), NamedTextColor.YELLOW));
                }
            }
        }
        if (++ticks > 360) {
            ticks = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        context.getUser().setFallDamageProtection(10);
        clearTrain();
    }

    @EventHandler
    public void onClickedCarriage(PlayerInteractAtEntityEvent event) {
        Player clicker = event.getPlayer();

        if (clicker.isInsideVehicle() || clicker.isSneaking() || carriages.isEmpty()) {
            return;
        }
        Entity clickedEntity = event.getRightClicked();

        if (clickedEntity == player || isCarriage(clickedEntity)) {
            spawnCarriage(clicker);
        }
    }

    private boolean isCarriage(Entity entity) {
        for (Carriage carriage : carriages) {
            if (carriage.seat == entity || carriage.block.getEntity() == entity) {
                return true;
            }
        }
        return false;
    }

    private void dismountDriver(CosmeticContext<MountType> context) {
        context.getUser().removeCosmetic(context.getType().getCategory(), false, true);
    }

    private void spawnCarriage(Player target) {
        int carriageAmount = carriages.size();
        Location tail = carriages.get(carriageAmount - 1).seat.getLocation();

        if (carriageAmount > 1) {
            tail.add(getTrajectory2d(
                    carriages.get(carriageAmount - 2).seat.getLocation(),
                    carriages.get(carriageAmount - 1).seat.getLocation()
            ));
        } else {
            tail.subtract(player.getLocation().getDirection().setY(0));
        }
        Entity seat = FloatingBlocks.seat(tail);
        StructureBlock wool = new StructureBlock("minecraft:" + Materials.getRandomWool().name().toLowerCase(Locale.ROOT));
        Carriage carriage = new Carriage(seat, wool, target);

        Sounds.playAt(tail, XSound.ENTITY_HORSE_SADDLE, 0.5f, 1.0f);
        Compats.get().addPassenger(seat, target);
        carriages.add(carriage);
    }

    public void clearTrain() {
        for (int i = 0; i < carriages.size(); i++) {
            Carriage carriage = carriages.get(i);
            Location location = carriage.seat.getLocation();
            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location, 3, 0.0d, 0.0d, 0.0d, 0.0d);

            if (i == 0) {
                FloatingBlocks.remove(carriage.block);
            } else {
                carriage.remove();
            }
        }
        carriages.clear();
    }

    private static final class Carriage {

        private final Entity seat;
        private final FloatingBlock block;
        private final Player rider;

        Carriage(Entity seat, StructureBlock block, Player rider) {
            this.seat = seat;
            this.rider = rider;
            Location location = seat.getLocation().subtract(0.0d, FloatingBlocks.size(SCALE), 0.0d);
            this.block = FloatingBlocks.block(location, block, SCALE, seat);
        }

        void moveTo(Location location) {
            Compats.get().teleportWithPassengers(seat, location);
            block.follow();
            block.setRotation(location.getYaw(), location.getPitch());
        }

        void remove() {
            FloatingBlocks.remove(block);
            FloatingBlocks.removeSeat(seat);
        }
    }
}
