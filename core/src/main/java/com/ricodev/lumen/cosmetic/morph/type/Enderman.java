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
package com.ricodev.lumen.cosmetic.morph.type;

import org.bukkit.Material;

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.util.material.Materials;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MetadataUtil;

public class Enderman implements MorphBehavior {

    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
            .flicker(false)
            .withColor(Color.BLACK)
            .with(Type.BALL_LARGE)
            .trail(false)
            .build();

    private boolean defaultFlight = false;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
        Player player = context.getPlayer();

        if (player.getAllowFlight()) {
            defaultFlight = true;
        } else if (context.getType().hasAbility()) {
            player.setAllowFlight(true);
        }
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        return InteractionResult.noAction();
    }

    @Override
    public InteractionResult onToggleSneak(CosmeticContext<MorphType> context, PlayerToggleSneakEvent event, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (!player.isFlying()) {
            return performTeleport(context);
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        Player player = context.getPlayer();
        player.setAllowFlight(defaultFlight);
    }

    @Override
    public boolean hasAttackAnimation() {
        return true;
    }

    @Override
    public boolean hasItemHoldAnimation() {
        return true;
    }

    private InteractionResult performTeleport(CosmeticContext<MorphType> context) {
        Player player = context.getPlayer();
        Location currentLocation = player.getLocation();

        player.getWorld().playEffect(currentLocation, Effect.SMOKE, 4);

        Location teleportLocation = player.getTargetBlock((java.util.Set<Material>) null, 17).getLocation();
        teleportLocation.setPitch(currentLocation.getPitch());
        teleportLocation.setYaw(currentLocation.getYaw());

        if (!Materials.isAir(teleportLocation.getBlock().getType())) {
            teleportLocation.setY(teleportLocation.getY() + 1.0d);
        }

        player.teleport(teleportLocation);
        Sounds.playAt(teleportLocation, XSound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.0f);

        spawnTeleportFirework(teleportLocation);

        return InteractionResult.success();
    }

    private void spawnTeleportFirework(Location location) {
        Entities.spawn(location.add(0.0d, 1.0d, 0.0d), Firework.class,
                entity -> {
                    FireworkMeta meta = entity.getFireworkMeta();
                    meta.addEffect(FIREWORK_EFFECT);
                    entity.setFireworkMeta(meta);
                    MetadataUtil.setCustomEntity(entity);
                }
        ).detonate();
    }
}
