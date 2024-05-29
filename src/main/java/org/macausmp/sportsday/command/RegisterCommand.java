package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.macausmp.sportsday.competition.Competitions;

public class RegisterCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> {
            if (!Competitions.isContestant(p)) {
                int number;
                if (args.length >= 1) {
                    try {
                        number = Integer.parseInt(args[0]);
                        if (number < 0) {
                            sender.sendMessage(Component.translatable("argument.number.negative")
                                    .color(NamedTextColor.RED));
                            return;
                        }
                    } catch (Exception e) {
                        sender.sendMessage(Component.translatable("parsing.int.invalid")
                                .arguments(Component.text(args[0])).color(NamedTextColor.RED));
                        return;
                    }
                } else {
                    number = Competitions.genNumber();
                }
                if (!Competitions.join(p, number))
                    sender.sendMessage(Component.translatable("competition.register.failed.number_occupied")
                            .arguments(Component.text(number)).color(NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.translatable("competition.register.failed")
                        .arguments(p.displayName()).color(NamedTextColor.RED));
            }
        });
    }

    @Override
    public String name() {
        return "register";
    }
}
