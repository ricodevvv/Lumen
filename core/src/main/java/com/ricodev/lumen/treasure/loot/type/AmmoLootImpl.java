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
package com.ricodev.lumen.treasure.loot.type;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.treasure.loot.AmmoLoot;
import com.ricodev.lumen.api.treasure.loot.GeneratedLoot;
import com.ricodev.lumen.api.treasure.loot.LootCategory;
import com.ricodev.lumen.api.treasure.loot.number.IntProvider;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.treasure.loot.GeneratedLootImpl;
import com.ricodev.lumen.treasure.loot.LootEntryImpl;

public class AmmoLootImpl extends LootEntryImpl<IntProvider> implements AmmoLoot {

    private final GadgetType gadgetType;

    public AmmoLootImpl(IntProvider intProvider, LootCategory category, GadgetType gadgetType) {
        super(intProvider, category);
        this.gadgetType = gadgetType;
    }

    @Override
    public String getKey() {
        return gadgetType.getKey();
    }

    @Override
    public String getNameTranslationKey() {
        return gadgetType.getNameTranslationKey();
    }

    @Override
    public CosmeticRarity getRarity() {
        return gadgetType.getAmmoRarity();
    }

    @Override
    public ItemStack getItemStack() {
        return gadgetType.getItemStack();
    }

    @Override
    public GeneratedLoot generate(Player player) {
        return new GeneratedAmmoLoot(this, intProvider.get());
    }

    @Override
    public GadgetType getGadgetType() {
        return gadgetType;
    }

    private static class GeneratedAmmoLoot extends GeneratedLootImpl<AmmoLoot> {

        public GeneratedAmmoLoot(AmmoLoot entry, int amount) {
            super(entry, amount);
        }

        @Override
        public String getNameTranslationKey() {
            return "treasure_chest.loot.ammo";
        }

        @Override
        public void give(Player player, User user) {
            PLUGIN.getStorage().addGadgetAmmoAsync(user, entry.getGadgetType(), amount).thenAccept(result -> {
                if (result.leftBoolean()) {
                    PLUGIN.getTreasureChestManager().getLootBroadcaster().broadcastMessage(
                            player,
                            getRarity(),
                            getNameTranslationKey() + ".broadcast",
                            receiverUser -> TagResolver.resolver(
                                    Placeholder.unparsed("player", player.getName()),
                                    getResolvers(receiverUser)
                            ));
                } else {
                    user.sendMessage(user.translate("generic.error.database"));
                }
            });
        }
    }
}
