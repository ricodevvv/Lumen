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
package com.ricodev.lumen.listener;

import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.compat.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import com.ricodev.lumen.util.MetadataUtil;

public class EntityListener implements Listener {

    private static final String[] CANCELLED_FOR_CUSTOM_ENTITIES = {
            "org.bukkit.event.entity.EntityTransformEvent",
            "org.bukkit.event.entity.EntityPickupItemEvent",
            "org.bukkit.event.entity.VillagerCareerChangeEvent",
            "org.bukkit.event.player.PlayerBucketEntityEvent",
            "org.bukkit.event.entity.EntityDropItemEvent"
    };

    public EntityListener(LumenPlugin plugin) {
        for (String eventClass : CANCELLED_FOR_CUSTOM_ENTITIES) {
            Events.listen(plugin, eventClass, event -> {
                if (MetadataUtil.isCustomEntity(Events.entity(event, "getEntity"))) {
                    Events.cancel(event);
                }
            });
        }
        Events.listen(plugin, "org.bukkit.event.player.PlayerPickupItemEvent", event -> {
            if (MetadataUtil.isCustomEntity(Events.entity(event, "getItem"))) {
                Events.cancel(event);
            }
        });
        Events.listen(plugin, "org.bukkit.event.entity.ProjectileHitEvent", EventPriority.NORMAL, false, event -> {
            Entity entity = Events.entity(event, "getEntity");
            Entity hitEntity = Events.entity(event, "getHitEntity");

            if (entity instanceof org.bukkit.entity.FishHook && MetadataUtil.isCustomEntity(hitEntity)) {
                entity.remove();
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(EntityBlockFormEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombust(EntityCombustEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(EntityPortalEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity()) || MetadataUtil.isCustomEntity(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity()) || MetadataUtil.isCustomEntity(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity()) || MetadataUtil.isCustomEntity(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())
                || event.getRemover() != null && MetadataUtil.isCustomEntity(event.getRemover())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity enteredEntity = event.getEntered();

        if (MetadataUtil.isCustomEntity(event.getVehicle()) && !(enteredEntity instanceof HumanEntity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (MetadataUtil.isCustomEntity(event.getVehicle())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (MetadataUtil.isCustomEntity(event.getVehicle())
                || event.getAttacker() != null && MetadataUtil.isCustomEntity(event.getAttacker())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (MetadataUtil.isCustomEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
