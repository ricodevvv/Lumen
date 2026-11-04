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
package com.ricodev.lumen.cosmetic.miniature.type;

import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureBehavior;
import com.ricodev.lumen.api.cosmetic.miniature.MiniatureType;
import com.ricodev.lumen.api.entity.CosmeticEntity;

public class Snail implements MiniatureBehavior {

    @Override
    public void onEquip(CosmeticContext<MiniatureType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<MiniatureType> context, CosmeticEntity nmsEntity) {
    }

    @Override
    public void onUpdate(CosmeticContext<MiniatureType> context, CosmeticEntity nmsEntity, int tick) {
    }

    @Override
    public void onUnequip(CosmeticContext<MiniatureType> context) {
    }
}
