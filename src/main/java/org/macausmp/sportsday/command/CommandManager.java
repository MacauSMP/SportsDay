package org.macausmp.sportsday.command;

import org.macausmp.sportsday.SportsDay;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class CommandManager {
    private final SportsDay plugin = SportsDay.getInstance();
    private static final Set<IPluginCommand> COMMANDS = new HashSet<>();
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
            Objects.requireNonNull(plugin.getCommand(command.name())).setExecutor(command);
            Objects.requireNonNull(plugin.getCommand(command.name())).setTabCompleter(command);
        }
    }
}
