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
package com.ricodev.lumen.cosmetic.registry;

import com.ricodev.lumen.util.TriFunction;
import org.bukkit.entity.Player;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.CosmeticBehavior;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticRegistry;
import com.ricodev.lumen.api.menu.CosmeticMenu;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;
import com.ricodev.lumen.menu.CosmeticMenuImpl;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.Collection;
import java.util.function.BiFunction;

public class CosmeticCategoryImpl<T extends CosmeticType<T, B>,
        B extends CosmeticBehavior<T>,
        U extends CosmeticType.Builder<T, B, U>>
        implements CosmeticCategory<T, B, U> {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();

    private final String key;
    private final CosmeticRegistry<T, B, U> registry;
    private final Config config;
    private final boolean enabled;
    private final String permission;
    private final String purchasePermission;
    private final ItemBuilderImpl menuItem;
    private final TriFunction<Lumen, User, CosmeticCategory<T, B, U>, CosmeticMenuImpl<T>> menuFactory;

    public CosmeticCategoryImpl(String key,
                                BiFunction<String, CosmeticCategory<T, B, U>, U> builderFactory,
                                TriFunction<Lumen, User, CosmeticCategory<T, B, U>, CosmeticMenuImpl<T>> menuFactory) {
        this.key = key.toLowerCase();
        this.registry = new CosmeticRegistryImpl<>(this, builderFactory);
        this.config = PLUGIN.getConfigManager().register(key);
        this.enabled = config.getBoolean("enabled");
        this.permission = "lumen.cosmetic." + key + ".*";
        this.purchasePermission = "lumen.purchase." + key + ".*";
        this.menuItem = new ItemBuilderImpl(PLUGIN.getConfigManager().getMainConfig(), "menu.main.items." + key);
        this.menuFactory = menuFactory;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public CosmeticRegistry<T, B, U> getCosmeticRegistry() {
        return registry;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getPurchasePermission() {
        return purchasePermission;
    }

    @Override
    public ItemBuilder getMenuItem() {
        return menuItem;
    }

    @Override
    public CosmeticMenu<T> createMenu(Lumen plugin, User user) {
        return menuFactory.apply(plugin, user, this);
    }

    @Override
    public int getUnlockedCosmetics(Player player) {
        Collection<? extends CosmeticType<?, ?>> cosmeticTypes = registry.getEnabledTypes();

        if (player.hasPermission(CosmeticTypeImpl.PERMISSION_ALL_COSMETICS) || player.hasPermission(permission)) {
            return cosmeticTypes.size();
        }
        int i = 0;

        for (CosmeticType<?, ?> cosmeticType : cosmeticTypes) {
            if (cosmeticType.hasPermission(player)) {
                i++;
            }
        }
        return i;
    }
}
