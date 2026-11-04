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

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.cryptomorin.xseries.XMaterial;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Bat implements MorphBehavior {

    private static final ItemStack COCO_BEANS_ITEM = XMaterial.COCOA_BEANS.parseItem();

    private boolean flyable;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
        flyable = context.getType().getCategory().getConfig().getBoolean("morphs.settings.flyable");
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (nmsEntity.getBukkitEntity() instanceof org.bukkit.entity.Bat) {
            org.bukkit.entity.Bat bat = (org.bukkit.entity.Bat) nmsEntity.getBukkitEntity();
            bat.setAwake(true);
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Action action = event.getAction();

        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && flyable) {
            Player player = context.getPlayer();
            Location location = player.getLocation();

            player.setVelocity(location.getDirection().multiply(0.4d).setY(0.6d));
            Sounds.playAt(location, XSound.ENTITY_BAT_TAKEOFF, 0.6f, 1.5f);

            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public InteractionResult onToggleSneak(CosmeticContext<MorphType> context, PlayerToggleSneakEvent event, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();
        Location location = player.getLocation();

        Sounds.playAt(location, XSound.ENTITY_BAT_AMBIENT, 0.6f, 1.0f);

        CosmeticEntity droppedItem = CosmeticEntities.create(location.getWorld(), Entities.itemType());
        droppedItem.setEntityItemStack(COCO_BEANS_ITEM);
        droppedItem.setPositionRotation(location.add(0.0d, 0.1d, 0.0d));
        droppedItem.getTracker().startTracking();
        droppedItem.getTracker().destroyAfter(80);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
    }
}
