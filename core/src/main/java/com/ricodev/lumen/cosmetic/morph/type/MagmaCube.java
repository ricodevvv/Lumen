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
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class MagmaCube implements MorphBehavior {

    private static final Vector UP_FORCE = new Vector(0.0d, 0.6d, 0.0d);

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (nmsEntity.getBukkitEntity() instanceof Slime) {
            Slime slime = (Slime) nmsEntity.getBukkitEntity();
            slime.setSize(2);
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (player.isSneaking() && player.isOnGround()) {
            Location location = player.getLocation();
            Vector vector = location.getDirection().multiply(0.8d).add(UP_FORCE);

            if (vector.getY() < 0.5d) {
                vector.setY(0.5d);
            }
            player.setVelocity(vector);
            Sounds.playAt(player, XSound.ENTITY_MAGMA_CUBE_JUMP, 0.8f, 1.0f);
            Particles.spawn(player.getWorld(), LumenParticle.LAVA, location, 12, 1.0d, 0.8d, 1.0d, 0.0d);

            context.getUser().setFallDamageProtection(5);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
    }
}
