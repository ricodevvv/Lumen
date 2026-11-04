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
package com.ricodev.lumen.storage;

import com.ricodev.lumen.api.util.BooleanIntPair;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.storage.Database;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.user.User;

import java.util.concurrent.CompletableFuture;

public abstract class DatabaseImpl implements Database {

    protected final LumenPlugin plugin;

    public DatabaseImpl(LumenPlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract CompletableFuture<BooleanIntPair> addCoinsAsyncImpl(User user, int amount);

    protected abstract CompletableFuture<BooleanIntPair> removeCoinsAsyncImpl(User user, int amount);

    protected abstract CompletableFuture<BooleanIntPair> setCoinsAsyncImpl(User user, int amount);

    protected abstract CompletableFuture<BooleanIntPair> addGadgetAmmoAsyncImpl(User user, GadgetType gadgetType, int amount);

    protected abstract CompletableFuture<BooleanIntPair> removeGadgetAmmoAsyncImpl(User user, GadgetType gadgetType, int amount);

    protected abstract CompletableFuture<BooleanIntPair> setGadgetAmmoAsyncImpl(User user, GadgetType gadgetType, int amount);

    protected abstract CompletableFuture<BooleanIntPair> addTreasureChestsAsyncImpl(User user, TreasureChest treasureChest, int amount);

    protected abstract CompletableFuture<BooleanIntPair> removeTreasureChestsAsyncImpl(User user, TreasureChest treasureChest, int amount);

    protected abstract CompletableFuture<BooleanIntPair> setTreasureChestsAsyncImpl(User user, TreasureChest treasureChest, int amount);

    @Override
    public CompletableFuture<BooleanIntPair> addCoinsAsync(User user, int amount) {
        return addCoinsAsyncImpl(user, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> removeCoinsAsync(User user, int amount) {
        return removeCoinsAsyncImpl(user, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> setCoinsAsync(User user, int amount) {
        return setCoinsAsyncImpl(user, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> addGadgetAmmoAsync(User user, GadgetType gadgetType, int amount) {
        return addGadgetAmmoAsyncImpl(user, gadgetType, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> removeGadgetAmmoAsync(User user, GadgetType gadgetType, int amount) {
        return removeGadgetAmmoAsyncImpl(user, gadgetType, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> setGadgetAmmoAsync(User user, GadgetType gadgetType, int amount) {
        return setGadgetAmmoAsyncImpl(user, gadgetType, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> addTreasureChestsAsync(User user, TreasureChest treasureChest, int amount) {
        return addTreasureChestsAsyncImpl(user, treasureChest, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> removeTreasureChestsAsync(User user, TreasureChest treasureChest, int amount) {
        return removeTreasureChestsAsyncImpl(user, treasureChest, amount);
    }

    @Override
    public CompletableFuture<BooleanIntPair> setTreasureChestsAsync(User user, TreasureChest treasureChest, int amount) {
        return setTreasureChestsAsyncImpl(user, treasureChest, amount);
    }
}
