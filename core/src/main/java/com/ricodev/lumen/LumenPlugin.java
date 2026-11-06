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
package com.ricodev.lumen;

import com.ricodev.lumen.util.entity.Entities;
import com.ricodev.lumen.util.entity.FloatingBlocks;

import com.ricodev.lumen.api.Lumen;
import com.ricodev.lumen.api.LumenProvider;
import com.ricodev.lumen.api.cosmetic.registry.CategoryRegistries;
import com.ricodev.lumen.api.cosmetic.registry.CosmeticRarityRegistry;
import com.ricodev.lumen.api.storage.Database;
import com.ricodev.lumen.api.user.User;
import com.ricodev.lumen.command.CommandBase;
import com.ricodev.lumen.command.SimpleCommand;
import com.ricodev.lumen.command.commands.CosmeticsCommand;
import com.ricodev.lumen.command.commands.EquipCommand;
import com.ricodev.lumen.command.commands.LumenCommand;
import com.ricodev.lumen.command.commands.ToggleSelfViewCommand;
import com.ricodev.lumen.command.commands.UnequipAllCommand;
import com.ricodev.lumen.command.commands.UnequipCommand;
import com.ricodev.lumen.compat.Compats;
import com.ricodev.lumen.compat.ServerVersion;
import com.ricodev.lumen.config.ConfigManagerImpl;
import com.ricodev.lumen.cosmetic.registry.CategoryRegistriesImpl;
import com.ricodev.lumen.cosmetic.registry.CosmeticRarityRegistryImpl;
import com.ricodev.lumen.economy.EconomyManagerImpl;
import com.ricodev.lumen.listener.BlockListener;
import com.ricodev.lumen.listener.CosmeticItemListener;
import com.ricodev.lumen.listener.CosmeticListener;
import com.ricodev.lumen.listener.CreatureSpawnListener;
import com.ricodev.lumen.listener.EntityListener;
import com.ricodev.lumen.listener.FallDamageListener;
import com.ricodev.lumen.listener.InventoryListener;
import com.ricodev.lumen.listener.PlayerListener;
import com.ricodev.lumen.listener.hook.essentials.EssentialsVanishListener;
import com.ricodev.lumen.listener.hook.premiumvanish.PremiumVanishListener;
import com.ricodev.lumen.locale.LanguageManagerImpl;
import com.ricodev.lumen.menu.MenuManagerImpl;
import com.ricodev.lumen.placeholder.PlaceholderManager;
import com.ricodev.lumen.platform.LegacyAdapter;
import com.ricodev.lumen.storage.DatabaseTypeProvider;
import com.ricodev.lumen.api.treasure.TreasureChestPlatform;
import com.ricodev.lumen.api.treasure.animation.TreasureChestAnimationRegistry;
import com.ricodev.lumen.treasure.TreasureChestManagerImpl;
import com.ricodev.lumen.treasure.animation.TreasureChestAnimationRegistryImpl;
import com.ricodev.lumen.user.UserManagerImpl;
import com.ricodev.lumen.util.ResourceExporter;
import com.ricodev.lumen.util.block.FakeBlockManager;
import com.ricodev.lumen.util.item.ItemParser;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class LumenPlugin extends JavaPlugin implements Lumen {

    private static LumenPlugin plugin;

    private Logger logger;
    private Executor syncExecutor;
    private Executor asyncExecutor;
    private ConfigManagerImpl configManager;
    private LanguageManagerImpl languageManager;
    private CosmeticRarityRegistryImpl cosmeticRarityRegistry;
    private CategoryRegistriesImpl categoryRegistries;
    private UserManagerImpl userManager;
    private TreasureChestAnimationRegistry treasureChestAnimationRegistry;
    private TreasureChestManagerImpl treasureChestManager;
    private MenuManagerImpl menuManager;
    private FakeBlockManager fakeBlockManager;
    private EconomyManagerImpl economyManager;
    private PlaceholderManager placeholderManager;
    private CommandBase commandBase;
    private LegacyAdapter platformAdapter;
    private Database database;

    @Override
    public void onLoad() {
        LumenPlugin.plugin = this;
        logger = getLogger();
        syncExecutor = runnable -> getServer().getScheduler().runTask(this, runnable);
        asyncExecutor = runnable -> getServer().getScheduler().runTaskAsynchronously(this, runnable);

        logger.info("Initializing on Minecraft " + ServerVersion.describe() + "...");
        Compats.init(logger);
        platformAdapter = new LegacyAdapter();

        ResourceExporter.export(this);

        configManager = new ConfigManagerImpl(this);
        languageManager = new LanguageManagerImpl(this);
        cosmeticRarityRegistry = new CosmeticRarityRegistryImpl(this);
        categoryRegistries = new CategoryRegistriesImpl(this);
        userManager = new UserManagerImpl(this);
        treasureChestAnimationRegistry = new TreasureChestAnimationRegistryImpl();
        treasureChestManager = new TreasureChestManagerImpl(this);
        menuManager = new MenuManagerImpl(this);
        fakeBlockManager = new FakeBlockManager(this);
        economyManager = new EconomyManagerImpl(this);
        placeholderManager = new PlaceholderManager(this);
        commandBase = new CommandBase(this);

        database = DatabaseTypeProvider.createDatabase(this, configManager.getMainConfig().getString("storage.type"));
    }

    @Override
    public void onEnable() {
        economyManager.hookPlugin();

        registerListeners();
        registerCommands();
        hookPlugins();

        com.github.retrooper.packetevents.PacketEvents.getAPI().getEventManager().registerListener(new com.ricodev.lumen.util.entity.RideInput());
        getServer().getScheduler().runTask(this, treasureChestManager::loadPlatforms);
        userManager.loadOnlinePlayers();
        LumenProvider.register(this);

        if (!ItemParser.MISSING.isEmpty()) {
            logger.info("These config items don't exist on this version, so they show as barriers: "
                    + String.join(", ", ItemParser.MISSING) + ". Swap them for something older if you want.");
        }
        logger.info("Initialized!");
    }

    private void registerListeners() {
        userManager.registerListeners();
        treasureChestManager.registerListeners();
        menuManager.registerListeners();

        registerListeners(new BlockListener(),
                new CosmeticItemListener(this),
                new CosmeticListener(this),
                new CreatureSpawnListener(),
                new EntityListener(this),
                new FallDamageListener(this),
                new InventoryListener(),
                new PlayerListener(this)
        );
    }

    @Override
    public void onDisable() {
        if (userManager != null) {
            for (User user : userManager.getAllConnected()) {
                user.clearAllCosmetics(true, false);
            }
        }
        Entities.removeDecorativeItems();
        FloatingBlocks.removeAll();

        if (database != null) {
            database.shutdown();
        }
        if (treasureChestManager != null) {
            for (TreasureChestPlatform platform : treasureChestManager.getPlatforms()) {
                platform.hideDisplay();
            }
        }
        if (fakeBlockManager != null) {
            fakeBlockManager.shutdown();
        }
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    private void registerCommands() {
        commandBase.getCommands().clear();

        registerCommands(new CosmeticsCommand(this),
                new EquipCommand(this),
                new UnequipCommand(this),
                new UnequipAllCommand(this),
                new ToggleSelfViewCommand(this),
                new LumenCommand(this)
        );
    }

    private void hookPlugins() {
        PluginManager pluginManager = getServer().getPluginManager();

        if (pluginManager.getPlugin("Essentials") != null) {
            logger.info("Hooking into Essentials...");
            registerListeners(new EssentialsVanishListener(this));
        }
        if (pluginManager.getPlugin("PremiumVanish") != null || pluginManager.getPlugin("SuperVanish") != null) {
            logger.info("Hooking into PremiumVanish/SuperVanish...");
            registerListeners(new PremiumVanishListener(this));
        }
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            if (listener != null) {
                pluginManager.registerEvents(listener, this);
            }
        }
    }

    @SafeVarargs
    private final void registerCommands(SimpleCommand<CommandSender>... commands) {
        for (SimpleCommand<CommandSender> command : commands) {
            commandBase.registerCommand(command);
        }
    }

    public static LumenPlugin getPlugin() {
        return plugin;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public Executor getSyncExecutor() {
        return syncExecutor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    @Override
    public ConfigManagerImpl getConfigManager() {
        return configManager;
    }

    @Override
    public LanguageManagerImpl getLanguageManager() {
        return languageManager;
    }

    @Override
    public CosmeticRarityRegistry getCosmeticRarityRegistry() {
        return cosmeticRarityRegistry;
    }

    @Override
    public CategoryRegistries getCategoryRegistries() {
        return categoryRegistries;
    }

    @Override
    public UserManagerImpl getUserManager() {
        return userManager;
    }

    @Override
    public TreasureChestAnimationRegistry getTreasureChestAnimationRegistry() {
        return treasureChestAnimationRegistry;
    }

    @Override
    public TreasureChestManagerImpl getTreasureChestManager() {
        return treasureChestManager;
    }

    @Override
    public MenuManagerImpl getMenuManager() {
        return menuManager;
    }

    public FakeBlockManager getFakeBlockManager() {
        return fakeBlockManager;
    }

    @Override
    public EconomyManagerImpl getEconomyManager() {
        return economyManager;
    }

    @Override
    public LegacyAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public CommandBase getCommandBase() {
        return commandBase;
    }

    @Override
    public Database getStorage() {
        return database;
    }
}
