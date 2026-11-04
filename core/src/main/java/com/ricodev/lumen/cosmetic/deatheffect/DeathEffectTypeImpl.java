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
package com.ricodev.lumen.cosmetic.deatheffect;

import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffect;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectBehavior;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class DeathEffectTypeImpl extends CosmeticTypeImpl<DeathEffectType, DeathEffectBehavior> implements DeathEffectType {

    public DeathEffectTypeImpl(String key,
                               CosmeticCategory<DeathEffectType, DeathEffectBehavior, ?> category,
                               Supplier<DeathEffectBehavior> behaviorFactory,
                               boolean enabled,
                               boolean purchasable,
                               int cost,
                               CosmeticRarity rarity,
                               ItemStack itemStack,
                               List<String> treasureChests) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
    }

    @Override
    protected DeathEffect createInstance(LumenPlugin plugin, User user, DeathEffectBehavior behavior) {
        return new DeathEffectImpl(plugin, user, this, behavior);
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> implements DeathEffectType.Builder {

        public BuilderImpl(String key, CosmeticCategory<DeathEffectType, DeathEffectBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected DeathEffectType.Builder self() {
            return this;
        }

        @Override
        public DeathEffectType build() {
            return new DeathEffectTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests
            );
        }
    }
}
