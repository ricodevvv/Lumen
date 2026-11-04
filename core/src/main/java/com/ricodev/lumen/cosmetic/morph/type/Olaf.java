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

import com.ricodev.lumen.compat.Events;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

public class Olaf implements MorphBehavior, Listener {

    private Snowball snowball;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Action action = event.getAction();

        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && snowball == null) {
            Player player = context.getPlayer();
            snowball = player.launchProjectile(Snowball.class);
            Sounds.playAt(player, XSound.ENTITY_SNOWBALL_THROW, 0.8f, 1.2f);
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        if (snowball != null) {
            snowball.remove();
            snowball = null;
        }
    }

    @EventHandler
    public void onBallHit(ProjectileHitEvent event) {
        if (event.getEntity() == snowball) {
            if (Events.entity(event, "getHitEntity") instanceof Player) {
                Player player = (Player) Events.entity(event, "getHitEntity");
                Location location = snowball.getLocation();
                MathUtil.pushEntity(player, location, 0.8d, 0.6d);
                location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.SNOW_BLOCK);
            }
            snowball = null;
        }
    }
}
