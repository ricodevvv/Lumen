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
package com.ricodev.lumen.cosmetic.music;

import com.ricodev.lumen.api.cosmetic.music.Song;
import com.ricodev.lumen.cosmetic.music.nbs.NbsReader;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticRarity;
import com.ricodev.lumen.api.cosmetic.music.Music;
import com.ricodev.lumen.api.cosmetic.music.MusicBehavior;
import com.ricodev.lumen.api.cosmetic.music.MusicType;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticCategory;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticTypeImpl;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public class MusicTypeImpl extends CosmeticTypeImpl<MusicType, MusicBehavior> implements MusicType {

    private final Song song;

    public MusicTypeImpl(String key,
                         CosmeticCategory<MusicType, MusicBehavior, ?> category,
                         Supplier<MusicBehavior> behaviorFactory,
                         boolean enabled,
                         boolean purchasable,
                         int cost,
                         CosmeticRarity rarity,
                         ItemStack itemStack,
                         List<String> treasureChests,
                         Song song) {
        super(key, category, behaviorFactory, enabled, purchasable, cost, rarity, itemStack, treasureChests);
        this.song = song;
    }

    @Override
    protected Music createInstance(LumenPlugin plugin, User user, MusicBehavior behavior) {
        return new MusicImpl(plugin, user, this, behavior);
    }

    @Override
    public @Nullable Song getSong() {
        return song;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<MusicType, MusicBehavior, MusicType.Builder> implements MusicType.Builder {

        private Song song;

        public BuilderImpl(String key, CosmeticCategory<MusicType, MusicBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected MusicType.Builder self() {
            return this;
        }

        @Override
        public MusicType.Builder readFromConfig() {
            super.readFromConfig();
            loadSong();
            return this;
        }

        private void loadSong() {
            File file = PLUGIN.getDataFolder().toPath().resolve("songs").resolve(key + ".nbs").toFile();

            if (!file.exists()) {
                PLUGIN.getLogger().log(Level.WARNING, "Song file not found: " + file.getName()
                        + ". Ensure the file exists in the songs folder (case-sensitive).");
                return;
            }
            try {
                song = NbsReader.read(file);
            } catch (java.io.IOException e) {
                PLUGIN.getLogger().log(Level.WARNING, "Couldn't read the song " + file.getName() + ".", e);
            }
        }

        @Override
        public MusicType.Builder song(Song song) {
            this.song = song;
            return this;
        }

        @Override
        public MusicType build() {
            return new MusicTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    purchasable,
                    cost,
                    rarity,
                    itemStack,
                    treasureChests,
                    song
            );
        }
    }
}
