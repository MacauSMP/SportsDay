package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;

import java.util.ArrayList;
import java.util.List;

public class CustomizeCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> p.openInventory(new CustomizeMenuGUI(p).getInventory()));
    }

    @Override
    public String name() {
        return "customize";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
