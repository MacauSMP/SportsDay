package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PingCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length >= 1) {
            Player p = Bukkit.getPlayerExact(args[0]);
            sender.sendMessage(p == null ? Component.translatable("argument.player.unknown").color(NamedTextColor.RED) : Component.translatable("%s's ping is %sms").args(p.displayName(), Component.text(p.getPing())));
        } else {
            requirePlayer(sender, p -> p.sendMessage("Your ping is %sms".formatted(p.getPing())));
        }
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return args.length == 1 ? null : new ArrayList<>();
    }
}
