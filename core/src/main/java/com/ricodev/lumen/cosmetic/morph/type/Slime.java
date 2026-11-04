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

import com.ricodev.lumen.compat.Compats;

import com.cryptomorin.xseries.XMaterial;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.*;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;

public class Slime implements MorphBehavior, Listener {

    private static final XMaterial BLOCK_DATA = XMaterial.SLIME_BLOCK;

    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);
    private FallingBlock fallingBlock;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        if (nmsEntity.getBukkitEntity() instanceof org.bukkit.entity.Slime) {
            org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) nmsEntity.getBukkitEntity();
            slime.setSize(3);
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        if (event.getAction() == Action.LEFT_CLICK_AIR && fallingBlock == null) {
            Player player = context.getPlayer();
            Location location = player.getLocation();

            fallingBlock = Compats.get().spawnFallingBlock(location, BLOCK_DATA);
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);
            Vector vector = location.add(0.0D, 0.3, 0.0D).getDirection().multiply(1.3D);
            fallingBlock.setVelocity(vector.add(new Vector(0.0D, 0.5D, 0.0D)));

            context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), () -> {
                if (fallingBlock != null) {
                    removeBlock();
                }
            }, 75L);
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (player.isSneaking() && player.isOnGround()) {
            player.getLocation(location);
            location.setPitch(0.0f);
            player.setVelocity(location.getDirection().multiply(0.8d).add(new Vector(0.0d, 0.6d, 0.0d)));
            Sounds.playAt(player, XSound.ENTITY_SLIME_JUMP, 0.8f, 1.0f);
            Particles.spawn(location.getWorld(), LumenParticle.ITEM_SLIME,
                    location,
                    30,
                    1.5d,
                    0.5d,
                    1.5d,
                    0.0d
            );
        }

        if (fallingBlock == null || !fallingBlock.isValid()) {
            return;
        }
        Location fallingBlockLocation = fallingBlock.getLocation(location);
        Player hitPlayer = MathUtil.getClosestPlayerFromLocation(fallingBlockLocation, 1.5D);

        if (hitPlayer == null || hitPlayer == player) {
            return;
        }
        MathUtil.pushEntity(hitPlayer, location, 0.4d, 0.3d);
        User otherUser = context.getPlugin().getUserManager().getConnected(hitPlayer);

        if (otherUser != null) {
            otherUser.setFallDamageProtection(3);
        }
        Sounds.playAt(fallingBlock, XSound.ENTITY_SLIME_ATTACK, 1.0f, 1.0f);
        removeBlock();
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        if (fallingBlock != null) {
            removeBlock();
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() == fallingBlock) {
            removeBlock();
            event.setCancelled(true);
        }
    }

    private void removeBlock() {
        fallingBlock.getWorld().playEffect(fallingBlock.getLocation(), Effect.STEP_SOUND, BLOCK_DATA.get());
        fallingBlock.remove();
        fallingBlock = null;
    }
}
