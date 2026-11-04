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
package com.ricodev.lumen.util.block;

import com.cryptomorin.xseries.XMaterial;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.util.MetadataUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Shows client-side blocks to nearby players and puts the real ones back when they expire.
 */
public class FakeBlockManager {

    private static final double VIEW_RANGE_SQUARED = 64.0d * 64.0d;
    public static final long PERMANENT = -1L;

    /**
     * Name fragments of blocks that are never replaced: anything interactive, see-through,
     * oddly shaped or with a block entity behind it.
     */
    private static final String[] BLOCKED_NAMES = {
            "SLAB", "STEP", "STAIRS", "DOOR", "BUTTON", "PLATE", "BED", "SIGN", "BANNER", "WALL", "FENCE",
            "SHULKER", "ICE", "WOOL", "CARPET", "LANTERN", "CANDLE", "CAULDRON", "ANVIL", "GLASS", "PISTON",
            "CHEST", "FURNACE", "HOPPER", "DISPENSER", "DROPPER", "BEACON", "BREWING", "JUKEBOX", "ENCHANT",
            "DAYLIGHT", "SPAWNER", "COMMAND", "LEVER", "REDSTONE", "TNT", "NOTE", "BARRIER", "CACTUS", "SLIME",
            "HONEY", "GLOWSTONE", "CRAFTING", "WORKBENCH", "OBSERVER", "PATH", "FARMLAND", "SOIL", "MAGMA",
            "TARGET", "LAMP", "SKULL", "HEAD", "LECTERN", "BARREL", "SMOKER", "BELL", "CAMPFIRE", "BEEHIVE",
            "BEE_NEST", "CONDUIT", "STRUCTURE", "JIGSAW", "CRAFTER", "VAULT", "TRIAL", "POT", "BOOKSHELF", "SCULK"
    };

    protected final LumenPlugin plugin;

    private final Map<Block, TrackedBlock> trackedBlocks = new LinkedHashMap<>();
    private BukkitTask task;
    private long currentTick;

    public FakeBlockManager(LumenPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a fake block to nearby players and tracks it.
     *
     * @param durationTicks how long the fake block lasts in ticks, or {@link #PERMANENT}
     * @return true if the fake block was set
     */
    public boolean setFakeBlock(Block block, XMaterial material, boolean overwrite, long durationTicks) {
        if (!overwrite && isFakeBlock(block)) {
            return false;
        }
        if (!canSetFakeBlock(block)) {
            return false;
        }
        MetadataUtil.setCustomBlock(block);
        trackedBlocks.put(block, new TrackedBlock(material, durationTicks < 0
                ? PERMANENT
                : currentTick + durationTicks));
        broadcastBlockChange(block, material);
        startTaskIfNeeded();
        return true;
    }

    public boolean setFakeBlock(Block block, ItemStack itemStack, boolean overwrite, long durationTicks) {
        if (itemStack == null || !itemStack.getType().isBlock()) {
            return false;
        }
        return setFakeBlock(block, XMaterial.matchXMaterial(itemStack), overwrite, durationTicks);
    }

    public boolean setFakeBlock(Block block, ItemStack itemStack, boolean overwrite) {
        return setFakeBlock(block, itemStack, overwrite, PERMANENT);
    }

    /**
     * Restores the real block for all players and stops tracking it.
     */
    public void resetFakeBlock(Block block) {
        if (trackedBlocks.remove(block) != null) {
            restore(block);
        }
        stopTaskIfIdle();
    }

    public void shutdown() {
        for (Block block : trackedBlocks.keySet()) {
            restore(block);
        }
        trackedBlocks.clear();
        stopTaskIfIdle();
    }

    /**
     * Re-sends all tracked fake blocks in range to the given player.
     */
    public void refresh(Player player) {
        Location playerLocation = player.getLocation();

        for (Map.Entry<Block, TrackedBlock> entry : trackedBlocks.entrySet()) {
            Block block = entry.getKey();

            if (block.getWorld().equals(player.getWorld())
                    && block.getLocation().distanceSquared(playerLocation) < VIEW_RANGE_SQUARED) {
                Compats.get().sendBlockChange(player, block.getLocation(), entry.getValue().material);
            }
        }
    }

    public boolean isFakeBlock(Block block) {
        return trackedBlocks.containsKey(block) || MetadataUtil.isCustomBlock(block);
    }

    public int getTrackedBlockCount() {
        return trackedBlocks.size();
    }

    /**
     * Checks whether a block is suitable for a client-side replacement.
     */
    public boolean canSetFakeBlock(Block block) {
        Material material = block.getType();

        if (!material.isBlock() || !material.isSolid() || !material.isOccluding()) {
            return false;
        }
        String name = material.name().toUpperCase(Locale.ROOT);

        for (String blocked : BLOCKED_NAMES) {
            if (name.contains(blocked)) {
                return false;
            }
        }
        return !(block.getState() instanceof InventoryHolder);
    }

    private void tick() {
        currentTick++;
        Iterator<Map.Entry<Block, TrackedBlock>> iterator = trackedBlocks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Block, TrackedBlock> entry = iterator.next();

            if (entry.getValue().isExpired(currentTick)) {
                iterator.remove();
                restore(entry.getKey());
            }
        }
        stopTaskIfIdle();
    }

    private void startTaskIfNeeded() {
        if (task == null && !trackedBlocks.isEmpty()) {
            task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        }
    }

    private void stopTaskIfIdle() {
        if (task != null && trackedBlocks.isEmpty()) {
            task.cancel();
            task = null;
        }
    }

    private void restore(Block block) {
        Location location = block.getLocation();

        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) < VIEW_RANGE_SQUARED) {
                Compats.get().resetBlockChange(player, block);
            }
        }
        MetadataUtil.removeCustomBlock(block);
    }

    private void broadcastBlockChange(Block block, XMaterial material) {
        Location location = block.getLocation();

        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) < VIEW_RANGE_SQUARED) {
                Compats.get().sendBlockChange(player, location, material);
            }
        }
    }

    private static final class TrackedBlock {

        private final XMaterial material;
        private final long expiryTick;

        private TrackedBlock(XMaterial material, long expiryTick) {
            this.material = material;
            this.expiryTick = expiryTick;
        }

        boolean isExpired(long currentTick) {
            return expiryTick != PERMANENT && currentTick >= expiryTick;
        }
    }
}
