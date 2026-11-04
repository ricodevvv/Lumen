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
package com.ricodev.lumen.api.util;

/**
 * A success flag paired with a number, like "did it work" and "what's the new balance".
 */
public final class BooleanIntPair {

    private final boolean left;
    private final int right;

    private BooleanIntPair(boolean left, int right) {
        this.left = left;
        this.right = right;
    }

    public static BooleanIntPair of(boolean left, int right) {
        return new BooleanIntPair(left, right);
    }

    public boolean leftBoolean() {
        return left;
    }

    public int rightInt() {
        return right;
    }
}
