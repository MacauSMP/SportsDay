package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.gui.GUIButton;

public class CompetitionMenuGUI extends AbstractCompetitionGUI {
    public CompetitionMenuGUI() {
        super(27, Component.translatable("gui.menu.title"));
        getInventory().setItem(10, GUIButton.COMPETITION_INFO);
        getInventory().setItem(12, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(14, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(16, GUIButton.VERSION);
    }
}
