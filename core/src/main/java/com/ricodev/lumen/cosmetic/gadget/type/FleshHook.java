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

import com.ricodev.lumen.compat.Compats;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class FleshHook implements GadgetBehavior {

    private static final ItemStack ITEM_STACK = XMaterial.TRIPWIRE_HOOK.parseItem();
    private static final int MAX_TICKS_ALIVE = 120;

    private Item item;
    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        if (item != null) {
            item.remove();
        }
        location = context.getPlayer().getEyeLocation();

        item = Entities.dropItem(location, ITEM_STACK, entity -> {
            entity.setPickupDelay(Integer.MAX_VALUE);
            entity.setVelocity(location.getDirection().multiply(1.5d));

            MetadataUtil.setCustomEntity(entity);
        });
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (item == null || !item.isValid()) {
            return;
        }
        Player player = context.getPlayer();
        item.getLocation(location);
        Particles.spawn(location.getWorld(), LumenParticle.CRIT, location, 0);
        Sounds.playFor(player, location, XSound.ITEM_FLINTANDSTEEL_USE, 0.5f, 0.8f);

        if (item.isOnGround() || item.getTicksLived() > MAX_TICKS_ALIVE) {
            despawn();
            return;
        }
        Player hitPlayer = MathUtil.getClosestVisiblePlayerFromLocation(player, location, 1.5d);

        if (hitPlayer != null && hitPlayer != player) {
            Compats.get().playHurtAnimation(hitPlayer);
            Sounds.playAt(hitPlayer.getLocation(location), XSound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
            MathUtil.pullEntity(hitPlayer, player.getLocation(location), 3.0d, 1.2d);
            Sounds.playFor(player, location, XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            despawn();

            User user = context.getPlugin().getUserManager().getConnected(hitPlayer);

            if (user != null) {
                user.setFallDamageProtection(8);
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        despawn();
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
        return false;
    }

    private void despawn() {
        if (item != null) {
            item.remove();
            item = null;
        }
    }
}
