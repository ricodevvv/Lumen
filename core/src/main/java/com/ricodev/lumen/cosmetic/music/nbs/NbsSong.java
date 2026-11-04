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
package com.ricodev.lumen.cosmetic.music.nbs;

import com.ricodev.lumen.api.cosmetic.music.Song;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A decoded .nbs song: the notes grouped by the tick they play on.
 */
public final class NbsSong implements Song {

    private final String title;
    private final String author;
    private final int length;
    private final float speed;
    private final Map<Integer, List<Note>> notes;

    NbsSong(String title, String author, int length, float speed, Map<Integer, List<Note>> notes) {
        this.title = title;
        this.author = author;
        this.length = length;
        this.speed = speed;
        this.notes = notes;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    public List<Note> getNotes(int tick) {
        List<Note> list = notes.get(tick);
        return list == null ? Collections.<Note>emptyList() : list;
    }

    /**
     * One note block hit. Key 45 is the F#4 a plain note block plays.
     */
    public static final class Note {

        private final int instrument;
        private final int key;
        private final float volume;
        private final int pitch;

        Note(int instrument, int key, float volume, int pitch) {
            this.instrument = instrument;
            this.key = key;
            this.volume = volume;
            this.pitch = pitch;
        }

        public int getInstrument() {
            return instrument;
        }

        public int getKey() {
            return key;
        }

        public float getVolume() {
            return volume;
        }

        /**
         * Fine pitch in cents on top of the key, newer .nbs versions only.
         */
        public int getPitch() {
            return pitch;
        }
    }
}
