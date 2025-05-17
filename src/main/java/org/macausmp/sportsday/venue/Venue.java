package org.macausmp.sportsday.venue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.LocationDataType;

import java.util.Objects;
import java.util.UUID;

public class Venue implements ComponentLike {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final PersistentDataType<PersistentDataContainer, Venue> VENUE_DATA_TYPE = new PersistentDataType<>() {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Venue> getComplexType() {
            return Venue.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Venue complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(PLUGIN, "uuid"), STRING, complex.getUUID().toString());
            container.set(new NamespacedKey(PLUGIN, "name"), STRING, complex.name);
            container.set(new NamespacedKey(PLUGIN, "item"), BYTE_ARRAY, complex.item.serializeAsBytes());
            container.set(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE, complex.location);
            return container;
        }

        @Override
        public @NotNull Venue fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            String name = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "name"), STRING));
            ItemStack item = ItemStack.deserializeBytes(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "item"), BYTE_ARRAY)));
            Location location = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE));
            Venue venue = new Venue(VenueType.VENUE, uuid, name, location);
            venue.item = item;
            return venue;
        }
    };

    private final VenueType<? extends Venue> type;
    private final UUID uuid;
    private String name;
    private ItemStack item;
    private Location location;

    public Venue(@NotNull VenueType<? extends Venue> type, @NotNull UUID uuid, @NotNull String name, @NotNull Location location){
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.item = new ItemStack(Material.MAP);
        this.location = location;
    }

    public VenueType<? extends Venue> getType() {
        return type;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.text(name);
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }
}
