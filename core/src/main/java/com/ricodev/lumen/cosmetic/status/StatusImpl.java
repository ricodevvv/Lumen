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
package com.ricodev.lumen.cosmetic.status;

import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.util.MetadataUtil;
import com.ricodev.lumen.util.entity.Entities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.status.Status;
import com.ricodev.lumen.api.cosmetic.status.StatusBehavior;
import com.ricodev.lumen.api.cosmetic.status.StatusType;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.cosmetic.CosmeticImpl;

public class StatusImpl extends CosmeticImpl<StatusType, StatusBehavior> implements Status {

    private ArmorStand stand;
    private int ticks;

    private final double heightOffset;

    public StatusImpl(LumenPlugin plugin, User user, StatusType type, StatusBehavior behavior) {
        super(plugin, user, type, behavior);
        heightOffset = type.getCategory().getConfig().getDouble("height_offset");
    }

    @Override
    protected void onEquip() {
        stand = Entities.spawn(getUpdatedLocation(), ArmorStand.class, spawned -> {
            spawned.setVisible(false);
            spawned.setSmall(true);
            spawned.setMarker(true);
            spawned.setGravity(false);
            spawned.setBasePlate(false);
            spawned.setCustomNameVisible(true);
            MetadataUtil.setCustomEntity(spawned);
        });
        refreshText();

        if (!user.hasSelfViewStatus()) {
            Compats.get().hideEntity(plugin, player, stand);
        }
        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    protected void onUpdate() {
        if (stand == null || !stand.isValid()) {
            return;
        }
        Location location = getUpdatedLocation();

        if (!location.getWorld().equals(stand.getWorld())) {
            onUnequip();
            onEquip();
            return;
        }
        stand.teleport(location);

        if (cosmeticType.getRefreshInterval() > 0) {
            if (ticks > cosmeticType.getRefreshInterval()) {
                refreshText();
                ticks = 0;
            }
            ticks++;
        }
    }

    @Override
    protected void onUnequip() {
        if (stand != null) {
            stand.remove();
            stand = null;
        }
    }

    private Location getUpdatedLocation() {
        double sneakOffset = player.isSneaking() ? -0.3d : 0.0d;
        return player.getLocation().add(0.0d, player.getEyeHeight() + 0.2d + heightOffset + sneakOffset, 0.0d);
    }

    private void refreshText() {
        String text = plugin.getPlatformAdapter().serialize(cosmeticType.getTextProvider().apply(cosmeticType, user));
        text = plugin.getPlaceholderManager().resolve(player, text);

        if (!text.equals(stand.getCustomName())) {
            stand.setCustomName(text);
        }
    }

    @Override
    public Entity getEntity() {
        return stand;
    }
}
