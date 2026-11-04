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

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.util.MathUtil;

public class Witch implements MorphBehavior, Listener {

    private static final double POTION_RANGE = 3.5d;

    private ThrownPotion potion;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Player player = context.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR && potion == null) {
            potion = player.launchProjectile(ThrownPotion.class);
            potion.setVelocity(player.getLocation().getDirection().multiply(1.1d));

            Sounds.playAt(player, XSound.ENTITY_WITCH_THROW, 1.0f, 1.0f);
            Sounds.playAt(player, XSound.ENTITY_WITCH_AMBIENT, 1.0f, 1.0f);

            return InteractionResult.success();
        }
        return InteractionResult.noAction();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
        if (potion == null) {
            return;
        }

        potion.remove();
        potion = null;
    }

    @Override
    public boolean hasAttackAnimation() {
        return false;
    }

    @Override
    public boolean hasItemHoldAnimation() {
        return true;
    }

    @EventHandler
    public void onPotionHit(ProjectileHitEvent event) {
        if (event.getEntity() == potion) {
            Location location = event.getEntity().getLocation();

            for (Player hitPlayer : MathUtil.getClosestPlayersFromLocation(location, POTION_RANGE)) {
                hitPlayer.addPotionEffect(new PotionEffect(XPotion.BLINDNESS.get(), 30, 1));
            }
            potion = null;
        }
    }
}
