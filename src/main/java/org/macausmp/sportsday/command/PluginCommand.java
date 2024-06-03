package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a plugin command.
 */
public abstract class PluginCommand implements CommandExecutor, TabCompleter {
    /**
     * Execute command.
     * @param sender command sender
     * @param args arguments
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    /**
     * Command name.
     * @return name of command
     */
    public abstract String name();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    protected void requirePlayer(@NotNull CommandSender sender, @NotNull Consumer<Player> consumer) {
        if (sender instanceof Player p)
            consumer.accept(p);
        else
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
    }
}
