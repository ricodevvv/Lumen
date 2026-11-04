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

import com.ricodev.lumen.util.entity.Entities;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class Zombie implements MorphBehavior, Listener {

    private static final double RANGE = 24.0d;

    private org.bukkit.entity.Zombie zombie;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && zombie == null) {
            Location location = player.getLocation();

            zombie = Entities.spawn(location, org.bukkit.entity.Zombie.class,
                    entity -> {
                        entity.setBaby(true);
                        entity.setVelocity(location.getDirection().setY(0.6d));

                        MetadataUtil.setCustomEntity(entity);
                    }
            );

            for (Player targetPlayer : MathUtil.getClosestPlayersFromLocation(location, RANGE)) {
                if (targetPlayer == player) {
                    continue;
                }
                zombie.setTarget(targetPlayer);
                break;
            }

            context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), () -> {
                if (zombie != null && zombie.isValid()) {
                    zombie.getLocation(location);

                    for (int i = 0; i < 10; i++) {
                        Particles.spawn(location.getWorld(), LumenParticle.FIREWORK, location, 0,
                                MathUtil.randomRange(-2.0d, 2.0d),
                                MathUtil.randomRange(-2.0d, 2.0d),
                                MathUtil.randomRange(-2.0d, 2.0d),
                                0.1d
                        );
                    }
                    Sounds.playAt(location, XSound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                    zombie.remove();
                    zombie = null;
                }
            }, 160L);
        }
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        if (zombie != null) {
            zombie.remove();
            zombie = null;
        }
    }

    @Override
    public boolean hasAttackAnimation() {
        return true;
    }

    @Override
    public boolean hasItemHoldAnimation() {
        return true;
    }

    @EventHandler
    public void onZombieHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() == zombie) {
            Player hitPlayer = (Player) event.getEntity();
            Location location = zombie.getLocation();

            MathUtil.pushEntity(hitPlayer, location, 0.6d, 0.5d);

            Compats.get().playHurtAnimation(hitPlayer);
            Sounds.playAt(hitPlayer, XSound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);

            Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, location, 1);

            Sounds.playAt(zombie, XSound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);

            zombie.remove();
            zombie = null;

            event.setCancelled(true);
        }
    }
}
