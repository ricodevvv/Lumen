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
package com.ricodev.lumen.menu.menus.purchase;

import com.ricodev.lumen.util.Cooldowns;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.config.Config;
import com.ricodev.lumen.api.economy.EconomyProvider;
import com.ricodev.lumen.api.event.PlayerPurchaseTreasureChestEvent;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.menu.MenuImpl;
import com.ricodev.lumen.menu.menus.TreasureChestMenu;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.logging.Level;

public class TreasurePurchaseMenu extends MenuImpl {

    private static final int COOLDOWN = 20;

    private final TreasureChest treasureChest;
    private final Config config;
    private int amount = 1;

    public TreasurePurchaseMenu(Lumen plugin, User user, TreasureChest treasureChest) {
        super(plugin, user,
                user.translate("menu.purchase.treasure_chest.title"),
                plugin.getConfigManager().getMainConfig().getInt("menu.purchase.treasure_chest.rows")
        );
        this.treasureChest = treasureChest;
        this.config = plugin.getConfigManager().getMainConfig();
    }

    @Override
    protected void addItems() {
        ItemBuilder treasureChestItem = new ItemBuilderImpl(treasureChest.getItemStack());
        String name = treasureChest.getName(user);
        int cost = treasureChest.getCost() * amount;
        TagResolver tagResolver = TagResolver.resolver(
                treasureChest.getResolvers(user),
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("cost", String.valueOf(cost)),
                Placeholder.unparsed("amount", String.valueOf(amount))
        );

        treasureChestItem.setDisplayName(user.translate(
                "menu.purchase.treasure_chest.item.name",
                tagResolver
        ));
        treasureChestItem.setLore(user.translateList(
                "menu.purchase.treasure_chest.item.desc",
                tagResolver
        ));
        treasureChestItem.setAmount(amount);

        setItem(13, treasureChestItem.getItemStack(), event -> {
            if (event.isLeftClick()) {
                if (amount < 64) {
                    amount++;
                    playClickSound();
                    addItems();
                } else {
                    playDenySound();
                }
            } else {
                if (amount > 1) {
                    amount--;
                    playClickSound();
                    addItems();
                } else {
                    playDenySound();
                }
            }
        });

        ItemBuilder acceptPurchase = new ItemBuilderImpl(config, "menu.purchase.treasure_chest.items.accept");
        acceptPurchase.setDisplayName(user.translate(
                "menu.purchase.treasure_chest.accept.name",
                tagResolver
        ));
        acceptPurchase.setLore(user.translateList(
                "menu.purchase.treasure_chest.accept.desc",
                tagResolver
        ));
        Cooldowns.set(player, "purchase", COOLDOWN);

        setItem(acceptPurchase.getSlot(), acceptPurchase.getItemStack(), event -> {
            if (Cooldowns.has(player, "purchase")) {
                return;
            }
            close();
            Cooldowns.set(player, "purchase", COOLDOWN);

            EconomyProvider economy = plugin.getEconomyManager().getEconomyProvider();

            if (!economy.hasCoins(user, cost)) {
                economy.sendInsufficientCoinsMessage(user, cost);
                playDenySound();
                return;
            }
            economy.removeCoinsAsync(user, cost).thenAcceptAsync(result -> {
                if (result.booleanValue()) {
                    plugin.getStorage().addTreasureChestsAsync(user, treasureChest, amount).thenAcceptAsync(result2 -> {
                        if (result2.leftBoolean()) {
                            playSuccessSound();
                            plugin.getJavaPlugin().getServer().getPluginManager().callEvent(new PlayerPurchaseTreasureChestEvent(plugin, user, player, treasureChest, amount));
                            plugin.getJavaPlugin().getLogger().log(Level.INFO, "[TREASURE CHEST] " + user + " bought " + amount + " " + treasureChest.getKey() + " for " + cost + ".");
                            new TreasureChestMenu(plugin, user).open();
                        } else {
                            economy.addCoinsAsync(user, cost);
                            user.sendMessage(user.translate("generic.error.database"));
                            playDenySound();
                        }
                    }, plugin.getSyncExecutor());
                } else {
                    economy.sendInsufficientCoinsMessage(user, cost);
                    playDenySound();
                }
            }, plugin.getSyncExecutor());
        });

        ItemBuilder denyPurchase = new ItemBuilderImpl(config, "menu.purchase.treasure_chest.items.deny");
        denyPurchase.setDisplayName(user.translate(
                "menu.purchase.treasure_chest.deny.name",
                tagResolver
        ));
        denyPurchase.setLore(user.translateList(
                "menu.purchase.treasure_chest.deny.desc",
                tagResolver
        ));

        setItem(denyPurchase.getSlot(), denyPurchase.getItemStack(), event -> {
            playClickSound();
            close();
        });
    }

    @Override
    public ItemStack getFillEmptySlotsItem() {
        Config config = plugin.getConfigManager().getMainConfig();

        if (!config.getBoolean("menu.purchase.treasure_chest.items.fill_empty_slots.enabled")) {
            return null;
        }
        return new ItemBuilderImpl(config, "menu.purchase.treasure_chest.items.fill_empty_slots").getItemStack();
    }
}
