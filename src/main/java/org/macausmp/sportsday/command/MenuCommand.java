package org.macausmp.sportsday.command;

import org.bukkit.command.CommandSender;
import org.macausmp.sportsday.gui.menu.MenuGUI;

public class MenuCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> p.openInventory(new MenuGUI().getInventory()));
    }

    @Override
    public String name() {
        return "menu";
    }
}
