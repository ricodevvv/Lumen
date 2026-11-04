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
package com.ricodev.lumen.util.entity;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which movement keys each player is holding while riding, read from the steer
 * vehicle packet (up to 1.21.1) or the player input packet (1.21.2+).
 */
public final class RideInput extends PacketListenerAbstract {

    private static final Map<UUID, Input> INPUTS = new ConcurrentHashMap<>();

    public static Input get(UUID uuid) {
        Input input = INPUTS.get(uuid);
        return input != null ? input : Input.NONE;
    }

    public static void clear(UUID uuid) {
        INPUTS.remove(uuid);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getUser() == null || event.getUser().getUUID() == null) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            WrapperPlayClientSteerVehicle packet = new WrapperPlayClientSteerVehicle(event);
            INPUTS.put(event.getUser().getUUID(), new Input(packet.getForward(), packet.getSideways(), packet.isJump()));
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT) {
            WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);
            float forward = (packet.isForward() ? 1.0f : 0.0f) - (packet.isBackward() ? 1.0f : 0.0f);
            float strafe = (packet.isLeft() ? 1.0f : 0.0f) - (packet.isRight() ? 1.0f : 0.0f);
            INPUTS.put(event.getUser().getUUID(), new Input(forward, strafe, packet.isJump()));
        }
    }

    public static final class Input {

        static final Input NONE = new Input(0.0f, 0.0f, false);

        private final float forward;
        private final float strafe;
        private final boolean jump;

        Input(float forward, float strafe, boolean jump) {
            this.forward = forward;
            this.strafe = strafe;
            this.jump = jump;
        }

        public float getForward() {
            return forward;
        }

        public float getStrafe() {
            return strafe;
        }

        public boolean isJump() {
            return jump;
        }
    }
}
