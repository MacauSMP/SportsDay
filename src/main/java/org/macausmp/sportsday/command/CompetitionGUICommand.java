package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionGUICommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            if (args.length != 0 && args[0].equals("book")) {
                p.getInventory().addItem(ItemUtil.OP_BOOK);
                return;
            }
            p.openInventory(new CompetitionMenuGUI().getInventory());
        } else {
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
        }
    }

    @Override
    public String name() {
        return "competitiongui";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) list.add("book");
        return list;
    }
}
