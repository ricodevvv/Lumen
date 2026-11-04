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
package com.ricodev.lumen.cosmetic.pet.type;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.pet.PetBehavior;
import com.ricodev.lumen.api.cosmetic.pet.PetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.item.Heads;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

public class SantaClaus implements PetBehavior {

    private static final Particles.Dust DUST_OPTIONS = new Particles.Dust(Color.fromRGB(255, 255, 255), 1);

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);

    @Override
    public void onEquip(CosmeticContext<PetType> context) {
        context.getPlayer().getLocation(location);
    }

    @Override
    public void onSetupEntity(CosmeticContext<PetType> context, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityEquipment entityEquipment = livingEntity.getEquipment();
            Color color = Color.RED;

            entityEquipment.setHelmet(Heads.SANTA.getSkull());
            entityEquipment.setChestplate(
                    new ItemBuilderImpl(Material.LEATHER_CHESTPLATE).setLeatherArmorColor(color).getItemStack());
            entityEquipment.setLeggings(
                    new ItemBuilderImpl(Material.LEATHER_LEGGINGS).setLeatherArmorColor(color).getItemStack());
            entityEquipment.setBoots(new ItemBuilderImpl(Material.LEATHER_BOOTS).setLeatherArmorColor(color).getItemStack());
            entityEquipment.setItemInHand(Heads.RED_PRESENT.getSkull());

            ((Zombie) entity).setBaby(false);
            livingEntity.setCanPickupItems(false);
        }
    }

    @Override
    public void onUpdate(CosmeticContext<PetType> context, Entity entity) {
        for (int i = 0; i < 10; ++i) {
            Particles.spawn(location.getWorld(), LumenParticle.DUST, entity.getLocation(location).add(
                    MathUtil.randomRange(-1.2d, 1.2d),
                    MathUtil.randomRange(0.0d, 3.5d),
                    MathUtil.randomRange(-1.2d, 1.2d)
            ), 0, 255, 255, 255, 0.1f, DUST_OPTIONS);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<PetType> context) {
    }
}
