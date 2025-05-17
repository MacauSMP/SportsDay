package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class SportsDayCommand extends PluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> {
            if (args.length != 0 && args[0].equals("book")) {
                p.getInventory().addItem(ItemUtil.OP_BOOK);
                return;
            }
            p.openInventory(new AdminMenuGUI().getInventory());
        });
    }

    @Override
    public String name() {
        return "sportsday";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1)
            list.add("book");
        return list;
    }
}
