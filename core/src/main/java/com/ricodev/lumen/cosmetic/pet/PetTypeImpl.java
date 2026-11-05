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

import com.cryptomorin.xseries.XSound;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.pet.Pet;
import com.ricodev.lumen.api.cosmetic.pet.PetBehavior;
import com.ricodev.lumen.api.cosmetic.pet.PetType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class PetTypeImpl extends CosmeticTypeImpl<PetType, PetBehavior> implements PetType {

    private final EntityType entityType;
    private final boolean baby;
    private final double scale;
    private final XSound spawnSound;
    private String configuredEntity;
    private final ItemStack tossItem;

    public PetTypeImpl(String key,
                       CosmeticCategory<PetType, PetBehavior, ?> category,
                       Supplier<PetBehavior> behaviorFactory,
                       boolean enabled,
                       boolean purchasable,
                       int cost,
                       CosmeticRarity rarity,
                       ItemStack itemStack,
                       List<String> treasureChests,
                       EntityType entityType,
                       boolean baby,
                       double scale,
                       @Nullable XSound spawnSound,
                       @Nullable ItemStack tossItem) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.entityType = entityType;
        this.baby = baby;
        this.scale = scale;
        this.spawnSound = spawnSound;
        this.tossItem = tossItem;
    }

    @Override
    protected Pet createInstance(LumenPlugin plugin, User user, PetBehavior behavior) {
        return new PetImpl(plugin, user, this, behavior);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean isBaby() {
        return baby;
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    @Nullable
    public Sound getSpawnSound() {
        return spawnSound == null ? null : spawnSound.get();
    }

    /**
     * The entity name as written in the config, which may differ from the spawned type on old versions.
     */
    @Nullable
    public String getConfiguredEntity() {
        return configuredEntity;
    }

    @Nullable
    public XSound getSpawnXSound() {
        return spawnSound;
    }

    @Override
    @Nullable
    public ItemStack getTossItem() {
        return tossItem;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<PetType, PetBehavior, PetType.Builder> implements PetType.Builder {

        /**
         * Pets whose mob doesn't exist on this server version, logged once after loading.
         */
        public static final java.util.List<String> UNSUPPORTED = new java.util.ArrayList<>();

        private EntityType entityType;
        private boolean baby;
        private double scale = 1.0d;
        private XSound spawnSound;
        private String configuredEntity;
        private ItemStack tossItem;

        public BuilderImpl(String key, CosmeticCategory<PetType, PetBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected PetType.Builder self() {
            return this;
        }

        @Override
        public PetType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            configuredEntity = config.getString(path + "entity");
            entityType = parseEntity(path + "entity");

            if (entityType == null) {
                UNSUPPORTED.add(key);
                enabled = false;
                entityType = EntityType.PIG;
            }
            baby = config.getBoolean(path + "baby");
            scale = config.getDouble(path + "scale");
            spawnSound = parseSound(path + "spawn_sound");

            String itemPath = path + "toss_item";

            if (config.getBoolean(itemPath + ".enabled")) {
                tossItem = new ItemBuilderImpl(config, itemPath).getItemStack();
            }
            return this;
        }

        public PetType.Builder entityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public PetType.Builder baby(boolean baby) {
            this.baby = baby;
            return this;
        }

        public PetType.Builder scale(double scale) {
            this.scale = scale;
            return this;
        }

        public PetType.Builder spawnSound(@Nullable Sound spawnSound) {
            this.spawnSound = spawnSound == null ? null : XSound.of(spawnSound);
            return this;
        }

        public PetType.Builder tossItem(@Nullable ItemStack tossItem) {
            this.tossItem = tossItem;
            return this;
        }

        @Override
        public PetType build() {
            PetTypeImpl type = new PetTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    entityType,
                    baby,
                    scale,
                    spawnSound,
                    tossItem
            );
            type.configuredEntity = configuredEntity;
            return type;
        }
    }
}
