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
package com.ricodev.lumen.storage.database;

import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.storage.SQLDatabase;
import com.ricodev.lumen.storage.connection.file.SqliteConnectionProvider;

public class SqliteDatabase extends SQLDatabase {

    public SqliteDatabase(LumenPlugin plugin) {
        super(plugin, new SqliteConnectionProvider());
    }

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    protected String getCreateUsersTable() {
        return "CREATE TABLE IF NOT EXISTS %s (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL, " +
                "coins INTEGER NOT NULL DEFAULT 0, " +
                "self_view_morph INTEGER NOT NULL DEFAULT 0, " +
                "self_view_status INTEGER NOT NULL DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_seen DATETIME DEFAULT CURRENT_TIMESTAMP " +
                ");";
    }

    @Override
    protected String getCreateCosmeticsTable() {
        return "CREATE TABLE IF NOT EXISTS %s (" +
                "player_id INTEGER NOT NULL, " +
                "category TEXT NOT NULL, " +
                "cosmetic TEXT NOT NULL, " +
                "PRIMARY KEY (player_id, category), " +
                "FOREIGN KEY (player_id) REFERENCES %s(id) ON DELETE CASCADE " +
                ");";
    }

    @Override
    protected String getCreateGadgetAmmoTable() {
        return "CREATE TABLE IF NOT EXISTS %s (" +
                "player_id INTEGER NOT NULL, " +
                "gadget TEXT NOT NULL, " +
                "amount INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (player_id, gadget), " +
                "FOREIGN KEY (player_id) REFERENCES %s(id) ON DELETE CASCADE, " +
                "CHECK (amount >= 0)" +
                ");";
    }

    @Override
    protected String getEquipCosmeticQuery() {
        return "INSERT OR REPLACE INTO %s (player_id, category, cosmetic) VALUES (?, ?, ?);";
    }

    @Override
    protected String getAddGadgetAmmo() {
        return "INSERT OR REPLACE INTO %1$s (player_id, gadget, amount) VALUES (?1, ?2, " +
                "COALESCE((SELECT amount FROM %1$s WHERE player_id = ?1 AND gadget = ?2), 0) + ?3);";
    }

    @Override
    protected String getSetGadgetAmmo() {
        return "INSERT OR REPLACE INTO %s (player_id, gadget, amount) VALUES (?, ?, ?);";
    }

}
