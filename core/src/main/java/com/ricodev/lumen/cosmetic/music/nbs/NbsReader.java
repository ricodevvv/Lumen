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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads Note Block Studio files, both the classic format and the newer versioned ones (1 to 5).
 * Everything in the format is little endian.
 */
public final class NbsReader {

    private NbsReader() {
    }

    public static NbsSong read(File file) throws IOException {
        try (InputStream input = Files.newInputStream(file.toPath())) {
            return read(input);
        }
    }

    public static NbsSong read(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(stream);
        int length = readShort(in);
        int version = 0;

        if (length == 0) {
            version = in.readUnsignedByte();
            in.readUnsignedByte();

            if (version >= 3) {
                length = readShort(in);
            }
        }
        int layerCount = readShort(in);
        String title = readString(in);
        String author = readString(in);
        readString(in);
        readString(in);
        float speed = readShort(in) / 100.0f;
        in.readByte();
        in.readByte();
        in.readByte();
        readInt(in);
        readInt(in);
        readInt(in);
        readInt(in);
        readInt(in);
        readString(in);

        if (version >= 4) {
            in.readByte();
            in.readByte();
            readShort(in);
        }
        Map<Integer, List<NoteData>> raw = new HashMap<>();
        int tick = -1;
        int lastTick = 0;

        while (true) {
            int jumpTicks = readShort(in);

            if (jumpTicks == 0) {
                break;
            }
            tick += jumpTicks;
            int layer = -1;

            while (true) {
                int jumpLayers = readShort(in);

                if (jumpLayers == 0) {
                    break;
                }
                layer += jumpLayers;
                int instrument = in.readUnsignedByte();
                int key = in.readUnsignedByte();
                int velocity = 100;
                int pitch = 0;

                if (version >= 4) {
                    velocity = in.readUnsignedByte();
                    in.readUnsignedByte();
                    pitch = (short) readShort(in);
                }
                raw.computeIfAbsent(tick, ignored -> new ArrayList<>()).add(new NoteData(layer, instrument, key, velocity, pitch));
                lastTick = Math.max(lastTick, tick);
            }
        }
        int[] layerVolumes = readLayerVolumes(in, layerCount, version);
        Map<Integer, List<NbsSong.Note>> notes = new HashMap<>();

        for (Map.Entry<Integer, List<NoteData>> entry : raw.entrySet()) {
            List<NbsSong.Note> list = new ArrayList<>();

            for (NoteData data : entry.getValue()) {
                int layerVolume = data.layer < layerVolumes.length ? layerVolumes[data.layer] : 100;
                list.add(new NbsSong.Note(data.instrument, data.key, data.velocity * layerVolume / 10000.0f, data.pitch));
            }
            notes.put(entry.getKey(), list);
        }
        return new NbsSong(title, author, Math.max(length, lastTick), speed <= 0 ? 10.0f : speed, notes);
    }

    private static int[] readLayerVolumes(DataInputStream in, int layerCount, int version) throws IOException {
        int[] volumes = new int[layerCount];
        java.util.Arrays.fill(volumes, 100);

        try {
            for (int i = 0; i < layerCount; i++) {
                readString(in);

                if (version >= 4) {
                    in.readByte();
                }
                volumes[i] = in.readUnsignedByte();

                if (version >= 2) {
                    in.readUnsignedByte();
                }
            }
        } catch (EOFException ignored) {
        }
        return volumes;
    }

    private static int readShort(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        return b1 | (b2 << 8);
    }

    private static int readInt(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = readInt(in);

        if (length < 0 || length > 1 << 20) {
            throw new IOException("Broken string length " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final class NoteData {

        private final int layer;
        private final int instrument;
        private final int key;
        private final int velocity;
        private final int pitch;

        private NoteData(int layer, int instrument, int key, int velocity, int pitch) {
            this.layer = layer;
            this.instrument = instrument;
            this.key = key;
            this.velocity = velocity;
            this.pitch = pitch;
        }
    }
}
