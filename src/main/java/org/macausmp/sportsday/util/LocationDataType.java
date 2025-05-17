package org.macausmp.sportsday.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public class LocationDataType implements PersistentDataType<PersistentDataContainer, Location> {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final LocationDataType LOCATION_DATA_TYPE = new LocationDataType();

    private LocationDataType() {}

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(new NamespacedKey(PLUGIN, "world"), STRING, complex.getWorld().getName());
        container.set(new NamespacedKey(PLUGIN, "x"), DOUBLE, complex.x());
        container.set(new NamespacedKey(PLUGIN, "y"), DOUBLE, complex.y());
        container.set(new NamespacedKey(PLUGIN, "z"), DOUBLE, complex.z());
        container.set(new NamespacedKey(PLUGIN, "yaw"), FLOAT, complex.getYaw());
        container.set(new NamespacedKey(PLUGIN, "pitch"), FLOAT, complex.getPitch());
        return container;
    }

    @Override
    public @NotNull Location fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        String world = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "world"), STRING));
        double x = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "x"), DOUBLE));
        double y = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "y"), DOUBLE));
        double z = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "z"), DOUBLE));
        float yaw = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "yaw"), FLOAT));
        float pitch = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "pitch"), FLOAT));
        return new Location(Bukkit.getServer().getWorld(world), x, y, z, yaw, pitch);
    }
}
