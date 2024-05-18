package org.macausmp.sportsday.command;

import org.bukkit.command.CommandSender;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;

public class CustomizeCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> p.openInventory(new CustomizeMenuGUI(p).getInventory()));
    }

    @Override
    public String name() {
        return "customize";
    }
}
