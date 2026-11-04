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

import com.ricodev.lumen.util.MetadataUtil;

import com.ricodev.lumen.util.entity.Entities;

import org.bukkit.util.EulerAngle;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class Detonator implements GadgetBehavior {

    private static final ItemStack TNT_ITEM = XMaterial.TNT.parseItem();
    private static final int BOMB_AMOUNT = 3;

    private final List<ArmorStand> entities = new ArrayList<>();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    private boolean shouldExplode() {
        return entities.size() >= BOMB_AMOUNT;
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        if (shouldExplode()) {
            for (ArmorStand armorStand : entities) {
                Location location = armorStand.getLocation();

                for (Player nearbyPlayer : MathUtil.getClosestPlayersFromLocation(location, 4.0d)) {
                    MathUtil.pushEntity(nearbyPlayer, location, 2.0d, 0.5d);
                }
                Sounds.playAt(location, XSound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, location.add(0.0d, 1.0d, 0.0d), 0);
                armorStand.remove();
            }
            entities.clear();
            return InteractionResult.success();
        } else {
            if (clickedBlock == null) {
                return InteractionResult.fail();
            }
            Vector position = clickedPosition != null ? clickedPosition : new Vector(0.5d, 1.0d, 0.5d);
            Location location = clickedBlock.getLocation().add(position.getX(), position.getY() - 0.7d, position.getZ());
            Sounds.playAt(location, XSound.ENTITY_HORSE_ARMOR, 0.5f, 1.5f);

            Player player = context.getPlayer();

            ArmorStand armorStand = Entities.spawn(location, ArmorStand.class, stand -> {
                stand.setVisible(false);
                stand.setSmall(true);
                stand.setArms(false);
                stand.setMarker(true);
                stand.setGravity(false);
                stand.setHelmet(TNT_ITEM);
                stand.setHeadPose(new EulerAngle(0.0d, Math.toRadians(player.getLocation().getYaw()), 0.0d));
                MetadataUtil.setCustomEntity(stand);
            });
            entities.add(armorStand);

            return InteractionResult.fail();
        }
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        for (ArmorStand armorStand : entities) {
            Location location = armorStand.getLocation();
            Particles.spawn(location.getWorld(), LumenParticle.CLOUD, location, 0);
            armorStand.remove();
        }
        entities.clear();
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
