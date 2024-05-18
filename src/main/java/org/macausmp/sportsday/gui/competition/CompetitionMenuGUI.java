package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;

public class CompetitionMenuGUI extends AbstractCompetitionGUI {
    public CompetitionMenuGUI() {
        super(27, Component.translatable("gui.menu.title"));
        getInventory().setItem(10, COMPETITION_CONSOLE);
        getInventory().setItem(12, CONTESTANTS_LIST);
        getInventory().setItem(14, COMPETITION_SETTINGS);
        getInventory().setItem(16, VERSION);
    }
}
