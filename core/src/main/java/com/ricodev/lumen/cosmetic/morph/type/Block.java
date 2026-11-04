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

import org.bukkit.inventory.ItemStack;

import org.bukkit.entity.ArmorStand;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Block implements MorphBehavior {

    private Material currentMaterial = Material.EMERALD_BLOCK;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    /**
     * The block is a block display on new versions. Here it's an invisible armor stand wearing
     * the block, so it looks the same from 1.8 up.
     */
    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (nmsEntity.getBukkitEntity() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) nmsEntity.getBukkitEntity();
            stand.setVisible(false);
            stand.setBasePlate(false);
            stand.setHelmet(new ItemStack(currentMaterial));
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            org.bukkit.block.Block block = event.getClickedBlock();

            if (block.getType().isBlock() && block.getType().isSolid() && block.getType() != currentMaterial) {
                currentMaterial = block.getType();
                Player player = context.getPlayer();
                nmsEntity.setHelmet(new ItemStack(currentMaterial));

                player.getWorld().playEffect(
                        player.getLocation(),
                        Effect.STEP_SOUND,
                        currentMaterial
                );
                return InteractionResult.success();
            }
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
