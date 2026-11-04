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
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.economy.EconomyProvider;
import com.ricodev.lumen.api.event.PlayerPurchaseGadgetAmmoEvent;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.item.ItemBuilder;
import com.ricodev.lumen.cosmetic.gadget.GadgetImpl;
import com.ricodev.lumen.menu.MenuImpl;
import com.ricodev.lumen.util.item.ItemBuilderImpl;

import java.util.logging.Level;

public class AmmoPurchaseMenu extends MenuImpl {

    private static final int COOLDOWN = 20;

    private final GadgetType gadgetType;
    private final Config config;

    public AmmoPurchaseMenu(Lumen plugin, User user, GadgetType gadgetType) {
        super(plugin, user,
                user.translate("menu.purchase.gadget_ammo.title"),
                plugin.getConfigManager().getMainConfig().getInt("menu.purchase.gadget_ammo.rows")
        );
        this.gadgetType = gadgetType;
        this.config = plugin.getConfigManager().getMainConfig();
    }

    @Override
    protected void addItems() {
        CosmeticCategory<?, ?, ?> category = gadgetType.getCategory();
        ItemBuilder cosmeticItem = new ItemBuilderImpl(gadgetType.getItemStack());
        String name = gadgetType.getName(user);
        TagResolver tagResolver = TagResolver.resolver(
                gadgetType.getResolvers(user),
                Placeholder.unparsed("name", name)
        );
        cosmeticItem.setDisplayName(user.translate(
                "menu." + category.getKey() + "." + gadgetType.getKey() + ".name",
                tagResolver
        ));
        cosmeticItem.setLore(user.translateList(
                "menu." + category.getKey() + "." + gadgetType.getKey() + ".desc",
                tagResolver
        ));

        setItem(13, cosmeticItem.getItemStack(), event -> {
        });

        ItemBuilder acceptPurchase = new ItemBuilderImpl(config, "menu.purchase.gadget_ammo.items.accept");
        acceptPurchase.setDisplayName(user.translate(
                "menu.purchase.gadget_ammo.accept.name",
                tagResolver
        ));
        acceptPurchase.setLore(user.translateList(
                "menu.purchase.gadget_ammo.accept.desc",
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
            int cost = gadgetType.getAmmoCost();

            if (!economy.hasCoins(user, cost)) {
                economy.sendInsufficientCoinsMessage(user, cost);
                playDenySound();
                return;
            }
            economy.removeCoinsAsync(user, cost).thenAcceptAsync(result -> {
                if (result.booleanValue()) {
                    plugin.getStorage().addGadgetAmmoAsync(user, gadgetType, gadgetType.getAmmoPurchaseAmount()).thenAcceptAsync(result2 -> {
                        if (result2.leftBoolean()) {
                            plugin.getJavaPlugin().getServer().getPluginManager().callEvent(new PlayerPurchaseGadgetAmmoEvent(plugin, user, player, gadgetType));

                            GadgetImpl gadget = (GadgetImpl) user.getCosmetic(plugin.getCategoryRegistries().gadgets());

                            if (gadget != null && gadget.isEquipped()) {
                                gadget.setGadgetItemInInventory();
                            }
                            playSuccessSound();
                            plugin.getJavaPlugin().getLogger().log(Level.INFO, "[AMMO] " + user + " bought " + gadgetType.getAmmoPurchaseAmount() + " " + gadgetType.getKey() + " ammo for " + cost + ".");
                        } else {
                            economy.addCoinsAsync(user, cost);
                            user.sendMessage(user.translate("generic.error.database"));
                        }
                    }, plugin.getSyncExecutor());
                } else {
                    economy.sendInsufficientCoinsMessage(user, cost);
                    playDenySound();
                }
            }, plugin.getSyncExecutor());
        });

        ItemBuilderImpl denyPurchase = new ItemBuilderImpl(config, "menu.purchase.gadget_ammo.items.deny");
        denyPurchase.setDisplayName(user.translate(
                "menu.purchase.gadget_ammo.deny.name",
                tagResolver
        ));
        denyPurchase.setLore(user.translateList(
                "menu.purchase.gadget_ammo.deny.desc",
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

        if (!config.getBoolean("menu.purchase.gadget_ammo.items.fill_empty_slots.enabled")) {
            return null;
        }
        return new ItemBuilderImpl(config, "menu.purchase.gadget_ammo.items.fill_empty_slots").getItemStack();
    }
}
