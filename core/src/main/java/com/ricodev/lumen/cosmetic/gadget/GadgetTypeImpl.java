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
package com.ricodev.lumen.cosmetic.gadget;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.gadget.Gadget;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GadgetTypeImpl extends CosmeticTypeImpl<GadgetType, GadgetBehavior> implements GadgetType {

    private final double cooldown;
    private final double duration;
    private final CosmeticRarity ammoRarity;
    private final boolean infinityAmmo;
    private final boolean purchasableAmmo;
    private final int ammoPurchaseAmount;
    private final int ammoCost;

    public GadgetTypeImpl(String key,
                          CosmeticCategory<GadgetType, GadgetBehavior, ?> category,
                          Supplier<GadgetBehavior> behaviourFactory,
                          boolean enabled,
                          boolean purchasable,
                          int cost,
                          CosmeticRarity rarity,
                          ItemStack itemStack,
                          List<String> treasureChests,
                          double cooldown,
                          double duration,
                          CosmeticRarity ammoRarity,
                          boolean infinityAmmo,
                          boolean purchasableAmmo,
                          int ammoPurchaseAmount,
                          int ammoCost) {
        super(key, category, behaviourFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.cooldown = cooldown;
        this.duration = duration;
        this.ammoRarity = ammoRarity;
        this.infinityAmmo = infinityAmmo;
        this.purchasableAmmo = purchasableAmmo;
        this.ammoPurchaseAmount = ammoPurchaseAmount;
        this.ammoCost = ammoCost;
    }

    @Override
    protected Gadget createInstance(LumenPlugin plugin, User user, GadgetBehavior behaviour) {
        return new GadgetImpl(plugin, user, this, behaviour);
    }

    @Override
    public TagResolver getResolvers(User user) {
        return TagResolver.resolver(
                super.getResolvers(user),
                Placeholder.unparsed("ammo", String.valueOf(user.getAmmo(this))),
                Placeholder.unparsed("ammo_cost", String.valueOf(ammoCost)),
                Placeholder.unparsed("ammo_purchase_amount", String.valueOf(ammoPurchaseAmount))
        );
    }

    @Override
    public double getCooldown() {
        return cooldown;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public CosmeticRarity getAmmoRarity() {
        return ammoRarity;
    }

    @Override
    public boolean hasInfinityAmmo() {
        return infinityAmmo;
    }

    @Override
    public boolean hasPurchasableAmmo() {
        return purchasableAmmo;
    }

    @Override
    public int getAmmoPurchaseAmount() {
        return ammoPurchaseAmount;
    }

    @Override
    public int getAmmoCost() {
        return ammoCost;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<GadgetType, GadgetBehavior, GadgetType.Builder> implements GadgetType.Builder {

        private double cooldown;
        private double duration;
        private CosmeticRarity ammoRarity;
        private boolean infinityAmmo;
        private boolean purchasableAmmo;
        private int ammoPurchaseAmount;
        private int ammoCost;

        public BuilderImpl(String key, CosmeticCategory<GadgetType, GadgetBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected GadgetType.Builder self() {
            return this;
        }

        @Override
        public GadgetType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            cooldown = Math.max(0.1d, config.getDouble(path + "cooldown"));
            duration = Math.min(cooldown, config.getDouble(path + "duration"));

            ammoRarity = PLUGIN.getCosmeticRarityRegistry().getSafely(config.getString(path + "ammo.rarity"));
            infinityAmmo = config.getBoolean(path + "ammo.infinity");
            purchasableAmmo = config.getBoolean(path + "ammo.purchasable.enabled");
            ammoPurchaseAmount = Math.max(0, config.getInt(path + "ammo.purchasable.amount"));
            ammoCost = Math.max(0, config.getInt(path + "ammo.purchasable.cost"));

            return self();
        }

        @Override
        public GadgetType.Builder cooldown(double cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        @Override
        public GadgetType.Builder duration(double duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public GadgetType.Builder ammoRarity(CosmeticRarity rarity) {
            this.ammoRarity = rarity;
            return this;
        }

        @Override
        public GadgetType.Builder infinityAmmo(boolean infinityAmmo) {
            this.infinityAmmo = infinityAmmo;
            return this;
        }

        @Override
        public GadgetType.Builder purchasableAmmo(boolean purchasableAmmo) {
            this.purchasableAmmo = purchasableAmmo;
            return this;
        }

        @Override
        public GadgetType.Builder ammoPurchaseAmount(int ammoPurchaseAmount) {
            this.ammoPurchaseAmount = ammoPurchaseAmount;
            return this;
        }

        @Override
        public GadgetType.Builder ammoCost(int ammoCost) {
            this.ammoCost = ammoCost;
            return this;
        }

        @Override
        public GadgetType build() {
            return new GadgetTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    cooldown,
                    duration,
                    ammoRarity,
                    infinityAmmo,
                    purchasableAmmo,
                    ammoPurchaseAmount,
                    ammoCost
            );
        }
    }
}
