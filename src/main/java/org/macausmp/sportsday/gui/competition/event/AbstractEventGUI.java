package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEventGUI<T extends IEvent> extends AbstractCompetitionGUI {
    protected static final Set<AbstractEventGUI<? extends IEvent>> HANDLER = new HashSet<>();
    protected final T event;

    public AbstractEventGUI(int size, Component title, T event) {
        super(size, title);
        this.event = event;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.addWrapper(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
    }

    public static void updateGUI() {
        HANDLER.forEach(AbstractEventGUI::update);
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }
}
