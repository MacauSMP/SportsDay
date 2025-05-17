package org.macausmp.sportsday.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public class VectorDataType implements PersistentDataType<PersistentDataContainer, Vector> {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final VectorDataType VECTOR_DATA_TYPE = new VectorDataType();

    private VectorDataType() {}

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Vector> getComplexType() {
        return Vector.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull Vector complex, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(new NamespacedKey(PLUGIN, "x"), DOUBLE, complex.getX());
        container.set(new NamespacedKey(PLUGIN, "y"), DOUBLE, complex.getY());
        container.set(new NamespacedKey(PLUGIN, "z"), DOUBLE, complex.getZ());
        return container;
    }

    @Override
    public @NotNull Vector fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
        double x = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "x"), DOUBLE));
        double y = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "y"), DOUBLE));
        double z = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "z"), DOUBLE));
        return new Vector(x, y, z);
    }
}
