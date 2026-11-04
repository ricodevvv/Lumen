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

import com.ricodev.lumen.compat.Events;

import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MetadataUtil;

public class WitherMissile implements GadgetBehavior, Listener {

    private WitherSkull witherSkull;
    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        despawn();

        Player player = context.getPlayer();
        location = player.getEyeLocation();

        witherSkull = Entities.spawn(location, WitherSkull.class, entity -> {
            entity.setCharged(true);
            entity.setIsIncendiary(false);
            entity.setYield(0.0f);
            entity.setShooter(player);
            entity.setVelocity(location.getDirection());
            MetadataUtil.setCustomEntity(entity);
        });
        Compats.get().addPassenger(witherSkull, player);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (witherSkull != null && witherSkull.isValid()) {
            location = witherSkull.getLocation(location);

            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location, 0);
            Sounds.playAt(location, XSound.BLOCK_TRIPWIRE_CLICK_ON, 0.2f, 2.0f);

            if (witherSkull.getTicksLived() > context.getType().getDurationTicks()) {
                explode();
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        despawn();
    }

    @Override
    public boolean requiresGroundOnUse() {
        return false;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return true;
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }

    private void despawn() {
        if (witherSkull != null) {
            witherSkull.remove();
            witherSkull = null;
        }
    }

    @EventHandler
    public void onSkullExplode(ExplosionPrimeEvent event) {
        if (event.getEntity() == witherSkull) {
            event.setCancelled(true);
            explode();
        }
    }

    @EventHandler
    public void onSkullHit(ProjectileHitEvent event) {
        if (event.getEntity() == witherSkull) {
            Events.cancel(event);
        }
    }

    private void explode() {
        Sounds.playAt(location, XSound.ENTITY_WITHER_BREAK_BLOCK, 0.5f, 1.0f);
        Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, location, 1, 0.1d, 0.1d, 0.1d, 0.1d);
        despawn();
    }
}
