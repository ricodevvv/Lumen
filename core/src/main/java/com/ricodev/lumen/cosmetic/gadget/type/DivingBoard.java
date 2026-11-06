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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.structure.type.BlockStructure;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.structure.type.BlockStructureImpl;

public class DivingBoard implements GadgetBehavior {

    private BlockStructure structure;
    private Location center;
    private Location location;
    private Location jump;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (structure == null) {
            structure = new BlockStructureImpl(context.getType().getStructure());
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        center = context.getPlayer().getLocation();
        location = center.clone();

        double angle = structure.spawn(center);
        jump = center.clone().add(MathUtil.rotateAroundAxisY(new Vector(0.0d, 3.0d, 3.0d), angle));

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> onUnequip(context),
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (jump != null) {
            for (Player worldPlayer : jump.getWorld().getPlayers()) {
                Block block = worldPlayer.getLocation(location).getBlock();

                if (block.equals(jump.getBlock())) {
                    worldPlayer.setVelocity(worldPlayer.getVelocity().add(new Vector(
                            0.0d,
                            Math.random() * 2.0d,
                            0.0d
                    )));
                    Sounds.playAt(location, XSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 1.4f);

                    User otherUser = context.getPlugin().getUserManager().getConnected(worldPlayer);

                    if (otherUser != null) {
                        otherUser.setFallDamageProtection(10);
                    }
                }
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        structure.remove();
        jump = null;
        center = null;
    }

    @Override
    public boolean requiresGroundOnUse() {
        return false;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return structure.isEnoughSpace(location);
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }
}
