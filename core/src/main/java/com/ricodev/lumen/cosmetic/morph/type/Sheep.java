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

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

public class Sheep implements MorphBehavior {

    private static final DyeColor[] DYE_COLORS = DyeColor.values();

    @Override
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
        Player player = context.getPlayer();

        if (!player.isSneaking()) {
            if (nmsEntity.getBukkitEntity() instanceof org.bukkit.entity.Sheep) {
                org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) nmsEntity.getBukkitEntity();
                sheep.setColor(DYE_COLORS[MathUtil.randomRangeInt(0, DYE_COLORS.length - 1)]);
                nmsEntity.sendEntityMetadataPacket();
            }
            Sounds.playAt(player, XSound.ENTITY_SHEEP_AMBIENT, 0.5f, 1.0f);
            Particles.spawn(player.getWorld(), LumenParticle.CLOUD,
                    player.getLocation().add(0.0d, 0.5d, 0.0d),
                    20,
                    0.5d,
                    0.5d,
                    0.5d,
                    0.0d
            );
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
    }
}
