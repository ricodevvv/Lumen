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
package com.ricodev.lumen.cosmetic.morph;

import com.ricodev.lumen.util.entity.Entities;

import com.ricodev.lumen.compat.Compats;

import com.ricodev.lumen.util.entity.CosmeticEntities;

import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.morph.Morph;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticImpl;

public class MorphImpl extends CosmeticImpl<MorphType, MorphBehavior> implements Morph {

    protected final Location location = new Location(null, 0.0d, 0.0, 0.0d);
    private final boolean showNameTag;
    protected CosmeticEntity nmsEntity;
    protected boolean cooldown;

    public MorphImpl(LumenPlugin plugin, User user, MorphType type, MorphBehavior behavior) {
        super(plugin, user, type, behavior);
        showNameTag = getType().getCategory().getConfig().getBoolean("self_view.show_name_tag");
    }

    @Override
    protected void onEquip() {
        user.removeCosmetic(plugin.getCategoryRegistries().balloons(), false, true);
        user.removeCosmetic(plugin.getCategoryRegistries().banners(), false, true);
        user.removeCosmetic(plugin.getCategoryRegistries().emotes(), false, true);
        user.removeCosmetic(plugin.getCategoryRegistries().mounts(), false, true);
        user.removeCosmetic(plugin.getCategoryRegistries().statuses(), false, true);

        nmsEntity = CosmeticEntities.create(player.getWorld(), cosmeticType.getEntityType());

        if (showNameTag) {
            nmsEntity.setCustomName(Component.text(player.getName()));
        }

        if (!user.hasSelfViewMorph()) {
            nmsEntity.getTracker().addAntiViewer(player);
        } else {
            nmsEntity.removeCollision(player);
        }
        nmsEntity.setPositionRotation(player.getLocation(location));
        behavior.setupEntity(this, nmsEntity);
        nmsEntity.getTracker().setOwner(player);
        nmsEntity.getTracker().startTracking();

        Compats.get().setInvisible(player, true);

        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    protected void onUpdate() {
        updateEntityPosition();

        if (cosmeticType.hasAbility()) {
            behavior.onUpdate(this, nmsEntity);
        }
    }

    @Override
    protected void onUnequip() {
        if (user.hasSelfViewMorph()) {
            nmsEntity.addCollision(player);
        }
        Compats.get().setInvisible(player, false);

        nmsEntity.getTracker().destroy();
        nmsEntity = null;
    }

    private void updateEntityPosition() {
        player.getLocation(location);
        nmsEntity.sendPositionRotationPacket(location);
    }

    private boolean canPerformAbility() {
        return !cooldown && cosmeticType.hasAbility();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == player && nmsEntity != null) {

            if (canPerformAbility()) {
                MorphBehavior.InteractionResult result = behavior.onToggleSneak(this, event, nmsEntity);

                if (result.shouldApplyCooldown()) {
                    addCooldown(cosmeticType.getCooldown());
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            return;
        }

        if (event.getPlayer() == player) {
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                if (behavior.hasAttackAnimation()) {
                    nmsEntity.sendSwingAnimationPacket(true);
                }
            }

            if (canPerformAbility()) {
                MorphBehavior.InteractionResult result = behavior.onInteract(this, event, nmsEntity);

                if (result.shouldApplyCooldown()) {
                    addCooldown(cosmeticType.getCooldown());
                }
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (event.getPlayer() == player && behavior.hasItemHoldAnimation()) {
            ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
            ItemStack previousItemStack = player.getInventory().getItem(event.getPreviousSlot());

            if (itemStack != null && previousItemStack == null
                    || itemStack == null && previousItemStack != null
                    || itemStack != null && previousItemStack.getType() != itemStack.getType()) {
                nmsEntity.setMainHand(itemStack);
                nmsEntity.sendEntityEquipmentPacket();
            }
        }
    }

    @Override
    public CosmeticEntity getCosmeticEntity() {
        return nmsEntity;
    }

    protected void addCooldown(double time) {
        cooldown = true;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (isEquipped()) {
                user.sendActionBar(user.translate(
                        "cosmetic.morphs.ability.cooldown.ready",
                        Placeholder.unparsed("morph", cosmeticType.getName(user))
                ));
                cooldown = false;
                Sounds.playFor(player, location, XSound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 2.0f);
            }
        }, (long) time * 20L);
    }

    public boolean isCooldown() {
        return cooldown;
    }
}
