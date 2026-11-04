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

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.EntityTracker;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.entity.EntityTrackerImpl;
import com.ricodev.lumen.util.MathUtil;

public class Skeleton implements MorphBehavior {

    private static final ItemStack ITEM_STACK = XMaterial.BONE.parseItem();

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
            Location location = player.getLocation();
            Location explodeLocation = location.clone().add(0.0d, 0.5d, 0.0d);

            EntityTracker entityTracker = new EntityTrackerImpl();

            for (int i = 0; i < 15; i++) {
                double x = MathUtil.randomRange(-0.8d, 0.8d);
                double y = MathUtil.randomRange(0.1d, 0.7d);
                double z = MathUtil.randomRange(-0.8d, 0.8d);

                CosmeticEntity itemEntity = CosmeticEntities.create(player.getWorld(), Entities.itemType(), entityTracker);
                itemEntity.setEntityItemStack(ITEM_STACK);
                itemEntity.setVelocity(x, y, z);
                itemEntity.setPositionRotation(explodeLocation);
            }
            entityTracker.startTracking();
            entityTracker.destroyAfter(60);

            for (Player pushPlayer : MathUtil.getClosestPlayersFromLocation(location, 3.0d)) {
                if (pushPlayer != player) {
                    MathUtil.pushEntity(pushPlayer, location, 1.0d, 0.5d);
                }
            }
            Sounds.playAt(player, XSound.ENTITY_SKELETON_HURT, 0.8f, 1.0f);
            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location.add(0.0d, 1.0d, 0.0d), 10, 0.3d, 0.3d, 0.3d, 0.0d);

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

    @Override
    public boolean hasAttackAnimation() {
        return true;
    }

    @Override
    public boolean hasItemHoldAnimation() {
        return true;
    }
}
