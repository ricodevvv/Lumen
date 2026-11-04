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
package com.ricodev.lumen.menu;

import com.ricodev.lumen.compat.Sounds;
import com.cryptomorin.xseries.XSound;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.menu.ClickableItem;
import com.ricodev.lumen.api.menu.Menu;
import com.ricodev.lumen.api.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class MenuImpl implements Menu {

    protected final Lumen plugin;
    protected final User user;
    protected final Player player;
    protected final Component title;
    protected final int rows;
    protected final Inventory inventory;
    private final Map<Integer, ClickableItem> clickableSlots = new HashMap<>();
    private boolean valid;
    private Menu previousMenu;

    protected MenuImpl(Lumen plugin, User user, Component title, int rows) {
        this.plugin = plugin;
        this.user = user;
        this.player = user.getPlayer();
        this.title = title;
        this.rows = Math.min(6, Math.max(1, rows));
        this.inventory = plugin.getPlatformAdapter().createInventory(player, getSize(), title);
    }

    public void open() {
        if (valid) {
            return;
        }
        addItems();
        fillEmptySlots(inventory, getFillEmptySlotsItem());

        player.openInventory(inventory);
        plugin.getMenuManager().register(this);
        valid = true;
    }

    public void close() {
        if (valid) {
            player.closeInventory();
        }
    }

    protected abstract void addItems();

    private int getActualSlot(int slot) {
        int size = getSize();

        if (slot < 0) {
            slot = size + slot;
        }
        return slot;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack, ClickableItem clickable) {
        if (itemStack == null || clickable == null) {
            return;
        }
        int actualSlot = getActualSlot(slot);

        if (actualSlot > inventory.getSize()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String itemName = itemMeta != null && itemMeta.hasDisplayName()
                    ? ChatColor.stripColor(itemMeta.getDisplayName())
                    : itemStack.getType().name();

            plugin.getJavaPlugin().getLogger().log(Level.WARNING, "Cannot add item " + itemName + " to menu " + title + ". Slot " + actualSlot
                    + " exceeds inventory size of " + inventory.getSize() + ".");
            return;
        }
        inventory.setItem(actualSlot, itemStack);
        clickableSlots.put(actualSlot, clickable);
    }

    @Override
    @Nullable
    public ClickableItem getClickableSlot(int slot) {
        return clickableSlots.getOrDefault(getActualSlot(slot), null);
    }

    @Override
    public void removeItem(int slot) {
        int actualSlot = getActualSlot(slot);

        inventory.setItem(actualSlot, null);
        clickableSlots.remove(actualSlot);
    }

    @Override
    public void clear() {
        inventory.clear();
        clickableSlots.clear();
    }

    @Override
    public void fillEmptySlots(Inventory inventory, @Nullable ItemStack itemStack) {
        if (inventory == null || itemStack == null) {
            return;
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack tempItemStack = inventory.getItem(i);

            if (tempItemStack == null || tempItemStack.getType() == Material.AIR) {
                inventory.setItem(i, itemStack);
            }
        }
    }

    @Override
    public void playClickSound() {
        Sounds.playFor(player, XSound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    @Override
    public void playSuccessSound() {
        Sounds.playFor(player, XSound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
    }

    @Override
    public void playDenySound() {
        Sounds.playFor(player, XSound.ENTITY_ITEM_BREAK, 0.5f, 0.5f);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getSize() {
        return getRows() * 9;
    }

    @Override
    public void invalidate() {
        valid = false;
        clear();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setPreviousMenu(Menu previousMenu) {
        this.previousMenu = previousMenu;
    }

    @Override
    @Nullable
    public Menu getPreviousMenu() {
        return previousMenu;
    }

    @Override
    public void trySetPreviousMenuItem(int slot, ItemStack itemStack) {
        Menu previousMenu = getPreviousMenu();

        if (previousMenu != null) {
            setItem(slot, itemStack, event -> {
                playClickSound();
                previousMenu.open();
            });
        }
    }
}
