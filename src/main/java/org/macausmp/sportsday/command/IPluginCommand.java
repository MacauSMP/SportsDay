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
public interface IPluginCommand extends CommandExecutor, TabCompleter {
    /**
     * Execute command.
     * @param sender command sender
     * @param args arguments
     */
    void onCommand(CommandSender sender, String[] args);

    /**
     * Command name.
     * @return name of command
     */
    String name();

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    @Override
    @Nullable
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    default void requirePlayer(@NotNull CommandSender sender, @NotNull Consumer<Player> consumer) {
        if (sender instanceof Player p)
            consumer.accept(p);
        else
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
    }
}
