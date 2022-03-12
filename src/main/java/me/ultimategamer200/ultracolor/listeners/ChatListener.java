package me.ultimategamer200.ultracolor.listeners;

import lombok.Getter;
import me.ultimategamer200.ultracolor.PlayerCache;
import me.ultimategamer200.ultracolor.gradients.PreDefinedGradient;
import me.ultimategamer200.ultracolor.gradients.PreDefinedGradientManager;
import me.ultimategamer200.ultracolor.settings.Settings;
import me.ultimategamer200.ultracolor.util.UltraColorPermissions;
import me.ultimategamer200.ultracolor.util.UltraColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.event.SimpleListener;
import org.mineacademy.fo.remain.CompChatColor;

/**
 * This listener listens to specifically the AsyncPlayerChatEvent.
 */
public class ChatListener extends SimpleListener<AsyncPlayerChatEvent> {
	/**
	 * The singleton instance
	 */
	@Getter
	private static final ChatListener instance = new ChatListener();

	// Creates the listener.
	public ChatListener() {
		super(AsyncPlayerChatEvent.class, Settings.CHAT_LISTENER_PRIORITY, true);
	}

	@Override
	protected void execute(AsyncPlayerChatEvent event) {
		final String message = event.getMessage();
		final Player player = event.getPlayer();
		final PlayerCache pCache = PlayerCache.fromPlayer(player);
		clearNameColorsIfNoPermission(player);
		clearChatColorsIfNoPermission(player);

		if (pCache.getChatCustomGradient1() != null || pCache.getChatCustomGradient2() != null) {
			if (pCache.getChatCustomGradient1() != null && pCache.getChatCustomGradient2() != null) {
				if (pCache.getChatFormat() == null)
					event.setMessage(ChatUtil.generateGradient(message, pCache.getChatCustomGradient1(), pCache.getChatCustomGradient2()));
				else
					event.setMessage(ChatUtil.generateGradient(UltraColorUtil.chatFormatToString(pCache.getChatFormat()) + message,
							pCache.getChatCustomGradient1(), pCache.getChatCustomGradient2()));
				return;
			}
		}

		if (pCache.isChatRainbowColors()) {
			if (pCache.getChatFormat() != null)
				event.setMessage(UltraColorUtil.convertStringToRainbow(message, true, pCache.getChatFormat().getName()));
			else
				event.setMessage(UltraColorUtil.convertStringToRainbow(message, false, ""));
			return;
		}

		if (pCache.getChatColor() != null) {
			if (pCache.getChatFormat() != null)
				event.setMessage(pCache.getChatColor() + UltraColorUtil.chatFormatToString(pCache.getChatFormat()) + message);
			else
				event.setMessage(UltraColorUtil.nameAndChatColorToString(pCache.getChatColor()) + message);
			return;
		}

		if (pCache.getChatFormat() != null && pCache.getChatColor() == null)
			event.setMessage(UltraColorUtil.chatFormatToString(pCache.getChatFormat()) + message);
	}

	private void clearNameColorsIfNoPermission(final Player player) {
		final PlayerCache pCache = PlayerCache.fromPlayer(player);

		if (pCache.getNameColor() != null || pCache.isNameRainbowColors()) {
			if (!player.hasPermission(UltraColorPermissions.Color.NAME_COLOR.replace("{color}", "*"))) {
				if (pCache.isNameRainbowColors()) {
					if (!player.hasPermission(UltraColorPermissions.Color.NAME_COLOR.replace("{color}", "r"))) {
						pCache.setNameRainbowColors(false);
						modifyNickDueToNoColorPermission(player);
					}
				} else {
					if (!pCache.getNameColor().isHex()) {
						if (!player.hasPermission(UltraColorPermissions.Color.NAME_COLOR.replace("{color}", String.valueOf(pCache.getNameColor()
								.getCode())))) {
							pCache.setNameColor(null);
							modifyNickDueToNoColorPermission(player);
						}
					} else {
						if (!player.hasPermission(UltraColorPermissions.Command.HEX_COLOR)) {
							pCache.setNameColor(null);
							modifyNickDueToNoColorPermission(player);
						}
					}
				}

				player.setDisplayName(UltraColorUtil.getPlayerNameInColor(player));
				return;
			}
		}

		if (pCache.getCustomGradient1() != null && pCache.getCustomGradient2() != null) {
			if (!player.hasPermission(UltraColorPermissions.Color.NAME_GRADIENTS.replace("{gradient-color}", "*"))) {
				for (final PreDefinedGradient gradient : PreDefinedGradientManager.getLoadedPreDefinedGradients()) {
					if (!gradient.getSettings().getType().equalsIgnoreCase("name") && !gradient.getSettings().getType()
							.equalsIgnoreCase("both"))
						continue;

					if (!player.hasPermission(gradient.getSettings().getPermission())) {
						if (pCache.getCustomGradient1().equals(CompChatColor.of(gradient.getSettings().getHexColors().get(0)))
								&& pCache.getCustomGradient2().equals(CompChatColor.of(gradient.getSettings().getHexColors().get(1)))) {
							pCache.setCustomGradient1(null);
							pCache.setCustomGradient2(null);
							modifyNickDueToNoColorPermission(player);
							break;
						}
					}
				}

				if (!player.hasPermission(UltraColorPermissions.Color.NAME_GRADIENTS.replace("{gradient-color}", "custom"))) {
					if (pCache.getCustomGradient1() != null && pCache.getCustomGradient2() != null) {
						pCache.setCustomGradient1(null);
						pCache.setCustomGradient2(null);
						modifyNickDueToNoColorPermission(player);
					}
				}

				player.setDisplayName(UltraColorUtil.getPlayerNameInColor(player));
			}
		}
	}

	private void modifyNickDueToNoColorPermission(final Player player) {
		final PlayerCache pCache = PlayerCache.fromPlayer(player);

		if (!pCache.getNickName().equalsIgnoreCase("none")) {
			if (pCache.getNameFormat() != null && !player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", "*"))) {
				if (player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", String.valueOf(pCache.getNameFormat().getChar()))))
					pCache.setColoredNickName(UltraColorUtil.nameFormatToString(pCache.getNameFormat()) + pCache.getNickName());
				else {
					pCache.setNameFormat(null);
					pCache.setColoredNickName(pCache.getNickName());
				}
			} else if (!player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", "*")))
				pCache.setColoredNickName(pCache.getNickName());
		} else {
			if (pCache.getNameFormat() != null && !player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", "*"))) {
				if (!player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", String.valueOf(pCache.getNameFormat().getChar()))))
					pCache.setNameFormat(null);
			} else if (!player.hasPermission(UltraColorPermissions.Color.NAME_FORMAT.replace("{format}", "*")))
				pCache.setColoredNickName(pCache.getNickName());
		}
	}

	private void clearChatColorsIfNoPermission(final Player player) {
		final PlayerCache pCache = PlayerCache.fromPlayer(player);

		if (pCache.getChatFormat() != null && !player.hasPermission(UltraColorPermissions.Color.CHAT_FORMAT.replace("{format}", "*"))) {
			if (!player.hasPermission(UltraColorPermissions.Color.CHAT_FORMAT.replace("{format}", String.valueOf(pCache.getChatFormat().getCode()))))
				pCache.setChatFormat(null);
		}

		if (pCache.getChatColor() != null || pCache.isChatRainbowColors()) {
			if (!player.hasPermission(UltraColorPermissions.Color.CHAT_COLOR.replace("{color}", "*"))) {
				if (pCache.isChatRainbowColors()) {
					if (!player.hasPermission(UltraColorPermissions.Color.CHAT_COLOR.replace("{color}", "r")))
						pCache.setChatRainbowColors(false);
				} else {
					if (!pCache.getChatColor().isHex()) {
						if (!player.hasPermission(UltraColorPermissions.Color.CHAT_COLOR.replace("{color}", String.valueOf(pCache.getChatColor()
								.getCode()))))
							pCache.setChatColor(null);
					} else {
						if (!player.hasPermission(UltraColorPermissions.Command.HEX_COLOR))
							pCache.setChatColor(null);
					}
				}
				return;
			}
		}

		if (pCache.getChatCustomGradient1() != null && pCache.getChatCustomGradient2() != null) {
			if (!player.hasPermission(UltraColorPermissions.Color.CHAT_GRADIENTS.replace("{gradient-color}", "*"))) {
				for (final PreDefinedGradient gradient : PreDefinedGradientManager.getLoadedPreDefinedGradients()) {
					if (!gradient.getSettings().getType().equalsIgnoreCase("chat") && !gradient.getSettings().getType()
							.equalsIgnoreCase("both"))
						continue;

					if (!player.hasPermission(gradient.getSettings().getPermission())) {
						if (pCache.getChatCustomGradient1().equals(CompChatColor.of(gradient.getSettings().getHexColors().get(0)))
								&& pCache.getChatCustomGradient2().equals(CompChatColor.of(gradient.getSettings().getHexColors().get(1)))) {
							pCache.setChatCustomGradient1(null);
							pCache.setChatCustomGradient2(null);
							break;
						}
					}
				}

				if (!player.hasPermission(UltraColorPermissions.Color.CHAT_GRADIENTS.replace("{gradient-color}", "custom"))) {
					if (pCache.getChatCustomGradient1() != null && pCache.getChatCustomGradient2() != null) {
						pCache.setChatCustomGradient1(null);
						pCache.setChatCustomGradient2(null);
					}
				}
			}
		}
	}
}