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
package com.ricodev.lumen.cosmetic.gadget.type;


import com.cryptomorin.xseries.XSound;

import com.ricodev.lumen.compat.Sounds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import com.ricodev.lumen.LumenPlugin;
import com.ricodev.lumen.api.cosmetic.CosmeticContext;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetBehavior;
import com.ricodev.lumen.api.cosmetic.gadget.GadgetType;
import com.ricodev.lumen.api.util.structure.StructureBlock;
import com.ricodev.lumen.util.entity.FloatingBlock;
import com.ricodev.lumen.util.entity.FloatingBlocks;

import java.util.*;

public class ConnectFour implements GadgetBehavior, Listener {

    private static final LumenPlugin PLUGIN = LumenPlugin.getPlugin();
    private static final Random RANDOM = new Random();

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final double CELL_SIZE = 0.3d;
    private static final double CELL_OFFSET = CELL_SIZE / 2;
    private static final double DROP_SPEED = 0.15d;
    private static final int GAME_OVER_DELAY_TICKS = 100;
    private static final StructureBlock FRAME_BLOCK = new StructureBlock("minecraft:black_concrete");
    private static final Vector BOARD_UP = new Vector(0.0d, 1.0d, 0.0d);

    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Ticks.duration(5),
            Ticks.duration(50),
            Ticks.duration(5)
    );

    private enum GameState {

        WAITING_FOR_OPPONENT,
        PLAYING,
        DROPPING_PIECE,
        GAME_OVER
    }

    private static final class Cell {

        private final int column;
        private final int row;

        Cell(int column, int row) {
            this.column = column;
            this.row = row;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Cell && ((Cell) other).column == column && ((Cell) other).row == row;
        }

        @Override
        public int hashCode() {
            return column * 31 + row;
        }
    }

    private enum CellState {

        EMPTY,
        PLAYER_ONE(new StructureBlock("minecraft:red_concrete"), Color.RED),
        PLAYER_TWO(new StructureBlock("minecraft:blue_concrete"), Color.BLUE);

        private final StructureBlock material;
        private final Color color;

        CellState() {
            this(null, null);
        }

        CellState(StructureBlock material, Color color) {
            this.material = material;
            this.color = color;
        }

        public StructureBlock getMaterial() {
            return material;
        }

        public Color getColor() {
            return color;
        }
    }

    private Player owner;
    private Player opponent;
    private UUID lastClicked;

    private GameState gameState;
    private boolean isPlayerOneTurn;
    private int gameOverTicks;

    private Location boardOrigin;
    private Vector boardRight;
    private final CellState[][] grid = new CellState[COLUMNS][ROWS];
    private final Set<FloatingBlock> boardFrame = new HashSet<>();
    private final Map<Cell, FloatingBlock> placedPieces = new HashMap<>();

    private FloatingBlock hoverEntity;
    private FloatingBlock droppingEntity;
    private int currentHoverColumn;
    private int targetRow;
    private double currentDropY;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        this.owner = context.getPlayer();
        resetGame();
    }

    private void resetGame() {
        gameState = GameState.WAITING_FOR_OPPONENT;
        isPlayerOneTurn = RANDOM.nextBoolean();
        opponent = null;
        currentHoverColumn = COLUMNS / 2;
        gameOverTicks = 0;

        for (int col = 0; col < COLUMNS; col++) {
            Arrays.fill(grid[col], CellState.EMPTY);
        }
        FloatingBlocks.remove(hoverEntity);
        FloatingBlocks.remove(droppingEntity);
        hoverEntity = null;
        droppingEntity = null;

        for (FloatingBlock piece : placedPieces.values()) {
            FloatingBlocks.remove(piece);
        }
        for (FloatingBlock frame : boardFrame) {
            FloatingBlocks.remove(frame);
        }
        placedPieces.clear();
        boardFrame.clear();
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action,
                                        @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        if (gameState != GameState.WAITING_FOR_OPPONENT) {
            return InteractionResult.fail();
        }
        Player clickedPlayer = null;

        if (lastClicked != null) {
            clickedPlayer = PLUGIN.getJavaPlugin().getServer().getPlayer(lastClicked);
        }

        if (clickedPlayer == null || clickedPlayer.equals(context.getPlayer())) {
            context.getUser().sendMessage(context.getUser().translate("cosmetic.gadgets.click_player"));
            Sounds.playFor(context.getPlayer(), XSound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            return InteractionResult.fail();
        }
        opponent = clickedPlayer;
        gameState = GameState.PLAYING;
        initializeBoard(context.getPlayer());
        spawnHoverPiece();
        showTeams();
        playCurrentSound();
        return InteractionResult.success();
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        if (gameState != GameState.PLAYING) {
            return;
        }
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (player.equals(getCurrentPlayer()) &&
                (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
            dropPiece();
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (gameState != GameState.PLAYING) {
            return;
        }
        if (event.getDamager() instanceof Player && event.getDamager().equals(getCurrentPlayer())) {
            dropPiece();
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().equals(owner) && event.getRightClicked() instanceof Player) {
            lastClicked = event.getRightClicked().getUniqueId();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(opponent)) {
            resetGame();
        }
    }

    private void initializeBoard(Player player) {
        Location playerLoc = player.getLocation();
        playerLoc.setPitch(0.0f);
        float yaw = 90.0f * (Math.round(playerLoc.getYaw() / 90.0f) & 0x3);
        playerLoc.setYaw(yaw);
        Vector direction = playerLoc.getDirection().setY(0).normalize();

        boardRight = direction.clone().crossProduct(BOARD_UP).normalize().multiply(-1);

        boardOrigin = playerLoc.clone()
                .add(direction.multiply(2.0d))
                .subtract(boardRight.clone().multiply(COLUMNS * CELL_OFFSET));
        boardOrigin.setY(playerLoc.getY() + CELL_SIZE);

        spawnBoardFrame();

        Location center = getBoardCenter();
        Sounds.playAt(center, XSound.BLOCK_BASALT_PLACE, 0.5f, 1.0f);
    }

    private void spawnBoardFrame() {
        for (int col = -1; col <= COLUMNS; col++) {
            for (int row = -1; row <= ROWS; row++) {
                if (col == -1 || col == COLUMNS || row == -1 || row == ROWS) {
                    Location loc = getGridLocation(col, row);
                    boardFrame.add(createBlockDisplay(loc, FRAME_BLOCK, null, false));
                }
            }
            Location loc1 = getGridLocation(col, -1, -1);
            boardFrame.add(createBlockDisplay(loc1, FRAME_BLOCK, null, false));

            Location loc2 = getGridLocation(col, -1, 1);
            boardFrame.add(createBlockDisplay(loc2, FRAME_BLOCK, null, false));
        }
    }

    private void spawnHoverPiece() {
        CellState state = getCurrentCellState();
        Location loc = getGridLocation(currentHoverColumn, ROWS);
        hoverEntity = createBlockDisplay(loc, state.getMaterial(), state.getColor(), true);
    }

    private void showTeams() {
        Player currentPlayer = getCurrentPlayer();
        Player waitingPlayer = isPlayerOneTurn ? opponent : owner;

        CellState currentState = getCurrentCellState();
        CellState waitingState = isPlayerOneTurn ? CellState.PLAYER_TWO : CellState.PLAYER_ONE;

        showTitle(currentPlayer, currentState.color);
        showTitle(waitingPlayer, waitingState.color);
    }

    private void showTitle(Player player, Color color) {
        PLUGIN.getUserManager().getConnected(player).showTitle(
                Component.text(" "),
                Component.text("■", TextColor.color(color.asRGB())),
                TITLE_TIMES
        );
    }

    private FloatingBlock createBlockDisplay(Location location, StructureBlock material, Color glowColor, boolean glowing) {
        FloatingBlock block = FloatingBlocks.cube(location, material, (float) CELL_SIZE * (glowing ? 0.9f : 1.0f));

        if (glowing) {
            block.glow(glowColor);
        }
        return block;
    }

    private Location getBoardCenter() {
        return getGridLocation(COLUMNS / 2, ROWS / 2);
    }

    private Location getGridLocation(int column, int row, int forward) {
        Vector boardForward = boardRight.clone().crossProduct(BOARD_UP);

        return boardOrigin.clone()
                .add(boardRight.clone().multiply(column * CELL_SIZE + CELL_OFFSET))
                .add(BOARD_UP.clone().multiply(row * CELL_SIZE + CELL_OFFSET))
                .add(boardForward.multiply(forward * CELL_SIZE));
    }

    private Location getGridLocation(int column, int row) {
        return boardOrigin.clone()
                .add(boardRight.clone().multiply(column * CELL_SIZE + CELL_OFFSET))
                .add(BOARD_UP.clone().multiply(row * CELL_SIZE + CELL_OFFSET));
    }

    private Location getDropLocation(int column, double y) {
        return boardOrigin.clone()
                .add(boardRight.clone().multiply(column * CELL_SIZE + CELL_OFFSET))
                .add(BOARD_UP.clone().multiply(y));
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        switch (gameState) {
            case PLAYING:
                updateHoverPosition(getCurrentPlayer());
                break;
            case DROPPING_PIECE:
                updateDroppingPiece();
                break;
            case GAME_OVER:
                if (++gameOverTicks >= GAME_OVER_DELAY_TICKS) {
                    resetGame();
                }
                break;
            default:
                break;
        }
    }

    private void updateHoverPosition(Player player) {
        if (hoverEntity == null) {
            return;
        }
        int newColumn = Math.max(0, Math.min(COLUMNS - 1, calculateLookedColumn(player)));

        if (newColumn != currentHoverColumn) {
            currentHoverColumn = newColumn;
        }
        Location loc = getGridLocation(currentHoverColumn, ROWS);
        hoverEntity.teleport(loc);
    }

    private int calculateLookedColumn(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector lookDir = eyeLoc.getDirection();

        Location boardCenter = getGridLocation(COLUMNS / 2, ROWS / 2);
        Vector toBoard = boardCenter.toVector().subtract(eyeLoc.toVector());
        double distance = toBoard.length();
        Location lookPoint = eyeLoc.clone().add(lookDir.multiply(distance));

        Vector offset = lookPoint.toVector().subtract(boardOrigin.toVector());
        return (int) (offset.dot(boardRight) / CELL_SIZE);
    }

    private void dropPiece() {
        targetRow = findEmptyRow(currentHoverColumn);

        if (targetRow == -1) {
            Sounds.playFor(getCurrentPlayer(), XSound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 0.5f);
            return;
        }
        gameState = GameState.DROPPING_PIECE;
        currentDropY = ROWS * CELL_SIZE + CELL_OFFSET;

        droppingEntity = hoverEntity;
        hoverEntity = null;

        setEntityGlowing(droppingEntity, false, null);

        Location location = droppingEntity.getLocation();
        Sounds.playAt(location, XSound.ENTITY_CHICKEN_EGG, 0.5f, 1.5f);
    }

    private int findEmptyRow(int column) {
        for (int row = 0; row < ROWS; row++) {
            if (grid[column][row] == CellState.EMPTY) {
                return row;
            }
        }
        return -1;
    }

    private void updateDroppingPiece() {
        if (droppingEntity == null) {
            return;
        }
        double targetY = targetRow * CELL_SIZE + CELL_OFFSET;
        currentDropY -= DROP_SPEED;

        if (currentDropY <= targetY) {
            currentDropY = targetY;
            droppingEntity.teleport(getDropLocation(currentHoverColumn, currentDropY));
            pieceLanded();
            return;
        }
        droppingEntity.teleport(getDropLocation(currentHoverColumn, currentDropY));
    }

    private void pieceLanded() {
        CellState state = getCurrentCellState();
        grid[currentHoverColumn][targetRow] = state;
        placedPieces.put(new Cell(currentHoverColumn, targetRow), droppingEntity);

        droppingEntity = null;

        List<int[]> winningCells = findWinningCells(currentHoverColumn, targetRow);

        if (!winningCells.isEmpty()) {
            handleWin(winningCells);
            return;
        }

        if (isBoardFull()) {
            handleDraw();
            return;
        }
        nextTurn();
    }

    private void handleWin(List<int[]> winningCells) {
        gameState = GameState.GAME_OVER;
        Color glowColor = getCurrentCellState().getColor();

        for (int[] cell : winningCells) {
            FloatingBlock piece = placedPieces.get(new Cell(cell[0], cell[1]));
            setEntityGlowing(piece, true, glowColor);
        }
        Location location = getBoardCenter();
        Sounds.playAt(location, XSound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.75f);
    }

    private void handleDraw() {
        gameState = GameState.GAME_OVER;
        Location location = getBoardCenter();
        Sounds.playAt(location, XSound.BLOCK_NOTE_BLOCK_BANJO, 0.5f, 0.5f);
    }

    private void nextTurn() {
        isPlayerOneTurn = !isPlayerOneTurn;
        gameState = GameState.PLAYING;
        currentHoverColumn = COLUMNS / 2;
        spawnHoverPiece();
        playCurrentSound();
    }

    private void playCurrentSound() {
        Player currentPlayer = getCurrentPlayer();
        Sounds.playFor(currentPlayer, XSound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 1.0f);
    }

    private void setEntityGlowing(FloatingBlock entity, boolean glowing, Color color) {
        if (entity != null) {
            entity.glow(glowing ? color : null);
        }
    }

    private List<int[]> findWinningCells(int col, int row) {
        CellState state = grid[col][row];
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            List<int[]> cells = collectLine(col, row, state, dir[0], dir[1]);

            if (cells.size() >= 4) {
                return cells;
            }
        }
        return Collections.emptyList();
    }

    private List<int[]> collectLine(int col, int row, CellState state, int dCol, int dRow) {
        List<int[]> cells = new ArrayList<>();
        cells.add(new int[]{col, row});

        collectInDirection(cells, col, row, state, dCol, dRow);
        collectInDirection(cells, col, row, state, -dCol, -dRow);

        return cells;
    }

    private void collectInDirection(List<int[]> cells, int col, int row, CellState state, int dCol, int dRow) {
        int c = col + dCol;
        int r = row + dRow;

        while (c >= 0 && c < COLUMNS && r >= 0 && r < ROWS && grid[c][r] == state) {
            cells.add(new int[]{c, r});
            c += dCol;
            r += dRow;
        }
    }

    private boolean isBoardFull() {
        for (int col = 0; col < COLUMNS; col++) {
            if (grid[col][ROWS - 1] == CellState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    private Player getCurrentPlayer() {
        return isPlayerOneTurn ? owner : opponent;
    }

    private CellState getCurrentCellState() {
        return isPlayerOneTurn ? CellState.PLAYER_ONE : CellState.PLAYER_TWO;
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        resetGame();
    }

    @Override
    public boolean requiresGroundOnUse() {
        return true;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        return true;
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }
}
