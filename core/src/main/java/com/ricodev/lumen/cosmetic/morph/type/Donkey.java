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

import com.ricodev.lumen.util.entity.Horses;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Donkey implements MorphBehavior {

    private boolean jumped = false;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
        context.getPlayer().setWalkSpeed(0.4f);
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (nmsEntity.getBukkitEntity() != null) {
            Horses.setup(nmsEntity.getBukkitEntity(), 0.7d, false, null, true, "DONKEY");
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            Player player = context.getPlayer();
            Sounds.playAt(player, XSound.ENTITY_DONKEY_ANGRY, 0.8f, 1.0f);
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (!player.isOnGround() && !player.isFlying() && !jumped) {
            nmsEntity.setHorseStanding(true);
            jumped = true;
            nmsEntity.sendEntityMetadataPacket();
        } else if ((player.isOnGround() || player.isFlying()) && jumped) {
            nmsEntity.setHorseStanding(false);
            jumped = false;
            nmsEntity.sendEntityMetadataPacket();
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        context.getPlayer().setWalkSpeed(0.2f);
    }
}
