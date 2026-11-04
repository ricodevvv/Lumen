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

import java.util.Map;

import java.util.HashMap;

import com.ricodev.lumen.compat.Events;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.material.Materials;

import java.util.HashSet;
import java.util.Set;

public class Paintball implements GadgetBehavior, Listener {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private static final int PAINT_RANGE = 2;
    private static final int PAINT_DURATION = 5 * 20;
    private static final double PAINT_PARTICLE_CHANCE = 0.6d;

    private final Set<Projectile> balls = new HashSet<>();
    private final Map<Projectile, ItemStack> colors = new HashMap<>();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        Location location = player.getEyeLocation();

        ItemStack color = Materials.getRandomWoolItem();
        balls.add(Entities.spawn(location, Snowball.class, entity -> {
            entity.setVelocity(location.getDirection().multiply(2.0d));
            entity.setShooter(player);
            Compats.get().setProjectileItem(entity, color);
            colors.put(entity, color);

            MetadataUtil.setCustomEntity(entity);
        }));
        Sounds.playAt(location, XSound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
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

    @EventHandler
    public void onBallHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball && balls.remove(((Snowball) event.getEntity()))) {
            Snowball snowball = (Snowball) event.getEntity();
            Location location = snowball.getLocation().add(snowball.getVelocity());
            Sounds.playAt(location, XSound.BLOCK_STONE_BREAK, 1.0f, 0.0f);
            ItemStack itemStack = colors.remove(snowball);

            if (itemStack == null) {
                itemStack = Materials.getRandomWoolItem();
            }

            for (Block block : MathUtil.getIn3DRadius(location, PAINT_RANGE)) {
                if (PLUGIN.getFakeBlockManager().setFakeBlock(block, itemStack, true, PAINT_DURATION)) {
                    if (MathUtil.THREAD_LOCAL_RANDOM.nextDouble() < PAINT_PARTICLE_CHANCE) {
                        location.getWorld().playEffect(block.getLocation(location), Effect.STEP_SOUND, itemStack.getType());
                    }
                }
            }
            snowball.remove();
            Events.cancel(event);
        }
    }
}
