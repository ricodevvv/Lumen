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
package com.ricodev.lumen.treasure;

import com.ricodev.lumen.util.Collects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.treasure.TreasureChestPlatform;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.api.util.structure.type.BlockStructure;
import com.ricodev.lumen.hologram.Hologram;
import com.ricodev.lumen.hologram.Spacing;
import com.ricodev.lumen.util.structure.NamedStructureData;
import com.ricodev.lumen.util.structure.type.BlockStructureImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreasureChestPlatformImpl implements TreasureChestPlatform {

    private static final List<Material> CHEST_TYPES = Collects.listOf(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST
    );

    private final LumenPlugin plugin;
    private final int id;
    private final Location center;
    private final NamedStructureData namedStructureData;
    private final BlockStructure blockStructure;

    private final List<Location> chestLocations = new ArrayList<>();
    private Hologram hologram;
    private User user;

    public TreasureChestPlatformImpl(LumenPlugin plugin, int id, Location center, NamedStructureData structureData) {
        this.plugin = plugin;
        this.id = id;
        this.center = center;
        this.namedStructureData = structureData;
        this.blockStructure = new BlockStructureImpl(structureData.structureData());

        Location chestLocation = center.clone();
        chestLocation.setYaw(0.0f);
        chestLocation.setPitch(0.0f);

        addChestLocations(
                chestLocation.clone().add(-1.0d, 0.0d, -3.0d),
                chestLocation.clone().add(1.0d, 0.0d, -3.0d),
                chestLocation.clone().add(3.0d, 0.0d, -1.0d),
                chestLocation.clone().add(3.0d, 0.0d, 1.0d),
                chestLocation.clone().add(1.0d, 0.0d, 3.0d),
                chestLocation.clone().add(-1.0d, 0.0d, 3.0d),
                chestLocation.clone().add(-3.0d, 0.0d, 1.0d),
                chestLocation.clone().add(-3.0d, 0.0d, -1.0d)
        );
        chestLocations.forEach(chestLoc -> chestLoc.getBlock().setType(Material.AIR));
        build();
    }

    @Override
    public void build() {
        blockStructure.spawn(center, true);

        if (hologram == null) {
            hologram = new Hologram(center.clone().add(0.0d, -0.2d, 0.0d));
            hologram.addLines(
                    plugin.getLanguageManager().renderList("treasure_chest.platform.hologram", plugin.getLanguageManager().getLocale()),
                    Spacing.SMALL
            );
        }
        hologram.spawn();
    }

    @Override
    public void destroy() {
        blockStructure.remove();
        hologram.despawn();
    }

    @Override
    public void hideDisplay() {
        destroyChest();
        hologram.despawn();
    }

    private void destroyChest() {
        for (Block block : blockStructure.getPlacedEntries()) {
            if (CHEST_TYPES.contains(block.getType())) {
                block.setType(Material.AIR);
            }
        }
    }

    private void addChestLocations(Location... location) {
        Collections.addAll(chestLocations, location);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Location getCenter() {
        return center;
    }

    public NamedStructureData getNamedStructureData() {
        return namedStructureData;
    }

    @Override
    public BlockStructure getBlockStructure() {
        return blockStructure;
    }

    @Override
    public List<Location> getChestLocations() {
        return chestLocations;
    }

    public Hologram getHologram() {
        return hologram;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean isInUse() {
        return user != null;
    }
}
