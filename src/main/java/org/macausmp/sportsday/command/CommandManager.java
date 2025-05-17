package org.macausmp.sportsday.command;

import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public final class CommandManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();

    public void register() {
        register(new SportsDayCommand());
        register(new SportingEventCommand());
        register(new ContestantCommand());
        register(new MenuCommand());
        register(new RegisterCommand());
        register(new UnregisterCommand());
        register(new CustomizeCommand());
        register(new PingCommand());
    }

    private void register(@NotNull PluginCommand command) {
        Objects.requireNonNull(PLUGIN.getCommand(command.name())).setExecutor(command);
        Objects.requireNonNull(PLUGIN.getCommand(command.name())).setTabCompleter(command);
    }
}
