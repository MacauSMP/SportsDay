package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.competition.Competitions;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            if (!Competitions.isCompetitor(p)) {
                int number;
                if (args.length >= 1) {
                    try {
                        number = Integer.parseInt(args[0]);
                        if (number < 0) {
                            sender.sendMessage(Component.translatable("argument.number.negative").color(NamedTextColor.RED));
                            return;
                        }
                    } catch (Exception e) {
                        sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[0])).color(NamedTextColor.RED));
                        return;
                    }
                } else {
                    number = Competitions.genNumber();
                }
                if (!Competitions.join(p, number)) sender.sendMessage(Component.translatable("command.competition.register_number_occupied").args(Component.text(number)).color(NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.translatable("command.competition.register.failed.self").args(p.displayName()).color(NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
        }
    }

    @Override
    public String name() {
        return "register";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
