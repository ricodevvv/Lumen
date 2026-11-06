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


import com.ricodev.lumen.util.entity.Entities;


import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.FastMathUtil;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.Events;
import com.ricodev.lumen.util.entity.FloatingBlock;
import com.ricodev.lumen.util.entity.FloatingBlocks;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.List;

public class CoinPartyBomb implements GadgetBehavior, Listener {

    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
            .flicker(false)
            .withColor(Color.YELLOW)
            .with(Type.BURST).
            trail(false).build();

    private static final float Y_MOVEMENT_PER_TICK = FastMathUtil.PI / 90.0f;
    private static final float ROTATION_PER_TICK = FastMathUtil.PI / 45.0f;
    private static final float HEAD_ROTATION_PER_TICK = (float) Math.toDegrees(ROTATION_PER_TICK);
    private static final float Y_OFFSET = 10.0f;

    private static final StructureBlock GOLD_BLOCK = new StructureBlock("minecraft:gold_block");
    private static final long EXTRA_TIME = 400L;
    private static final int DROP_INTERVAL = 4;

    private final List<Item> items = new ArrayList<>();
    private FloatingBlock bomb;
    private int coins;
    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private final Location center = new Location(null, 0.0d, 0.0d, 0.0d);
    private int tick;

    private Lumen plugin;
    private int pickupReward;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        if (plugin == null) {
            plugin = context.getPlugin();
        }
        Events.listenFirst(plugin.getJavaPlugin(), this, this::onCoinPickup,
                "org.bukkit.event.entity.EntityPickupItemEvent", "org.bukkit.event.player.PlayerPickupItemEvent");
        CosmeticType<?, ?> type = context.getType();
        pickupReward = type.getCategory().getConfig().getInt("cosmetics." + type.getKey() + ".pickup_reward");
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();

        if (context.getType().hasPurchasableAmmo()) {
            long totalValue = pickupReward * (context.getType().getDurationTicks() / DROP_INTERVAL);

            if (totalValue > context.getType().getAmmoCost()) {
                context.getUser().sendMessage(Component.text("This gadget is misconfigured and cannot be used. " +
                        "The coin rewards exceed the ammo cost. Please contact a server administrator.", NamedTextColor.RED));
                return InteractionResult.fail();
            }
        }
        player.getLocation(location);
        player.getLocation(center);
        location.setPitch(45.0f);

        bomb = FloatingBlocks.block(center, GOLD_BLOCK);

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), () -> {
            tick = 0;
            despawnBlock();
        }, context.getType().getDurationTicks());

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(),
                () -> {
                    if (tick == 0) {
                        onUnequip(context);
                    }
                },
                context.getType().getDurationTicks() + EXTRA_TIME
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (bomb == null) {
            return;
        }
        float y = Math.abs(FastMathUtil.sin(Y_MOVEMENT_PER_TICK * tick));
        location.setY(center.getY() + y * Y_OFFSET);
        location.setYaw(HEAD_ROTATION_PER_TICK * tick);

        bomb.teleport(location);

        if (tick % DROP_INTERVAL == 0) {
            Location location = bomb.getLocation();
            ItemStack coin = Compats.get().setItemTag(new ItemBuilderImpl(context.getType().getItemStack().getType()).setMaxSize(1).getItemStack(),
                    "coin", String.valueOf(coins++));

            items.add(Entities.dropItem(location, coin,
                    entity -> {
                        float angle = ROTATION_PER_TICK * tick;
                        float x = FastMathUtil.cos(angle);
                        float z = FastMathUtil.sin(angle);

                        entity.setVelocity(new Vector(x, MathUtil.randomRange(-3.0d, 0.1d), z));
                        entity.setPickupDelay(20);

                        MetadataUtil.setCustomEntity(entity);
                    }));

            Entities.spawn(location, Firework.class, entity -> {
                FireworkMeta meta = entity.getFireworkMeta();
                meta.addEffect(FIREWORK_EFFECT);
                entity.setFireworkMeta(meta);
                MetadataUtil.setCustomEntity(entity);
            }).detonate();

            Sounds.playAt(location, XSound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        }

        if (tick++ > 360) {
            tick = 0;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        tick = 0;
        despawnBlock();
        despawnItems();
    }

    public void despawnBlock() {
        FloatingBlocks.remove(bomb);
        bomb = null;
    }

    private void despawnItems() {
        for (Item item : items) {
            Particles.spawn(location.getWorld(), LumenParticle.LARGE_SMOKE, item.getLocation(location), 0);
            item.remove();
        }
        items.clear();
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

    private void onCoinPickup(Event event) {
        Item pickupItem = Events.get(event, "getItem");
        Entity picker = Events.entity(event, "getEntity");

        if (picker == null) {
            picker = Events.entity(event, "getPlayer");
        }
        if (picker instanceof Player && pickupItem != null && items.remove(pickupItem)) {
            Player pickupPlayer = (Player) picker;
            Events.cancel(event);
            pickupItem.remove();

            Location pickupLocation = pickupItem.getLocation().add(0.0d, 0.4d, 0.0d);
            Sounds.playAt(pickupLocation, XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
            Particles.spawn(pickupLocation.getWorld(), LumenParticle.FIREWORK, pickupLocation.add(0.0d, 0.4d, 0.0d), 0);

            User user = plugin.getUserManager().getConnected(pickupPlayer);

            if (user != null) {
                plugin.getEconomyManager().getEconomyProvider().addCoinsAsync(user, pickupReward);
            }
        }
    }
}
