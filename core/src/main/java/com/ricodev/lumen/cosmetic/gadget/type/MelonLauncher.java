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

import org.bukkit.entity.Entity;

import org.bukkit.event.Event;

import com.ricodev.lumen.compat.Events;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.List;

public class MelonLauncher implements GadgetBehavior, Listener {

    private static final ItemStack MELON_BLOCK = XMaterial.MELON.parseItem();
    private static final ItemStack MELON_SLICE = XMaterial.MELON_SLICE.parseItem();

    private static final PotionEffect POTION_EFFECT = new PotionEffect(PotionEffectType.SPEED, 100, 1);

    private static final double EXPLOSION_SPREAD = 0.3d;
    private static final long SLICES_DESPAWN = 20 * 6L;
    private static final long THROWN_ITEM_DESPAWN = 100;

    private static final int PICKUP_DELAY = 200;
    private static final int SLICES = 6;

    private Item item;
    private final List<Item> slices = new ArrayList<>();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        Events.listenFirst(context.getPlugin().getJavaPlugin(), this, this::onPlayerPickup,
                "org.bukkit.event.entity.EntityPickupItemEvent", "org.bukkit.event.player.PlayerPickupItemEvent");
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        if (item != null && item.isValid()) {
            item.remove();
            item = null;
        }
        Player player = context.getPlayer();
        Location location = player.getEyeLocation();
        item = Entities.dropItem(location, MELON_BLOCK, entity -> {
            entity.setPickupDelay(PICKUP_DELAY);
            entity.setVelocity(location.getDirection());

            MetadataUtil.setCustomEntity(entity);
        });
        Sounds.playFor(player, location, XSound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.8f);

        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (item == null || !item.isValid()) {
            return;
        }

        if (item.isOnGround()) {
            Location location = item.getLocation();
            location.getWorld().playEffect(location, Effect.STEP_SOUND, MELON_BLOCK.getType());

            List<Item> items = new ArrayList<>();

            for (int i = 0; i < SLICES; i++) {
                Item droppedItem = Entities.dropItem(location,
                        new ItemBuilderImpl(MELON_SLICE).setMaxSize(1).getItemStack(),
                        entity -> {
                            entity.setVelocity(new Vector(
                                    MathUtil.randomRange(-EXPLOSION_SPREAD, EXPLOSION_SPREAD),
                                    MathUtil.randomRange(0.2d, EXPLOSION_SPREAD),
                                    MathUtil.randomRange(-EXPLOSION_SPREAD, EXPLOSION_SPREAD)

                            ));
                            MetadataUtil.setCustomEntity(entity);
                        });
                items.add(droppedItem);
                slices.add(droppedItem);
            }
            despawn();

            context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                    () -> despawnSlices(items),
                    SLICES_DESPAWN
            );
        } else {
            if (item.getTicksLived() > THROWN_ITEM_DESPAWN) {
                despawn();
            }
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        despawn();
        despawnSlices(slices);
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

    private void onPlayerPickup(Event event) {
        Item pickupItem = Events.get(event, "getItem");
        Entity picker = Events.entity(event, "getEntity");

        if (picker == null) {
            picker = Events.entity(event, "getPlayer");
        }
        if (picker instanceof Player && pickupItem != null && slices.remove(pickupItem)) {
            Player pickupPlayer = (Player) picker;
            pickupPlayer.addPotionEffect(POTION_EFFECT);
            Sounds.playFor(pickupPlayer, pickupPlayer.getLocation(), XSound.ENTITY_GENERIC_EAT, 0.5f, 1.0f);

            pickupItem.remove();
            Events.cancel(event);
        }
    }

    private void despawn() {
        if (item != null) {
            item.remove();
            item = null;
        }
    }

    private void despawnSlices(List<Item> items) {
        for (Item slice : items) {
            if (items != slices) {
                slices.remove(slice);
            }
            if (slice.isValid()) {
                Location location = slice.getLocation();
                Particles.spawn(location.getWorld(), LumenParticle.LARGE_SMOKE, location.add(0.0d, 0.2d, 0.0d), 0);
                slice.remove();
            }
        }
        items.clear();
    }
}
