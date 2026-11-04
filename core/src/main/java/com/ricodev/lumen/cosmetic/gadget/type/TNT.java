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

import org.bukkit.Material;

import java.util.Map;

import java.util.HashMap;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.material.Materials;
import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

import java.util.HashSet;
import java.util.Set;

public class TNT implements GadgetBehavior, Listener {

    private static final Lumen PLUGIN = LumenPlugin.getPlugin();

    private static final int FUSE_TICKS = 80;

    private final Set<Entity> entities = new HashSet<>();
    private final Set<FallingBlock> fallingBlocks = new HashSet<>();
    private final Map<FallingBlock, Material> fallingMaterials = new HashMap<>();

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        Player player = context.getPlayer();
        Location location = player.getEyeLocation().add(0.0d, 0.8d, 0.0d);

        entities.add(Entities.spawn(location, TNTPrimed.class, entity -> {
            entity.setVelocity(location.getDirection().multiply(0.8d));
            entity.setFuseTicks(FUSE_TICKS);

            MetadataUtil.setCustomEntity(entity);
        }));
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        for (Entity entity : entities) {
            entity.remove();
        }
        entities.clear();

        for (FallingBlock fallingBlock : fallingBlocks) {
            fallingBlock.remove();
        }
        fallingBlocks.clear();
        fallingMaterials.clear();
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

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entities.contains(entity)) {
            event.setCancelled(true);

            Location location = entity.getLocation();

            Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION_EMITTER, location, 0);
            Sounds.playAt(location, XSound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.0f);

            for (Player player : MathUtil.getClosestPlayersFromLocation(location, 4.0d)) {
                MathUtil.pushEntity(player, location, MathUtil.randomRange(1.0d, 3.0d), MathUtil.randomRange(1.0d, 2.0d));

                User otherUser = PLUGIN.getUserManager().getConnected(player);

                if (otherUser != null) {
                    otherUser.setFallDamageProtection(8);
                }
            }
            int fallingBlockAmount = 0;

            for (Block block : event.blockList()) {
                if (!block.getType().isSolid()) {
                    continue;
                }
                if (fallingBlockAmount < 10) {
                    fallingBlockAmount++;

                    Location airLocation = block.getLocation().add(0.0d, 1.0d, 0.0d);

                    if (Materials.isAir(airLocation.getBlock().getType())) {
                        FallingBlock fallingBlock = Compats.get().spawnFallingBlock(location, block);
                        fallingMaterials.put(fallingBlock, block.getType());

                        Vector direction = block.getLocation().subtract(entity.getLocation()).toVector().add(new Vector(0.0d, 6.0d, 0.0d));

                        fallingBlock.setVelocity(direction.normalize());
                        fallingBlock.setDropItem(false);
                        fallingBlock.setHurtEntities(false);

                        MetadataUtil.setCustomEntity(fallingBlock);

                        fallingBlocks.add(fallingBlock);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockImpact(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock && fallingBlocks.remove(((FallingBlock) event.getEntity()))) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            Material material = fallingMaterials.remove(fallingBlock);
            fallingBlock.getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, material != null ? material : Material.STONE);
            event.setCancelled(true);
        }
    }
}
