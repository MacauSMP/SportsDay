package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a plugin command
 */
public interface IPluginCommand extends CommandExecutor, TabCompleter {
    /**
     * Execute command
     * @param sender command sender
     * @param args arguments
     */
    void onCommand(CommandSender sender, String[] args);

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    /**
     * Command name
     * @return name of command
     */
    String name();
}
