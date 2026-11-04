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
package com.ricodev.lumen.economy;

import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.economy.EconomyProvider;
import com.ricodev.lumen.economy.type.BuiltInEconomy;
import com.ricodev.lumen.economy.type.PlayerPointsEconomy;
import com.ricodev.lumen.economy.type.VaultEconomy;

import java.util.function.Function;

public enum EconomyType {

    BUILT_IN("Built-In", BuiltInEconomy::new),
    VAULT("Vault", plugin -> new VaultEconomy()),
    PLAYER_POINTS("Player Points", plugin -> new PlayerPointsEconomy()),
    CUSTOM("Custom", null);

    private final String name;
    private final Function<Lumen, EconomyProvider> factory;

    EconomyType(String name, Function<Lumen, EconomyProvider> factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public EconomyProvider create(Lumen plugin) {
        return factory != null ? factory.apply(plugin) : null;
    }
}
