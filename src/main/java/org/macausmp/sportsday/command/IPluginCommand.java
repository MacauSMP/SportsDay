package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

    /**
     * Command name
     * @return name of command
     */
    String name();

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    default void requirePlayer(@NotNull CommandSender sender, @NotNull Function<Player> function) {
        if (sender instanceof Player p) {
            function.apply(p);
        } else {
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
        }
    }

    interface Function<T> {
        void apply(T p);
    }
}
