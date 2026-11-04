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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.morph.FlyableMorph;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.MetadataUtil;

public class Wither extends FlyableMorph implements Listener {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private WitherSkull skull;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
        super.onEquip(context);
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        if (event.getAction() == Action.LEFT_CLICK_AIR && skull == null) {
            Player player = context.getPlayer();

            skull = player.launchProjectile(WitherSkull.class);
            MetadataUtil.setCustomEntity(skull);
            Sounds.playAt(player, XSound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f);

            context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), this::despawnSkull, 60L);
            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
        super.onUpdate(context, nmsEntity);
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        super.onUnequip(context);
        despawnSkull();
    }

    @EventHandler
    public void onSkullExplode(EntityExplodeEvent event) {
        if (event.getEntity() == skull) {
            event.setCancelled(true);

            Location location = skull.getLocation();
            Sounds.playAt(location, XSound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 1.0f);
            Particles.spawn(location.getWorld(), LumenParticle.EXPLOSION, location, 1, 0.1d, 0.1d, 0.1d, 0.1d);

            for (Player hitPlayer : MathUtil.getClosestPlayersFromLocation(location, 2.5d)) {
                User otherUser = PLUGIN.getUserManager().getConnected(hitPlayer);

                if (otherUser != null) {
                    otherUser.setFallDamageProtection(6);
                }
                hitPlayer.setVelocity(new Vector(MathUtil.randomRange(-0.5d, 0.5d),
                        MathUtil.randomRange(0.8d, 1.5d),
                        MathUtil.randomRange(-0.5d, 0.5d)
                ));
            }
            despawnSkull();
        }
    }

    private void despawnSkull() {
        if (skull != null) {
            skull.remove();
            skull = null;
        }
    }
}
