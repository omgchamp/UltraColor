package me.ultimategamer200.ultracolor.commands;

import me.ultimategamer200.ultracolor.menu.ColorSelectionMenu;
import me.ultimategamer200.ultracolor.settings.Localization;
import me.ultimategamer200.ultracolor.util.UltraColorPermissions;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.SimpleCommand;

@AutoRegister
public final class ColorCommand extends SimpleCommand {
	public ColorCommand() {
		super("ultracolor|uc");
		setPermission(UltraColorPermissions.COLOR);
		setPermissionMessage(Localization.Other.NO_PERMISSION.replace("{permission}", UltraColorPermissions.COLOR));
		setDescription("Opens a menu to select your chat and/or name colors.");
	}

	@Override
	protected void onCommand() {
		checkConsole();
		new ColorSelectionMenu().displayTo(getPlayer());
	}
}
