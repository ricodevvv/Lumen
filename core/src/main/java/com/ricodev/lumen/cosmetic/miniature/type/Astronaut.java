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
package com.ricodev.lumen.cosmetic.miniature.type;

import com.cryptomorin.xseries.XMaterial;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureBehavior;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

public class Astronaut implements MiniatureBehavior {

    private static final ItemStack HAND_ITEM = XMaterial.NETHER_STAR.parseItem();
    private static final ItemStack CHESTPLATE_ITEM;
    private static final ItemStack LEGGINGS_ITEM;
    private static final ItemStack BOOTS_ITEM;

    static {
        Color color = Color.WHITE;
        CHESTPLATE_ITEM = new ItemBuilderImpl(Material.LEATHER_CHESTPLATE).setLeatherArmorColor(color).getItemStack();
        LEGGINGS_ITEM = new ItemBuilderImpl(Material.LEATHER_LEGGINGS).setLeatherArmorColor(color).getItemStack();
        BOOTS_ITEM = new ItemBuilderImpl(Material.LEATHER_BOOTS).setLeatherArmorColor(color).getItemStack();
    }

    @Override
    public void onEquip(CosmeticContext<MiniatureType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MiniatureType> context, CosmeticEntity nmsEntity) {
        nmsEntity.setMainHand(HAND_ITEM);
        nmsEntity.setChestplate(CHESTPLATE_ITEM);
        nmsEntity.setLeggings(LEGGINGS_ITEM);
        nmsEntity.setBoots(BOOTS_ITEM);
    }

    @Override
    public void onUpdate(CosmeticContext<MiniatureType> context, CosmeticEntity nmsEntity, int tick) {
        Particles.spawn(nmsEntity.getPreviousLocation().getWorld(), LumenParticle.CLOUD, nmsEntity.getPreviousLocation(), 3, 0.1d, 0.0d, 0.1d, 0.0d);
    }

    @Override
    public void onUnequip(CosmeticContext<MiniatureType> context) {
    }
}
