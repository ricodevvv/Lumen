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
package com.ricodev.lumen.cosmetic.particleeffect;

import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffect;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectBehavior;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class ParticleEffectTypeImpl extends CosmeticTypeImpl<ParticleEffectType, ParticleEffectBehavior> implements ParticleEffectType {

    private final int repeatDelay;

    public ParticleEffectTypeImpl(String key,
                                  CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ?> category,
                                  Supplier<ParticleEffectBehavior> behaviorFactory,
                                  boolean enabled,
                                  boolean purchasable,
                                  int cost,
                                  CosmeticRarity rarity,
                                  ItemStack itemStack,
                                  List<String> treasureChests,
                                  int repeatDelay) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.repeatDelay = repeatDelay;
    }

    @Override
    protected ParticleEffect createInstance(LumenPlugin plugin, User user, ParticleEffectBehavior behavior) {
        return new ParticleEffectImpl(plugin, user, this, behavior);
    }

    @Override
    public int getRepeatDelay() {
        return repeatDelay;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> implements ParticleEffectType.Builder {

        private int repeatDelay = 1;

        public BuilderImpl(String key, CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected ParticleEffectType.Builder self() {
            return this;
        }

        @Override
        public ParticleEffectType.Builder repeatDelay(int repeatDelay) {
            this.repeatDelay = repeatDelay;
            return this;
        }

        @Override
        public ParticleEffectType build() {
            return new ParticleEffectTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    repeatDelay
            );
        }
    }
}
