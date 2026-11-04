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
package com.ricodev.lumen.cosmetic.status;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.cosmetic.status.Status;
import com.ricodev.lumen.api.cosmetic.status.StatusBehavior;
import com.ricodev.lumen.api.cosmetic.status.StatusType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class StatusTypeImpl extends CosmeticTypeImpl<StatusType, StatusBehavior> implements StatusType {

    private final long refreshInterval;
    private final BiFunction<StatusType, User, Component> textProvider;

    public StatusTypeImpl(String key,
                          CosmeticCategory<StatusType, StatusBehavior, ?> category,
                          Supplier<StatusBehavior> behaviorFactory,
                          boolean enabled,
                          boolean purchasable,
                          int cost,
                          CosmeticRarity rarity,
                          ItemStack itemStack,
                          List<String> treasureChests,
                          long refreshInterval,
                          BiFunction<StatusType, User, Component> textProvider) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.refreshInterval = refreshInterval;
        this.textProvider = textProvider;
    }

    @Override
    protected Status createInstance(LumenPlugin plugin, User user, StatusBehavior behavior) {
        return new StatusImpl(plugin, user, this, behavior);
    }

    @Override
    public long getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public BiFunction<StatusType, User, Component> getTextProvider() {
        return textProvider;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<StatusType, StatusBehavior, StatusType.Builder> implements StatusType.Builder {

        private long refreshInterval = -1L;
        private BiFunction<StatusType, User, Component> textProvider;

        public BuilderImpl(String key, CosmeticCategory<StatusType, StatusBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected StatusType.Builder self() {
            return this;
        }

        @Override
        public StatusType.Builder readFromConfig() {
            super.readFromConfig();

            refreshInterval = category.getConfig().getInt(getPath() + "refresh_interval");
            textProvider = (statusType, user) -> user.translate(
                    "cosmetic." + category.getKey() + "." + key + ".tag",
                    Placeholder.unparsed("name", statusType.getName(user))
            );
            return this;
        }

        @Override
        public StatusType.Builder refreshInterval(long refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }

        @Override
        public StatusType.Builder textProvider(BiFunction<StatusType, User, Component> textProvider) {
            this.textProvider = textProvider;
            return this;
        }

        @Override
        public StatusType build() {
            return new StatusTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    refreshInterval,
                    textProvider
            );
        }
    }
}
