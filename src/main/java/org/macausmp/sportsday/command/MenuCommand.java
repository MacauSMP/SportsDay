package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.menu.MenuGUI;

import java.util.ArrayList;
import java.util.List;

public class MenuCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> p.openInventory(new MenuGUI().getInventory()));
    }

    @Override
    public String name() {
        return "menu";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return new ArrayList<>();
    }
}
