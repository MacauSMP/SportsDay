package org.macausmp.sportsday.gui.competition.event;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ItemUtil;

public abstract class AbstractEventGUI<T extends IEvent> extends AbstractCompetitionGUI {
    protected final T event;

    public AbstractEventGUI(int size, @NotNull T event) {
        super(size, event.getName().color(NamedTextColor.BLACK));
        this.event = event;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.setGlint(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof AbstractEventGUI)
                .map(inv -> (AbstractEventGUI<? extends IEvent>) inv.getHolder()).forEach(AbstractEventGUI::update);
    }
}
