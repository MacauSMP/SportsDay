package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Config files for storing players' plugin data
 */
public final class ConfigManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private File competitorFile;
    private FileConfiguration competitorConfig;

    void setup() {
        if (!PLUGIN.getDataFolder().exists() && PLUGIN.getDataFolder().mkdir()) PLUGIN.getLogger().log(Level.INFO, "Data folder created");
        competitorFile = new File(PLUGIN.getDataFolder(), "competitor.yml");
        if (!competitorFile.exists()) {
            try {
                if (competitorFile.createNewFile()) PLUGIN.getLogger().log(Level.INFO, "competitor.yml file has been created");
            } catch (IOException e) {
                PLUGIN.getLogger().log(Level.SEVERE, "Could not create the competitor.yml file");
            }
        }
        competitorConfig = YamlConfiguration.loadConfiguration(competitorFile);
    }

    /**
     * Get the config that store competitors' data
     * @return competitors data file
     */
    public FileConfiguration getCompetitorConfig() {
        return competitorConfig;
    }

    public void saveConfig() {
        try {
            competitorConfig.save(competitorFile);
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not save the competitor.yml file", e);
        }
    }
}
