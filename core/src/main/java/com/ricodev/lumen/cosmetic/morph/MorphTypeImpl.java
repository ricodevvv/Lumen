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

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.morph.Morph;
import com.ricodev.lumen.api.cosmetic.morph.MorphBehavior;
import com.ricodev.lumen.api.cosmetic.morph.MorphType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class MorphTypeImpl extends CosmeticTypeImpl<MorphType, MorphBehavior> implements MorphType {

    private final double cooldown;
    private final EntityType entityType;
    private final boolean ability;

    public MorphTypeImpl(String key,
                         CosmeticCategory<MorphType, MorphBehavior, ?> category,
                         Supplier<MorphBehavior> behaviorFactory,
                         boolean enabled,
                         boolean purchasable,
                         int cost,
                         CosmeticRarity rarity,
                         ItemStack itemStack,
                         List<String> treasureChests,
                         double cooldown,
                         EntityType entityType,
                         boolean ability) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.cooldown = cooldown;
        this.entityType = entityType;
        this.ability = ability;
    }

    @Override
    protected Morph createInstance(LumenPlugin plugin, User user, MorphBehavior behavior) {
        return new MorphImpl(plugin, user, this, behavior);
    }

    @Override
    public double getCooldown() {
        return cooldown;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean hasAbility() {
        return ability;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<MorphType, MorphBehavior, MorphType.Builder> implements MorphType.Builder {

        private double cooldown;
        private EntityType entityType;
        private boolean ability = false;

        public BuilderImpl(String key, CosmeticCategory<MorphType, MorphBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected MorphType.Builder self() {
            return this;
        }

        @Override
        public MorphType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            cooldown = Math.max(0.1d, config.getDouble(path + "cooldown"));
            ability = config.getBoolean(path + "ability");

            return self();
        }

        @Override
        public MorphType.Builder cooldown(double cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        @Override
        public MorphType.Builder entityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        @Override
        public MorphType.Builder ability(boolean ability) {
            this.ability = ability;
            return this;
        }

        @Override
        public MorphType build() {
            return new MorphTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    cooldown,
                    entityType,
                    ability
            );
        }
    }
}
