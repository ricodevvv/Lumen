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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Villager implements MorphBehavior {

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Action action = event.getAction();
        Player player = context.getPlayer();
        Location location = player.getEyeLocation();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            Sounds.playAt(player, XSound.ENTITY_VILLAGER_YES, 0.5f, 1.0f);
            Particles.spawn(player.getWorld(), LumenParticle.HAPPY_VILLAGER, location, 20, 0.8d, 0.8d, 0.8d);
        } else {
            Sounds.playAt(player, XSound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            Particles.spawn(player.getWorld(), LumenParticle.ANGRY_VILLAGER, location, 10, 0.8d, 0.8d, 0.8d);
        }
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
    }

    @Override
    public boolean hasAttackAnimation() {
        return false;
    }

    @Override
    public boolean hasItemHoldAnimation() {
        return true;
    }
}
