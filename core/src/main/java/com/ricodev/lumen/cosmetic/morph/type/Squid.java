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

import com.cryptomorin.xseries.XPotion;

import com.ricodev.lumen.util.entity.Entities;

import com.cryptomorin.xseries.XMaterial;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Squid implements MorphBehavior {

    private final static ItemStack ITEMSTACK = XMaterial.INK_SAC.parseItem();
    private static final Vector UP_FORCE = new Vector(0.0d, 0.35d, 0.0d);
    private static final PotionEffect POTION_EFFECT = new PotionEffect(XPotion.BLINDNESS.get(), 60, 0);
    private final List<Item> items = new ArrayList<>();
    private int ticks;
    private boolean shooting;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ticks = 0;
            shooting = true;
            Player player = context.getPlayer();
            Sounds.playAt(player, XSound.ENTITY_SQUID_SQUIRT, 1.0f, 2.0f);
            context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), this::clearItems, 140L);
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    private final Location itemLocation = new Location(null, 0.0d, 0.0d, 0.0d);

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();

            if (item.isOnGround()) {
                item.remove();
                iterator.remove();
            } else {
                item.getLocation(itemLocation);
                Particles.spawn(itemLocation.getWorld(), LumenParticle.SPLASH, itemLocation, 0);

                Player hitPlayer = MathUtil.getClosestPlayerFromLocation(itemLocation, 1.0d);
                Player player = context.getPlayer();

                if (hitPlayer != null && hitPlayer != player) {
                    hitPlayer.addPotionEffect(POTION_EFFECT);
                    Particles.spawn(hitPlayer.getWorld(), LumenParticle.SPLASH,
                            hitPlayer.getEyeLocation(),
                            10,
                            0.3d,
                            0.4d,
                            0.3d,
                            0.1d
                    );
                    MathUtil.pushEntity(hitPlayer, itemLocation, 0.05d, 0.0d);

                    item.remove();
                    iterator.remove();
                }
            }
        }

        if (shooting) {
            if (ticks++ > 50) {
                shooting = false;
            }
            Player player = context.getPlayer();
            player.getLocation(itemLocation);

            items.add(Entities.dropItem(
                    itemLocation.add(0.0d, 0.2d, 0.0d),
                    new ItemBuilderImpl(ITEMSTACK).setMaxSize(1).getItemStack(),
                    item -> {
                        Vector vector = itemLocation.getDirection().multiply(1.3d);
                        item.setVelocity(vector.add(UP_FORCE));
                        item.setPickupDelay(Integer.MAX_VALUE);
                        MetadataUtil.setCustomEntity(item);
                    }
            ));

            if (ticks % 5 == 0) {
                Sounds.playAt(player, XSound.BLOCK_WATER_AMBIENT, 1.0f, 2.0f);
            }
            Particles.spawn(itemLocation.getWorld(), LumenParticle.SPLASH,
                    itemLocation,
                    10,
                    0.3d,
                    0.3d,
                    0.3d,
                    0.1d
            );
        }
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        clearItems();
    }

    private void clearItems() {
        for (Item item : items) {
            item.remove();
        }
        items.clear();
    }
}
