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
package com.ricodev.lumen.treasure;

import com.cryptomorin.xseries.XMaterial;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.CosmeticType;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.locale.Translator;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.treasure.animation.TreasureChestAnimationRegistry;
import com.ricodev.lumen.api.treasure.loot.LootCategory;
import com.ricodev.lumen.api.treasure.loot.LootEntry;
import com.ricodev.lumen.api.treasure.loot.LootTable;
import com.ricodev.lumen.api.treasure.loot.number.ConstantIntProvider;
import com.ricodev.lumen.api.treasure.loot.number.IntProvider;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.api.util.structure.StructureData;
import com.ricodev.lumen.treasure.loot.LootCategoryImpl;
import com.ricodev.lumen.treasure.loot.LootTableImpl;
import com.ricodev.lumen.treasure.loot.number.ConstantIntProviderImpl;
import com.ricodev.lumen.treasure.loot.number.RangedIntProviderImpl;
import com.ricodev.lumen.treasure.loot.type.AmmoLootImpl;
import com.ricodev.lumen.treasure.loot.type.CoinsLootImpl;
import com.ricodev.lumen.treasure.loot.type.CosmeticLootImpl;
import com.ricodev.lumen.treasure.loot.type.CustomLootImpl;
import com.ricodev.lumen.util.item.ItemBuilderImpl;
import com.ricodev.lumen.util.structure.StructureDataImpl;
import com.ricodev.lumen.util.structure.StructureReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TreasureChestImpl implements TreasureChest {

    private final LumenPlugin plugin;
    private final String key;
    private final boolean enabled;
    private final int priority;
    private final boolean purchasable;
    private final String purchasePermission;
    private final int cost;
    private final int chestsToOpen;
    private final TreasureChestAnimationRegistry.AnimationFactory animationFactory;
    private final boolean openingBroadcast;
    private final ItemBuilder itemBuilder;
    private final int page;
    private final List<StructureData> structures = new ArrayList<>();
    private final LootTable lootTable;

    public TreasureChestImpl(LumenPlugin plugin, Config config, String key) {
        this.plugin = plugin;
        this.key = key;

        String path = "treasure_chests." + key;
        enabled = config.getBoolean(path + ".enabled");
        priority = config.getInt(path + ".priority");
        purchasable = config.getBoolean(path + ".purchasable.enabled");
        purchasePermission = "lumen.purchase.treasure_chest." + key;
        cost = config.getInt(path + ".purchasable.cost");
        chestsToOpen = config.getInt(path + ".chests_to_open");
        animationFactory = plugin.getTreasureChestAnimationRegistry().get(config.getString(path + ".chest_animation"));
        openingBroadcast = config.getBoolean(path + ".opening_broadcast");
        itemBuilder = new ItemBuilderImpl(config, path);
        page = Math.max(1, config.getInt(path + ".page"));

        for (String layout : config.getStringList(path + ".animation_layouts")) {
            StructureDataImpl structureData = StructureReader.loadStructure(layout);

            if (structureData == null) {
                plugin.getLogger().log(Level.WARNING, "Failed to load animation layout " + layout + ". Skipping");
                continue;
            }
            structures.add(structureData);
        }
        Map<CosmeticRarity, Integer> rarityWeights = loadRarityWeights(config, path);

        List<LootEntry> entries = new ArrayList<>();
        loadCosmeticLoot(entries);
        loadGadgetAmmoLoot(config, entries);
        loadCoinLoot(config, path, entries);
        loadCustomLoot(config, path, entries);

        pruneUnusedRarities(rarityWeights, entries);

        this.lootTable = new LootTableImpl(entries, rarityWeights);
    }

    private Map<CosmeticRarity, Integer> loadRarityWeights(Config config, String path) {
        Map<CosmeticRarity, Integer> weights = new HashMap<>();
        String rarityWeightsPath = path + ".rarity_weights";

        for (String rarityKey : config.getSectionKeys(rarityWeightsPath)) {
            CosmeticRarity rarity = plugin.getCosmeticRarityRegistry().getSafely(rarityKey);
            int weight = config.getInt(rarityWeightsPath + "." + rarityKey);

            if (weight > 0) {
                weights.put(rarity, weight);
            }
        }
        return weights;
    }

    private void loadCosmeticLoot(List<LootEntry> entries) {
        ConstantIntProvider ONE_PROVIDER = new ConstantIntProviderImpl(1);

        for (CosmeticCategory<?, ?, ?> cosmeticCategory : plugin.getCategoryRegistries().getCategories()) {
            LootCategory category = new LootCategoryImpl(cosmeticCategory.getKey(), cosmeticCategory.getMenuItem());

            for (CosmeticType<?, ?> cosmeticType : cosmeticCategory.getCosmeticRegistry().getEnabledTypes()) {
                if (cosmeticType.getTreasureChests().contains(key)) {
                    entries.add(new CosmeticLootImpl(ONE_PROVIDER, category, cosmeticType));
                }
            }
        }
    }

    private void loadGadgetAmmoLoot(Config config, List<LootEntry> entries) {
        LootCategory category = new LootCategoryImpl("ammo", new ItemBuilderImpl(config, "loot_categories.items.ammo"));

        for (GadgetType gadgetType : plugin.getCategoryRegistries().gadgets().getCosmeticRegistry().getEnabledTypes()) {
            Map<String, IntProvider> ammoLoot = gadgetType.getAmmoLoot();

            if (ammoLoot.containsKey(key)) {
                entries.add(new AmmoLootImpl(
                        ammoLoot.get(key),
                        category,
                        gadgetType
                ));
            }
        }
    }

    private void loadCoinLoot(Config config, String path, List<LootEntry> entries) {
        String coinsPath = path + ".coins.";

        if (!config.getBoolean(coinsPath + "enabled")) {
            return;
        }
        CosmeticRarity rarity = plugin.getCosmeticRarityRegistry().getSafely(config.getString(coinsPath + "rarity"));
        LootCategory category = new LootCategoryImpl("coins", new ItemBuilderImpl(XMaterial.SUNFLOWER.parseItem()));

        int min = config.getInt(coinsPath + "minimum_amount");
        int max = config.getInt(coinsPath + "maximum_amount");

        entries.add(new CoinsLootImpl(new RangedIntProviderImpl(min, max), category, rarity));
    }

    private void loadCustomLoot(Config config, String path, List<LootEntry> entries) {
        Map<String, LootCategory> customCategories = loadCustomCategories(config, path);

        String customLootPath = path + ".custom.loot";
        ConstantIntProvider ONE_PROVIDER = new ConstantIntProviderImpl(1);

        for (String customKey : config.getSectionKeys(customLootPath)) {
            String customPath = customLootPath + "." + customKey + ".";

            if (!config.getBoolean(customPath + "enabled")) {
                continue;
            }
            CosmeticRarity rarity = plugin.getCosmeticRarityRegistry().getSafely(config.getString(customPath + "rarity"));
            String categoryKey = config.getString(customPath + "category");
            LootCategory category = customCategories.get(categoryKey);

            if (category == null) {
                plugin.getLogger().warning("Could not find category " + categoryKey + " for " + customKey + ".");
                continue;
            }
            ItemBuilderImpl customItemBuilder = new ItemBuilderImpl(config, customPath);
            entries.add(new CustomLootImpl(
                    ONE_PROVIDER,
                    category,
                    customKey,
                    customItemBuilder.getItemStack(),
                    rarity,
                    config.getStringList(customPath + "commands")
            ));
        }
    }

    private Map<String, LootCategory> loadCustomCategories(Config config, String path) {
        Map<String, LootCategory> customCategories = new HashMap<>();
        String customCategoriesPath = path + ".custom.categories";

        for (String categoryKey : config.getSectionKeys(customCategoriesPath)) {
            String categoryPath = customCategoriesPath + "." + categoryKey + ".";
            ItemBuilderImpl categoryItem = new ItemBuilderImpl(config, categoryPath);
            customCategories.put(categoryKey, new LootCategoryImpl(categoryKey, categoryItem));
        }
        return customCategories;
    }

    private void pruneUnusedRarities(Map<CosmeticRarity, Integer> rarityWeights, List<LootEntry> entries) {
        rarityWeights.entrySet().removeIf(entry ->
                entries.stream().noneMatch(lootEntry -> lootEntry.getRarity() == entry.getKey())
        );
    }

    @Override
    public LootTable getLootTable() {
        return lootTable;
    }

    @Override
    public boolean hasPurchasePermission(Player player) {
        return player.hasPermission(purchasePermission);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName(Translator translator) {
        return translator.translateRaw("treasure_chest." + key);
    }

    @Override
    public TagResolver getResolvers(User user) {
        return TagResolver.resolver(
                Placeholder.unparsed("current", String.valueOf(user.getTreasureChests(this))),
                Placeholder.unparsed("cost", String.valueOf(cost)),
                Placeholder.unparsed("currency", user.translateRaw("generic.currency"))
        );
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public boolean isPurchasable() {
        return purchasable;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public int getChestsToOpen() {
        return chestsToOpen;
    }

    @Override
    public TreasureChestAnimationRegistry.AnimationFactory getAnimationFactory() {
        return animationFactory;
    }

    @Override
    public List<StructureData> getStructures() {
        return structures;
    }

    @Override
    public boolean isOpeningBroadcast() {
        return openingBroadcast;
    }

    @Override
    public ItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    @Override
    public ItemStack getItemStack() {
        return itemBuilder.getItemStack();
    }
}
