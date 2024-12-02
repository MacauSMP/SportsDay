package org.macausmp.sportsday.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileStorage {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private final File file;
    private final PersistentDataAdapterContext context;
    private PersistentDataContainer container;

    public FileStorage(File file) {
        this.file = file;
        this.context = PLUGIN.getServer().getWorlds().getFirst().getPersistentDataContainer().getAdapterContext();
        this.container = context.newPersistentDataContainer();
    }

    public void read() {
        try {
            if (!checkExist())
                return;
            FileInputStream fis = new FileInputStream(file);
            container.readFromBytes(fis.readAllBytes());
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write() {
        try {
            checkExist();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(container.serializeToBytes());
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkExist() throws IOException {
        if (!file.exists() && file.createNewFile()) {
            PLUGIN.getSLF4JLogger().info("{} file has been created.", file.getName());
            // init file in nbt format
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(container.serializeToBytes());
            fos.close();
            return false;
        }
        return true;
    }

    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return container;
    }

    public void clear() {
        container = context.newPersistentDataContainer();
        write();
    }
}
