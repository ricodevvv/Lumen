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
package com.ricodev.lumen.cosmetic.pet;

import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.pet.Pet;
import com.ricodev.lumen.api.cosmetic.pet.PetBehavior;
import com.ricodev.lumen.api.cosmetic.pet.PetType;
import com.ricodev.lumen.api.event.CosmeticEntitySpawnEvent;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.Sounds;
import com.ricodev.lumen.cosmetic.CosmeticImpl;
import com.ricodev.lumen.util.MathUtil;
import com.ricodev.lumen.util.entity.Entities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PetImpl extends CosmeticImpl<PetType, PetBehavior> implements Pet {

    private static final double FOLLOW_DISTANCE = 2.5d;
    private static final double WALK_SPEED = 0.32d;
    private static final int UPDATE_INTERVAL = 10;

    protected Entity entity;
    protected Location location;
    private int ticks;

    public PetImpl(LumenPlugin plugin, User user, PetType type, PetBehavior behavior) {
        super(plugin, user, type, behavior);
    }

    @Override
    protected void onEquip() {
        spawn();
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    protected void onUpdate() {
        if (entity == null || !entity.isValid()) {
            return;
        }
        Entities.stepTowards(entity, player.getLocation(), FOLLOW_DISTANCE, WALK_SPEED);

        if (++ticks % UPDATE_INTERVAL != 0) {
            return;
        }
        if (cosmeticType.getTossItem() != null) {
            dropDespawningItem(cosmeticType.getTossItem());
        }
        behavior.onUpdate(this, entity);
    }

    @Override
    protected void onUnequip() {
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }

    @Override
    public void spawn() {
        spawn(player.getLocation());
    }

    @Override
    public void spawn(Location location) {
        if (entity != null) {
            behavior.onUnequip(this);
            onUnequip();
        }
        this.location = location.clone();

        Class<? extends Entity> entityClass = cosmeticType.getEntityType().getEntityClass();

        if (entityClass == null) {
            unequip(false, false);
            return;
        }
        entity = Entities.spawn(location, entityClass, spawned -> {
            Component nameTag = user.translate(
                    "cosmetic.pets.name_tag",
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.unparsed("cosmetic", cosmeticType.getName(user))
            );
            plugin.getPlatformAdapter().setCustomName(spawned, nameTag);
            spawned.setCustomNameVisible(!nameTag.equals(Component.empty()));
            Entities.makeCosmetic(spawned);
            Entities.applyLegacyVariant(spawned, ((PetTypeImpl) cosmeticType).getConfiguredEntity());

            if (spawned instanceof LivingEntity) {
                Compats.get().setScale((LivingEntity) spawned, cosmeticType.getScale());
            }
            if (cosmeticType.isBaby()) {
                if (spawned instanceof Ageable) {
                    Ageable ageable = (Ageable) spawned;
                    ageable.setBaby();
                    ageable.setAgeLock(true);
                    ageable.setBreed(false);
                } else if (spawned instanceof Zombie) {
                    ((Zombie) spawned).setBaby(true);
                }
            }
            behavior.onSetupEntity(this, spawned);
        });

        if (!entity.isValid()) {
            unequip(false, false);
            return;
        }
        Sounds.playAt(location, ((PetTypeImpl) cosmeticType).getSpawnXSound(), 0.5f, 1.0f);

        CosmeticEntitySpawnEvent event = new CosmeticEntitySpawnEvent(plugin, user, player, entity);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    protected void dropDespawningItem(ItemStack itemStack) {
        Vector velocity = new Vector(
                MathUtil.randomRange(-0.2d, 0.2d),
                MathUtil.randomRange(0.1d, 0.3d),
                MathUtil.randomRange(-0.2d, 0.2d)
        );
        Entities.dropDecorativeItem(plugin, entity.getLocation().add(0.0d, 0.4d, 0.0d), itemStack, velocity, 70);
    }
}
