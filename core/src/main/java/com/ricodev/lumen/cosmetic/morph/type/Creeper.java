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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;

public class Creeper implements MorphBehavior {

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private int charge;
    private boolean activated;

    public void onEquip(CosmeticContext<MorphType> context) {
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
        if (activated) {
            return InteractionResult.noAction();
        }
        Player player = context.getPlayer();

        setCreeperEffect(nmsEntity, true);
        Sounds.playAt(player, XSound.ENTITY_CREEPER_PRIMED, 0.5f, charge <= 10 ? charge / 5.0f : 1.0f);
        activated = true;

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (activated && charge++ >= 30) {
            Player player = context.getPlayer();
            player.getLocation(location);

            Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION_EMITTER, location, 1);
            Sounds.playAt(player, XSound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);

            for (Player hitPlayer : MathUtil.getClosestPlayersFromLocation(location, 5.0d)) {
                if (hitPlayer == player) {
                    continue;
                }
                MathUtil.pushEntity(hitPlayer, location, 1.2d, 1.5d);
                User otherUser = context.getPlugin().getUserManager().getConnected(hitPlayer);

                if (otherUser != null) {
                    otherUser.setFallDamageProtection(6);
                }
            }
            setCreeperEffect(nmsEntity, false);
            charge = 0;
            activated = false;
        }
    }

    private void setCreeperEffect(CosmeticEntity nmsEntity, boolean effect) {
        nmsEntity.setCreeperPowered(effect);
        nmsEntity.setCreeperIgnited(effect);
        nmsEntity.sendEntityMetadataPacket();
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        charge = 0;
    }
}
