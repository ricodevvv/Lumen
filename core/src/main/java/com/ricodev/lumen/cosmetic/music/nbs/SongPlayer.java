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

import com.cryptomorin.xseries.XSound;
import com.ricodev.lumen.util.AbstractRunnable;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Plays an {@link NbsSong} at a spot in the world, using note block sounds. Instruments that
 * the server doesn't have yet (anything after 1.12's guitar and friends on 1.8) fall back to harp.
 */
public class SongPlayer extends AbstractRunnable {

    private static final XSound[] INSTRUMENTS = {
            XSound.BLOCK_NOTE_BLOCK_HARP, XSound.BLOCK_NOTE_BLOCK_BASS, XSound.BLOCK_NOTE_BLOCK_BASEDRUM,
            XSound.BLOCK_NOTE_BLOCK_SNARE, XSound.BLOCK_NOTE_BLOCK_HAT, XSound.BLOCK_NOTE_BLOCK_GUITAR,
            XSound.BLOCK_NOTE_BLOCK_FLUTE, XSound.BLOCK_NOTE_BLOCK_BELL, XSound.BLOCK_NOTE_BLOCK_CHIME,
            XSound.BLOCK_NOTE_BLOCK_XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_COW_BELL,
            XSound.BLOCK_NOTE_BLOCK_DIDGERIDOO, XSound.BLOCK_NOTE_BLOCK_BIT, XSound.BLOCK_NOTE_BLOCK_BANJO,
            XSound.BLOCK_NOTE_BLOCK_PLING
    };
    private static final double HEARING_DISTANCE_SQUARED = 48.0d * 48.0d;

    private final NbsSong song;
    private final Location location;
    private final Runnable onEnd;
    private double time;
    private int nextTick;

    public SongPlayer(NbsSong song, Location location, Runnable onEnd) {
        this.song = song;
        this.location = location.clone();
        this.onEnd = onEnd;
    }

    public void start(Plugin plugin) {
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        time += 0.05d;
        int currentTick = (int) (time * song.getSpeed());

        while (nextTick <= currentTick) {
            for (NbsSong.Note note : song.getNotes(nextTick)) {
                play(note);
            }
            nextTick++;

            if (nextTick > song.getLength()) {
                cancel();
                onEnd.run();
                return;
            }
        }
    }

    private void play(NbsSong.Note note) {
        Sound sound = sound(note.getInstrument());

        if (sound == null || location.getWorld() == null) {
            return;
        }
        float pitch = pitch(note.getKey(), note.getPitch());
        float volume = Math.max(0.05f, note.getVolume());

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= HEARING_DISTANCE_SQUARED) {
                player.playSound(location, sound, volume * 3.0f, pitch);
            }
        }
    }

    private static Sound sound(int instrument) {
        XSound xSound = instrument >= 0 && instrument < INSTRUMENTS.length ? INSTRUMENTS[instrument] : INSTRUMENTS[0];
        Sound sound = xSound.get();
        return sound != null ? sound : INSTRUMENTS[0].get();
    }

    /**
     * Note block pitch from an .nbs key, folded into the 0.5 - 2.0 range the client can play.
     */
    private static float pitch(int key, int cents) {
        double semitones = key - 45 + cents / 100.0d;

        while (semitones < -12) {
            semitones += 12;
        }
        while (semitones > 12) {
            semitones -= 12;
        }
        return (float) Math.pow(2.0d, semitones / 12.0d);
    }
}
