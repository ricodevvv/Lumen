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

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.material.Materials;

import org.jetbrains.annotations.Nullable;

public class Confetti implements GadgetBehavior {

    private static final double SPREAD = 0.5d;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        Location location = player.getEyeLocation();
        Vector vector = player.getLocation().getDirection();

        Sounds.playAt(location, XSound.ENTITY_BLAZE_SHOOT, 0.8f, 2.0f);

        for (int i = 0; i < 3; ++i) {
            Particles.spawn(location.getWorld(), LumenParticle.FIREWORK,
                    location,
                    0,
                    vector.getX(),
                    vector.getY(),
                    vector.getZ(),
                    0.3d
            );
        }

        for (int i = 0; i < 50; ++i) {
            Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                    location,
                    0,
                    vector.getX() + MathUtil.randomRange(-SPREAD, SPREAD),
                    vector.getY() + MathUtil.randomRange(-SPREAD, SPREAD),
                    vector.getZ() + MathUtil.randomRange(-SPREAD, SPREAD),
                    0.8d,
                    Materials.getRandomWoolItem()
            );
        }
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
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
}
