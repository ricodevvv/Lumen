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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MetadataUtil;

import java.util.HashSet;
import java.util.Set;

public class Watergun implements GadgetBehavior {

    private static final ItemStack ITEM_STACK = XMaterial.BLUE_DYE.parseItem();
    private static final Vector OFFSET_VECTOR = new Vector(0.0d, 0.4d, 0.0d);

    private final Set<Projectile> balls = new HashSet<>();

    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        location = player.getEyeLocation();

        location = player.getLocation(location).add(0.0d, 1.5d, 0.0d);
        Vector vector = location.getDirection();
        vector.add(OFFSET_VECTOR);

        balls.add(Entities.spawn(location, Snowball.class, entity -> {
            entity.setVelocity(location.getDirection().multiply(2.0d));
            entity.setShooter(player);
            Compats.get().setProjectileItem(entity, ITEM_STACK);

            MetadataUtil.setCustomEntity(entity);
        }));
        Particles.spawn(player.getWorld(), LumenParticle.SPLASH, location, 0, vector.getX(), vector.getY(), vector.getZ(), 1.0d);
        Sounds.playAt(location, XSound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        for (Projectile projectile : balls) {
            if (projectile.isValid()) {
                Particles.spawn(context.getPlayer().getWorld(), LumenParticle.SPLASH,
                        projectile.getLocation(location),
                        0,
                        0.0d,
                        0.0d,
                        0.0d,
                        1.0d
                );
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        for (Entity entity : balls) {
            entity.remove();
        }
        balls.clear();
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
}
