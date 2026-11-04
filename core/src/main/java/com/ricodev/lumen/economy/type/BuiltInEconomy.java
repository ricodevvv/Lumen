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
package com.ricodev.lumen.economy.type;

import com.ricodev.lumen.api.util.BooleanIntPair;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.economy.EconomyProvider;
import com.ricodev.lumen.api.user.User;

import java.util.concurrent.CompletableFuture;

public class BuiltInEconomy implements EconomyProvider {

    private final Lumen plugin;

    public BuiltInEconomy(Lumen plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPlugin() {
        return plugin.getJavaPlugin().getName();
    }

    @Override
    public void hook(Lumen plugin) throws IllegalStateException {
    }

    @Override
    public int getCoins(User user) {
        return user.getCoins();
    }

    @Override
    public CompletableFuture<BooleanIntPair> getCoinsAsync(User user) {
        return CompletableFuture.completedFuture(BooleanIntPair.of(true, getCoins(user)));
    }

    @Override
    public CompletableFuture<Boolean> addCoinsAsync(User user, int amount) {
        return plugin.getStorage().addCoinsAsync(user, amount).thenApply(BooleanIntPair::leftBoolean);
    }

    @Override
    public CompletableFuture<Boolean> setCoinsAsync(User user, int amount) {
        return plugin.getStorage().setCoinsAsync(user, amount).thenApply(BooleanIntPair::leftBoolean);
    }

    @Override
    public CompletableFuture<Boolean> removeCoinsAsync(User user, int amount) {
        return plugin.getStorage().removeCoinsAsync(user, amount).thenApply(BooleanIntPair::leftBoolean);
    }
}
