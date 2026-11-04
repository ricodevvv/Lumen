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
package com.ricodev.lumen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Java 9+ collection factories, for code that has to run on the Java 8 that 1.8 servers use.
 * Everything returned is unmodifiable, same as the real ones.
 */
public final class Collects {

    private Collects() {
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        return values.length == 0 ? Collections.<T>emptyList() : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(values)));
    }

    public static <T> List<T> copyOf(Collection<? extends T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    public static <T> Set<T> copySet(Collection<? extends T> values) {
        return Collections.unmodifiableSet(new HashSet<>(values));
    }

    public static <K, V> Map<K, V> mapOf() {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> mapOf(K key, V value) {
        return Collections.singletonMap(key, value);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> copyMap(Map<? extends K, ? extends V> map) {
        return Collections.unmodifiableMap(new HashMap<>(map));
    }

    public static String repeat(String value, int times) {
        StringBuilder builder = new StringBuilder(value.length() * Math.max(0, times));

        for (int i = 0; i < times; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
