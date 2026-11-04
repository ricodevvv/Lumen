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
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Blaze implements MorphBehavior {

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private int tick;
    private boolean flyable;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
        flyable = context.getType().getCategory().getConfig().getBoolean("morphs.settings.flyable");
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (player.isSneaking() && flyable) {
            player.getLocation(location);
            if (tick % 15 == 0) {
                Sounds.playAt(player, XSound.ENTITY_BLAZE_AMBIENT, 0.1f, 1.0f);
            }
            player.setVelocity(location.getDirection().multiply(0.6d));

            location.add(0.0d, 0.5d, 0.0d);
            Particles.spawn(location.getWorld(), LumenParticle.FLAME, location, 0);
            Particles.spawn(location.getWorld(), LumenParticle.LAVA, location, 0);
            Particles.spawn(location.getWorld(), LumenParticle.LARGE_SMOKE, location, 0);

            if (++tick >= 360) {
                tick = 0;
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        if (flyable) {
            context.getUser().setFallDamageProtection(12);
        }
    }
}
