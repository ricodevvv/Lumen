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

import com.ricodev.lumen.compat.Particles;
import com.ricodev.lumen.compat.LumenParticle;
import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.material.Materials;

public class PartyPopper implements GadgetBehavior {

    private static final int ITEM_DESPAWN = 20 * 3;

    private boolean started;
    private int i;
    private Location location;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        location = context.getPlayer().getLocation();
        started = true;

        context.getPlugin().getJavaPlugin().getServer().getScheduler().runTaskLater(context.getPlugin().getJavaPlugin(), () -> {
                    started = false;
                    i = 0;
                },
                context.getType().getDurationTicks()
        );
        return InteractionResult.success();
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (started && i < 90) {
            Player player = context.getPlayer();
            Sounds.playAt(player.getLocation(location), XSound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);

            location.add(0.0d, 1.8d, 0.0d);
            Particles.spawn(location.getWorld(), LumenParticle.FIREWORK, location, 0,
                    MathUtil.randomRange(-1.0d, 1.0d),
                    MathUtil.randomRange(0.5d, 1.0d),
                    MathUtil.randomRange(-1.0d, 1.0d), 0.5d
            );
            dropDespawningItem(context.getPlugin(), location, Materials.getRandomWoolItem());
            i++;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        i = 0;
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

    protected void dropDespawningItem(Lumen plugin, Location location, ItemStack itemStack) {
        Vector velocity = new Vector(
                MathUtil.randomRange(-0.3d, 0.3d),
                MathUtil.randomRange(0.5d, 1.0d),
                MathUtil.randomRange(-0.3d, 0.3d)
        );
        Entities.dropDecorativeItem(plugin.getJavaPlugin(), location, itemStack, velocity, ITEM_DESPAWN);
    }
}
