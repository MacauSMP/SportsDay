package org.macausmp.sportsday.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class KeyDataType implements PersistentDataType<String, NamespacedKey> {
    public static final KeyDataType KEY_DATA_TYPE = new KeyDataType();

    private KeyDataType() {}

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<NamespacedKey> getComplexType() {
        return NamespacedKey.class;
    }

    @Override
    public @NotNull NamespacedKey fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return Objects.requireNonNull(NamespacedKey.fromString(primitive));
    }

    @Override
    public @NotNull String toPrimitive(@NotNull NamespacedKey complex, @NotNull PersistentDataAdapterContext context) {
        return complex.asString();
    }
}
