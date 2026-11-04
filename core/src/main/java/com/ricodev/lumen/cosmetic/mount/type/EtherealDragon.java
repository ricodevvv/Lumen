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
package com.ricodev.lumen.cosmetic.mount.type;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.mount.MountBehavior;
import com.ricodev.lumen.api.cosmetic.mount.MountType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class EtherealDragon implements MountBehavior {

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);

    @Override
    public void onEquip(CosmeticContext<MountType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        nmsEntity.setNoClip(false);
        nmsEntity.setHurtTicks(-1);

        Compats.get().setSilent(entity, true);
    }

    @Override
    public void postSetupEntity(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        Compats.get().addPassenger(entity, context.getPlayer());

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            Compats.get().setAI(livingEntity, true);
        }
    }

    @Override
    public void onUpdate(CosmeticContext<MountType> context, Entity entity, CosmeticEntity nmsEntity) {
        if (Compats.get().getPassengers(entity).isEmpty()) {
            context.getUser().removeCosmetic(context.getType().getCategory(), false, true);
            return;
        }
        context.getPlayer().getLocation(location);
        nmsEntity.move(location.getDirection().multiply(context.getType().getMovementSpeed()));
        nmsEntity.setYaw(location.getYaw() - 180.0f);
        nmsEntity.setPitch(location.getPitch());

        Particles.spawn(entity.getWorld(), LumenParticle.PORTAL, entity.getLocation(location), 70, 2.0d, 2.0d, 2.0d, 0.5d);
    }

    @Override
    public void onUnequip(CosmeticContext<MountType> context) {
        context.getUser().setFallDamageProtection(10);
    }
}
