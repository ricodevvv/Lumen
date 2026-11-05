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
package com.ricodev.lumen.cosmetic.mount;

import org.bukkit.event.Event;
import com.ricodev.lumen.compat.Events;
import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.Entities;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.mount.Mount;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.event.CosmeticEntitySpawnEvent;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticImpl;
import com.ricodev.lumen.util.MetadataUtil;

public class MountImpl extends CosmeticImpl<MountType, MountBehavior> implements Mount {

    private final boolean rideOnSpawn;
    private final boolean despawnOnDismount;

    protected CosmeticEntity nmsEntity;
    protected Entity entity;

    public MountImpl(LumenPlugin plugin, User user, MountType type, MountBehavior behavior) {
        super(plugin, user, type, behavior);

        Config config = type.getCategory().getConfig();

        this.rideOnSpawn = config.getBoolean("ride_on_spawn");
        this.despawnOnDismount = config.getBoolean("despawn_on_dismount");
    }

    @Override
    protected void onEquip() {
        Events.listenFirst(plugin, this, this::onDismount,
                "org.bukkit.event.entity.EntityDismountEvent", "org.spigotmc.event.entity.EntityDismountEvent");
        spawn();

        if (rideOnSpawn && entity.isValid()) {
            Compats.get().addPassenger(entity, player);
        }
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    protected void onUpdate() {
        if (nmsEntity != null && entity.isValid()) {
            behavior.onUpdate(this, entity, nmsEntity);

            if (entity instanceof LivingEntity && player.getVehicle() != entity && user.isMoving()) {
                nmsEntity.follow(player);
            }
        }
    }

    @Override
    protected void onUnequip() {
        if (nmsEntity != null) {
            nmsEntity = null;
        }

        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Entity clickedEntity = event.getRightClicked();

        if (clickedEntity != entity) {
            return;
        }
        Player clicker = event.getPlayer();

        if (clicker.equals(player)) {
            if (!player.isInsideVehicle()) {
                Compats.get().addPassenger(entity, player);
            }
        } else {
            User clickUser = plugin.getUserManager().getConnected(clicker);

            if (clickUser != null) {
                clickUser.sendMessage(clickUser.translate("cosmetic.mounts.equip.not_owner"));
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FALL && event.getEntity() == player
                && player.getVehicle() == entity) {
            event.setCancelled(true);
        }
    }




    private void onDismount(Event event) {
        Entity dismounted = Events.entity(event, "getDismounted");

        if (dismounted != null && dismounted == entity && despawnOnDismount && !Compats.get().isRepositioning(entity)) {
            user.removeCosmetic(cosmeticType.getCategory(), false, true);
        }
    }

    @Override
    public void spawn() {
        spawn(player.getLocation());
    }

    @Override
    public void spawn(Location location) {
        if (entity != null) {
            behavior.onUnequip(this);
            onUnequip();
        }
        entity = Entities.spawn(location, cosmeticType.getEntityType().getEntityClass(), entity -> {
            plugin.getPlatformAdapter().setCustomName(entity, user.translate(
                    "cosmetic.mounts.name_tag",
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.unparsed("cosmetic", cosmeticType.getName(user))));

            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Compats.get().setMaxHealth(livingEntity, 2.0d);
                Compats.get().setMovementSpeed(livingEntity, cosmeticType.getMovementSpeed());
            }
            nmsEntity = CosmeticEntities.wrap(entity);
            MetadataUtil.setCustomEntity(entity);
            behavior.setupEntity(this, entity, nmsEntity);
        });

        if (!entity.isValid()) {
            unequip(false, false);
            return;
        }
        behavior.postSetupEntity(this, entity, nmsEntity);

        CosmeticEntitySpawnEvent event = new CosmeticEntitySpawnEvent(plugin, user, player, entity);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public CosmeticEntity getCosmeticEntity() {
        return nmsEntity;
    }
}
