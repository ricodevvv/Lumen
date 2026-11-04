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
package com.ricodev.lumen.api.cosmetic.registry;

import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.cosmetic.CosmeticBehavior;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectBehavior;
import com.ricodev.lumen.api.cosmetic.arroweffect.ArrowEffectType;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectBehavior;
import com.ricodev.lumen.api.cosmetic.deatheffect.DeathEffectType;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectBehavior;
import com.ricodev.lumen.api.cosmetic.particleeffect.ParticleEffectType;
import com.ricodev.lumen.api.cosmetic.pet.PetBehavior;
import com.ricodev.lumen.api.cosmetic.pet.PetType;

import java.util.Collection;

/**
 * Represents the central registry that holds all cosmetic categories.
 *
 * @see CosmeticCategory
 * @see CosmeticRegistry
 * @see CosmeticType
 */
public interface CategoryRegistries {

    /**
     * Registers a new cosmetic category into the registry.
     *
     * @param registry the category to register
     * @param <T>      the cosmetic type contained within the category
     * @param <B>      the behavior type associated with the cosmetic
     * @param <U>      the builder type used to construct the cosmetic type
     */
    <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>>
    void register(CosmeticCategory<T, B, U> registry);

    /**
     * Retrieves a cosmetic category by its unique key.
     *
     * @param key the category key
     * @param <T> the cosmetic type contained within the category
     * @param <B> the behavior type associated with the cosmetic
     * @param <U> the builder type used to construct the cosmetic type
     * @return the matching {@link CosmeticCategory}, or {@code null} if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>> CosmeticCategory<T, B, U> getCategory(String key) {
        return (CosmeticCategory<T, B, U>) getCategoryRaw(key);
    }

    /**
     * Checks whether a category with the given key exists in the registry.
     *
     * @param key the category key
     * @return {@code true} if a category with the key exists, otherwise {@code false}
     */
    default boolean hasRegistry(String key) {
        return getCategoryRaw(key) != null;
    }

    /**
     * Retrieves a cosmetic category without generic type safety.
     *
     * @param key the category key
     * @return the category, or {@code null} if not found
     */
    @Nullable
    CosmeticCategory<? extends CosmeticType<?, ?>,
            ? extends CosmeticBehavior<?>,
            ? extends CosmeticType.Builder<?, ?, ?>> getCategoryRaw(String key);

    /**
     * Returns all registered cosmetic categories.
     *
     * @return a collection of all registered {@link CosmeticCategory} instances
     */
    Collection<CosmeticCategory<? extends CosmeticType<?, ?>,
            ? extends CosmeticBehavior<?>,
            ? extends CosmeticType.Builder<?, ?, ?>>> getCategories();

    /**
     * Gets the arrow effects cosmetic category.
     *
     * @return the built-in {@code arrow_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> arrowEffects() {
        return getCategory(BuiltIn.ARROW_EFFECTS);
    }

    /**
     * Gets the death effects cosmetic category.
     *
     * @return the built-in {@code death_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> deathEffects() {
        return getCategory(BuiltIn.DEATH_EFFECTS);
    }

    /**
     * Gets the gadgets cosmetic category.
     *
     * @return the built-in {@code gadgets} category, or {@code null} if not registered
     */
    default CosmeticCategory<GadgetType, GadgetBehavior, GadgetType.Builder> gadgets() {
        return getCategory(BuiltIn.GADGETS);
    }

    /**
     * Gets the particle effects cosmetic category.
     *
     * @return the built-in {@code particle_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> particleEffects() {
        return getCategory(BuiltIn.PARTICLE_EFFECTS);
    }

    /**
     * Gets the pets cosmetic category.
     *
     * @return the built-in {@code pets} category, or {@code null} if not registered
     */
    default CosmeticCategory<PetType, PetBehavior, PetType.Builder> pets() {
        return getCategory(BuiltIn.PETS);
    }

    /**
     * Defines constant keys for all built-in cosmetic categories.
     */
    interface BuiltIn {

        /**
         * Built-in key for arrow effects.
         */
        String ARROW_EFFECTS = "arrow_effects";

        /**
         * Built-in key for balloons.
         */

        /**
         * Built-in key for banners.
         */

        /**
         * Built-in key for death effects.
         */
        String DEATH_EFFECTS = "death_effects";

        /**
         * Built-in key for emotes.
         */

        /**
         * Built-in key for gadgets.
         */
        String GADGETS = "gadgets";

        /**
         * Built-in key for miniatures.
         */

        /**
         * Built-in key for morphs.
         */

        /**
         * Built-in key for mounts.
         */

        /**
         * Built-in key for music cosmetics.
         */

        /**
         * Built-in key for particle effects.
         */
        String PARTICLE_EFFECTS = "particle_effects";

        /**
         * Built-in key for pets.
         */
        String PETS = "pets";

        /**
         * Built-in key for statuses.
         */
    }
}
