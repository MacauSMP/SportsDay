package org.macausmp.sportsday.competition;

import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public interface Savable {
    void load(@NotNull PersistentDataContainer data);

    void save(@NotNull PersistentDataContainer data);
}
