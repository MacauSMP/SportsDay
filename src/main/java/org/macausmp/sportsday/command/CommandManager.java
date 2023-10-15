package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandManager implements CommandExecutor {
    private final SportsDay plugin = SportsDay.getInstance();
    private static final List<IPluginCommand> COMMANDS = new ArrayList<>();
    private static final IPluginCommand COMPETITION = new CompetitionCommand();
    private static final IPluginCommand COMPETITIONGUI = new CompetitionGUICommand();
    private static final IPluginCommand REGISTER = new RegisterCommand();
    private static final IPluginCommand UNREGISTER = new UnregisterCommand();
    private static final IPluginCommand MENU = new MenuCommand();
    private static final IPluginCommand CUSTOMIZE = new CustomizeCommand();
    private static final IPluginCommand PING = new PingCommand();

    public void register() {
        COMMANDS.add(COMPETITION);
        COMMANDS.add(COMPETITIONGUI);
        COMMANDS.add(REGISTER);
        COMMANDS.add(UNREGISTER);
        COMMANDS.add(MENU);
        COMMANDS.add(CUSTOMIZE);
        COMMANDS.add(PING);
        for (IPluginCommand command : COMMANDS) {
            Objects.requireNonNull(plugin.getCommand(command.name())).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand(command.name())).setTabCompleter(command);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (IPluginCommand pc : COMMANDS) {
            if (pc.name().equals(command.getName())) {
                pc.onCommand(sender, args);
                return true;
            }
        }
        return false;
    }
}
