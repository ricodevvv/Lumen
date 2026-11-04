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

import com.ricodev.lumen.compat.Compats;

import com.cryptomorin.xseries.XMaterial;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import com.ricodev.lumen.compat.LumenParticle;

import com.ricodev.lumen.compat.Particles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

import java.util.UUID;

public class CaveSpider implements MorphBehavior, Listener {

    private static final PotionEffect CONFUSION_EFFECT = new PotionEffect(XPotion.NAUSEA.get(), 180, 0);
    private static final PotionEffect BLINDNESS_EFFECT = new PotionEffect(XPotion.BLINDNESS.get(), 60, 0);

    private static final ItemStack REDSTONE_ITEM = XMaterial.REDSTONE.parseItem();
    private static final ItemStack SPIDER_EYE_ITEM = XMaterial.SPIDER_EYE.parseItem();

    private UUID lastClicked;

    @Override
    public void onEquip(CosmeticContext<MorphType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<MorphType> context, PlayerInteractEvent event, CosmeticEntity nmsEntity) {
        Player clickedPlayer = null;

        if (lastClicked != null) {
            clickedPlayer = context.getPlugin().getJavaPlugin().getServer().getPlayer(lastClicked);
        }

        if (clickedPlayer == null) {
            return InteractionResult.noAction();
        }
        Player player = context.getPlayer();

        clickedPlayer.addPotionEffect(CONFUSION_EFFECT);
        clickedPlayer.addPotionEffect(BLINDNESS_EFFECT);
        Compats.get().playHurtAnimation(clickedPlayer);
        Sounds.playAt(clickedPlayer, XSound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
        Sounds.playAt(player, XSound.ENTITY_SPIDER_HURT, 1.0f, 1.0f);

        Location location = clickedPlayer.getLocation();

        Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                location.add(0.0d, 1.0d, 0.0d),
                30,
                0.3d,
                0.3d,
                0.3d,
                0.0d,
                REDSTONE_ITEM
        );
        Particles.spawn(location.getWorld(), LumenParticle.ITEM,
                location.add(0.0d, 0.8d, 0.0d),
                20,
                0.3d,
                0.0d,
                0.3d,
                0.1d,
                SPIDER_EYE_ITEM
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<MorphType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUnequip(CosmeticContext<MorphType> context) {
    }

    @EventHandler
    public void onClickedPlayer(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = (Player) event.getRightClicked();
            lastClicked = player.getUniqueId();
        }
    }
}
