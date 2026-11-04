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
package com.ricodev.lumen.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.treasure.TreasureChest;
import com.ricodev.lumen.api.user.User;

/**
 * Called when a player opens a treasure chest.
 */
public class PlayerOpenTreasureChestEvent extends LumenEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final User user;
    private final Player player;
    private final TreasureChest treasureChest;

    /**
     * Constructs a new PlayerOpenTreasureChestEvent.
     *
     * @param plugin        the Lumen plugin instance
     * @param user          the user who is opening the treasure chest
     * @param player        the player who is opening the treasure chest
     * @param treasureChest the treasure chest being opened
     */
    public PlayerOpenTreasureChestEvent(Lumen plugin, User user, Player player, TreasureChest treasureChest) {
        super(plugin);
        this.user = user;
        this.player = player;
        this.treasureChest = treasureChest;
    }

    /**
     * Gets the user for this event.
     *
     * @return the {@link User} instance
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the player for this event.
     *
     * @return the {@link Player} instance
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the treasure chest for this event.
     *
     * @return the {@link TreasureChest} instance
     */
    public TreasureChest getTreasureChest() {
        return treasureChest;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the {@link HandlerList} instance
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the {@link HandlerList} instance
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
