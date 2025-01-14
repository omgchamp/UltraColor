package me.ultimategamer200.ultracolor.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ultimategamer200.ultracolor.PlayerCache;
import me.ultimategamer200.ultracolor.util.UltraColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.remain.CompChatColor;

/**
 * This class stores all the placeholders for the PlaceholderAPI integration.
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
	@Override
	public @NotNull String getIdentifier() {
		return "ucolor";
	}

	@Override
	public @NotNull String getAuthor() {
		return "UltimateGamer200";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public String onRequest(OfflinePlayer player, @NotNull String identifier) {
		if (player == null)
			return "";

		final PlayerCache pCache = PlayerCache.fromOfflinePlayer(player);
		final String playerName = player.getName();
		final ChatColor nameFormat = pCache.getNameFormat();
		final String format = nameFormat != null ? nameFormat.name() : "";

		if (identifier.equals("chat_color"))
			return colorizeColorPlaceholder(pCache, "chat");
		if (identifier.equals("name_color"))
			return colorizeColorPlaceholder(pCache, "name");
		if (identifier.equals("chat_color_name"))
			return colorizeColorNamePlaceholder(pCache, "chat");
		if (identifier.equals("name_color_name"))
			return colorizeColorNamePlaceholder(pCache, "name");

		if (identifier.equals("nickname")) {
			if (!pCache.getNickName().equalsIgnoreCase("none")) return pCache.getNickName();
			else return "None";
		}

		if (identifier.equals("colored_nickname")) {
			if (!pCache.getColoredNickName().equalsIgnoreCase("none"))
				return pCache.getColoredNickName();
			else {
				if (pCache.isNameRainbowColors())
					return UltraColorUtil.convertStringToRainbow(playerName, nameFormat != null, format);
				if (pCache.getCustomGradientOne() != null && pCache.getCustomGradientTwo() != null)
					return ChatUtil.generateGradient(playerName, pCache.getCustomGradientOne(), pCache.getCustomGradientTwo());

				if (pCache.getNameColor() != null || pCache.getNameFormat() != null) {
					if (pCache.getNameColor() != null && pCache.getNameFormat() == null)
						return pCache.getNameColor() + playerName;
					else if (pCache.getNameColor() != null)
						return pCache.getNameColor() + pCache.getNameFormat().toString() + playerName;
				}

				return playerName;
			}
		}

		return null;
	}

	private String colorizeColorPlaceholder(final PlayerCache pCache, final String type) {
		final CompChatColor gradientOne;
		final CompChatColor gradientTwo;
		final CompChatColor color;
		final ChatColor format;

		if (type.equalsIgnoreCase("chat")) {
			gradientOne = pCache.getChatCustomGradientOne();
			gradientTwo = pCache.getChatCustomGradientTwo();
			color = pCache.getChatColor();
			format = UltraColorUtil.getNameFormatToChatColor(pCache.getChatFormat().getName());
		} else {
			gradientOne = pCache.getCustomGradientOne();
			gradientTwo = pCache.getCustomGradientTwo();
			color = pCache.getNameColor();
			format = pCache.getNameFormat();
		}

		if (gradientOne != null && gradientTwo != null) return gradientOne.toString() + gradientTwo;

		if (color != null || format != null) {
			if (color != null && format == null)
				return color.toString();
			else if (color != null)
				return color + format.toString();
		}

		return "";
	}

	private String colorizeColorNamePlaceholder(final PlayerCache pCache, final String type) {
		final boolean isRainbowColor;
		final CompChatColor gradientOne;
		final CompChatColor gradientTwo;
		final CompChatColor color;
		final ChatColor format;

		if (type.equalsIgnoreCase("chat")) {
			isRainbowColor = pCache.isChatRainbowColors();
			gradientOne = pCache.getChatCustomGradientOne();
			gradientTwo = pCache.getChatCustomGradientTwo();
			color = pCache.getChatColor();
			format = UltraColorUtil.getNameFormatToChatColor(pCache.getChatFormat().getName());
		} else {
			isRainbowColor = pCache.isNameRainbowColors();
			gradientOne = pCache.getCustomGradientOne();
			gradientTwo = pCache.getCustomGradientTwo();
			color = pCache.getNameColor();
			format = pCache.getNameFormat();
		}

		if (isRainbowColor) return "Rainbow";

		if (gradientOne != null && gradientTwo != null) {
			if (format == null) return ChatUtil.generateGradient("this", gradientOne, gradientTwo);
			else return ChatUtil.generateGradient(UltraColorUtil.nameFormatToString(format) + "this",
					gradientOne, gradientTwo);
		}
		if (color == null && gradientOne == null && gradientTwo == null && format == null) return "None";

		if (color != null) {
			if (color.isHex()) {
				if (format != null) return color + UltraColorUtil.nameFormatToString(format) + "this";
				return color + "this";
			}

			if (format != null) return UltraColorUtil.nameFormatToString(format) + color.getName();
			return color.getName();
		} else if (format != null) return format.name();

		return "";
	}
}
