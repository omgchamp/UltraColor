package me.ultimategamer200.ultracolor;

import com.Zrips.CMI.CMI;
import com.earth2me.essentials.Essentials;
import lombok.SneakyThrows;
import me.ultimategamer200.ultracolor.commands.*;
import me.ultimategamer200.ultracolor.gradients.PreDefinedGradientManager;
import me.ultimategamer200.ultracolor.hooks.PlaceholderAPIHook;
import me.ultimategamer200.ultracolor.listeners.ChatListener;
import me.ultimategamer200.ultracolor.listeners.DatabaseListener;
import me.ultimategamer200.ultracolor.listeners.PlayerListener;
import me.ultimategamer200.ultracolor.mysql.UltraColorDatabase;
import me.ultimategamer200.ultracolor.settings.AllowedHexesData;
import me.ultimategamer200.ultracolor.settings.Localization;
import me.ultimategamer200.ultracolor.settings.Settings;
import me.ultimategamer200.ultracolor.subcommands.UltraColorCommandGroup;
import me.ultimategamer200.ultracolor.util.Filter;
import me.ultimategamer200.ultracolor.util.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.debug.LagCatcher;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonReturnBack;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SpigotUpdater;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * The main plugin instance. This class extends SimplePlugin rather than JavaPlugin because we use the Foundation library!
 * This library allows developers to cut time spent on coding by at least 1/10th, saving time on making products everyone can
 * enjoy!
 */
public class UltraColorPlugin extends SimplePlugin {
	/**
	 * The main command group. DO NOT REMOVE.
	 */
	private final SimpleCommandGroup mainCommandGroup = new UltraColorCommandGroup();

	@SneakyThrows
	@Override
	protected void onPluginStart() {
		// Injects our custom filter to the console.
		Filter.inject();
		// The log prefix for the plugin to use in console.
		Common.setLogPrefix("[UltraColor]");

		// If the server has a Java 15, but doesn't have NashornPlus and is older than 1.16, disable the plugin for safe keeping.
		if (Remain.getJavaVersion() >= 15 && !Common.doesPluginExist("NashornPlus")) {
			if (!MinecraftVersion.atLeast(MinecraftVersion.V.v1_16)) {
				Common.log("Detected server using Java 15+ without NashornPlus plugin!",
						"Please install NashornPlus plugin from bitbucket.org/kangarko/nashornplus/downloads/",
						"Disabling plugin to be on the safe side!");
				getPluginLoader().disablePlugin(this);
				return;
			}
		}

		Common.log(Common.consoleLineSmooth());
		Common.log(" _   _ _ _             _____       _            \n" +
				"| | | | | |           /  __ \\     | |           \n" +
				"| | | | | |_ _ __ __ _| /  \\/ ___ | | ___  _ __ \n" +
				"| | | | | __| '__/ _` | |    / _ \\| |/ _ \\| '__|\n" +
				"| |_| | | |_| | | (_| | \\__/\\ (_) | | (_) | |   \n" +
				" \\___/|_|\\__|_|  \\__,_|\\____/\\___/|_|\\___/|_| ");
		Common.log("Plugin loaded successfully!");
		Common.log("Made by: " + getPluginCreator());

		// Registers plugin commands and listeners.
		registerCommands();
		registerEvents();

		// Uses a prefix when using Common.tell() methods.
		Common.ADD_TELL_PREFIX = true;

		// Adds bStats to UltraColor on startup
		final Metrics metrics = new Metrics(this, 9266);
		final File mainConfig = new File(UltraColorPlugin.getPlugin(UltraColorPlugin.class).getDataFolder() + File.separator + "settings.yml");
		final YamlConfiguration yamlMainConfig = YamlConfiguration.loadConfiguration(mainConfig);

		metrics.addCustomChart(new Metrics.SimplePie("used_locale", () -> yamlMainConfig.getString("Locale")));
		metrics.addCustomChart(new Metrics.SimplePie("notify_updates", () -> String.valueOf(yamlMainConfig.getBoolean("Notify_Updates"))));

		// Checks if change-displayname is false in Essentials and/or CMI.
		if (HookManager.isEssentialsLoaded() || HookManager.isCMILoaded()) {
			if (HookManager.isEssentialsLoaded()) {
				final File essentialsXConfig = new File(Essentials.getPlugin(Essentials.class).getDataFolder() + File.separator + "config.yml");
				final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(essentialsXConfig);
				final boolean changeDisplayName = yamlConfiguration.getBoolean("change-displayname");

				if (changeDisplayName && Settings.Color_Settings.NAME_COLORS) {
					Common.log("Detected change-displayname set to 'true' in EssentialsX config.yml file!");
					Common.log("Please set this option to 'false' if you want name coloring functionality!");
				}
			} else {
				final File cmiConfig = new File(CMI.getPlugin(CMI.class).getDataFolder() + File.separator + "config.yml");
				final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(cmiConfig);
				final boolean changeDisplayName = yamlConfiguration.getBoolean("DisplayName.Change");

				if (changeDisplayName && Settings.Color_Settings.NAME_COLORS) {
					Common.log("Detected DisplayName.Change option set to 'true' in CMI config.yml file!");
					Common.log("Please set this option to 'false' if you want name coloring functionality!");
				}
			}
		}

		if (Localization.Gradient_Color_Selection_Return.ENABLED) {
			ButtonReturnBack.setMaterial(Settings.Gradient_Color_Menu_Items.RETURN_ITEM);
			ButtonReturnBack.setTitle(Localization.Gradient_Color_Selection_Return.CUSTOM_NAME);
			ButtonReturnBack.setLore(Localization.Gradient_Color_Selection_Return.CUSTOM_LORE);
		}

		Common.log(Common.consoleLineSmooth());
	}

	private void registerCommands() {
		registerCommand(new ColorCommand());
		registerCommand(new ChatColorCommand());
		registerCommand(new NameColorCommand());

		if (Settings.Other.NICKNAMES_ENABLE) {
			registerCommand(new NicknameCommand());
			registerCommand(new RealNameCommand());
		}

		if (Settings.Color_Settings.CHAT_HEX_COLORS || Settings.Color_Settings.NAME_HEX_COLORS) {
			if (Remain.hasHexColors())
				registerCommand(new HexColorCommand());
		}
		if (Settings.Color_Settings.CHAT_GRADIENT_COLORS || Settings.Color_Settings.NAME_GRADIENT_COLORS) {
			if (Remain.hasHexColors())
				registerCommand(new GradientCommand());
		}
	}

	private void registerEvents() {
		registerEvents(new PlayerListener());
		registerEvents(ChatListener.getInstance());

		// Database Registration
		if (Settings.Database.ENABLED) {
			LagCatcher.start("mysql");
			final String databaseURL = "jdbc:mysql://{host}/{database}?autoReconnect=true&useSSL=false";
			UltraColorDatabase.getInstance().connect(databaseURL.replace("{host}", Settings.Database.HOST + ":" + Settings.Database.PORT)
					.replace("{database}", Settings.Database.DATABASE), Settings.Database.USER, Settings.Database.PASS, "UltraColor");
			LagCatcher.end("mysql", 0, "Connection to MySQL established. Took {time} ms.");
			registerEvents(new DatabaseListener());
		}
	}

	// This method is called when you start the plugin AND when you reload it
	@Override
	protected void onReloadablesStart() {
		Messenger.setSuccessPrefix(Settings.Other_Prefixes.SUCCESS_PREFIX);
		Messenger.setErrorPrefix(Settings.Other_Prefixes.ERROR_PREFIX);

		// Loads pre-defined gradients if gradients are enabled and server is 1.16+
		if (Settings.Color_Settings.CHAT_GRADIENT_COLORS || Settings.Color_Settings.NAME_GRADIENT_COLORS) {
			if (Remain.hasHexColors())
				PreDefinedGradientManager.loadPreDefinedGradients();
		}

		if (Localization.Main_GUI_Customization_Chat_Color_Selection.ALLOW_INFO_BUTTON || Localization.Main_GUI_Customization_Name_Color_Selection.ALLOW_INFO_BUTTON
				|| Localization.Gradient_Color_Selection.ALLOW_INFO_BUTTON || Localization.Main_GUI_Customization.ALLOW_INFO_BUTTON) {
			Button.setInfoButtonMaterial(Settings.Other.INFO_ITEM);
			Button.setInfoButtonTitle(Localization.Menu_Titles.MENU_INFORMATION_TITLE);
		}

		if (Localization.Gradient_Color_Selection_Return.ENABLED) {
			ButtonReturnBack.setMaterial(Settings.Gradient_Color_Menu_Items.RETURN_ITEM);
			ButtonReturnBack.setTitle(Localization.Gradient_Color_Selection_Return.CUSTOM_NAME);
			ButtonReturnBack.setLore(Localization.Gradient_Color_Selection_Return.CUSTOM_LORE);
		}

		if (HookManager.isPlaceholderAPILoaded()) {
			new PlaceholderAPIHook().register();
			Common.log("Placeholders loaded!");
		}

		if (Settings.Color_Settings.ALLOW_ONLY_CERTAIN_HEX_COLORS) {
			final File file = FileUtil.getFile("allowed-hexes.yml");

			if (!file.exists()) {
				new AllowedHexesData();
				Common.log("Whitelisted Hexes are enabled!");
			}
		}

		// Save the data.db file
		DataFile.getInstance().save();

		// Clear the cache in the plugin so that we load it fresh for only players that are online right now,
		// saving memory
		PlayerCache.clearAllData();
	}

	@Override
	protected void onPluginStop() {
		if (Settings.Database.ENABLED && UltraColorDatabase.getInstance().isLoaded()) {
			Common.runLaterAsync(10, () -> {
				for (final Player player : Remain.getOnlinePlayers())
					UltraColorDatabase.getInstance().save(player.getName(), player.getUniqueId(), PlayerCache.fromPlayer(player));
			});
		}
	}

	public String getPluginCreator() {
		return "UltimateGamer200";
	}

	/**
	 * Gets the UltraColor Spigot resource to check updates for.
	 */
	@Override
	public SpigotUpdater getUpdateCheck() {
		return new SpigotUpdater(85332);
	}

	@Override
	public int getFoundedYear() {
		return 2020;
	}

	@Override
	public MinecraftVersion.V getMinimumVersion() {
		return MinecraftVersion.V.v1_8;
	}

	@Override
	public MinecraftVersion.V getMaximumVersion() {
		return MinecraftVersion.V.v1_18;
	}

	@Override
	public SimpleCommandGroup getMainCommand() {
		return mainCommandGroup;
	}

	// Automatically load classes extending YamlStaticConfig
	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class, Localization.class);
	}
}
