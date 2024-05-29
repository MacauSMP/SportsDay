package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.macausmp.sportsday.competition.Competitions;

public class UnregisterCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        requirePlayer(sender, p -> {
            if (args.length == 1 && args[0].equals("confirm")) {
                if (!Competitions.leave(p))
                    sender.sendMessage(Component.translatable("competition.unregister.failed")
                            .color(NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.translatable("command.unregister.confirmation")
                        .color(NamedTextColor.RED));
            }
        });
    }

    @Override
    public String name() {
        return "unregister";
    }
}
