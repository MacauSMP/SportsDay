package org.macausmp.sportsday.venue;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.LocationDataType;

import java.util.Objects;
import java.util.UUID;

public class CombatVenue extends Venue {
    public static final PersistentDataType<PersistentDataContainer, CombatVenue> COMBAT_VENUE_DATA_TYPE = new PersistentDataType<>() {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<CombatVenue> getComplexType() {
            return CombatVenue.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull CombatVenue complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(PLUGIN, "uuid"), STRING, complex.getUUID().toString());
            container.set(new NamespacedKey(PLUGIN, "name"), STRING, complex.getName());
            container.set(new NamespacedKey(PLUGIN, "item"), BYTE_ARRAY, complex.getItem().serializeAsBytes());
            container.set(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE, complex.getLocation());
            container.set(new NamespacedKey(PLUGIN, "p1"), LocationDataType.LOCATION_DATA_TYPE, complex.p1);
            container.set(new NamespacedKey(PLUGIN, "p2"), LocationDataType.LOCATION_DATA_TYPE, complex.p2);
            return container;
        }

        @Override
        public @NotNull CombatVenue fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "uuid"), STRING)));
            String name = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "name"), STRING));
            ItemStack item = ItemStack.deserializeBytes(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "item"), BYTE_ARRAY)));
            Location location = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "location"), LocationDataType.LOCATION_DATA_TYPE));
            CombatVenue combatVenue = new CombatVenue(VenueType.COMBAT_VENUE, uuid, name, location);
            combatVenue.setItem(item);
            combatVenue.p1 = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p1"), LocationDataType.LOCATION_DATA_TYPE));
            combatVenue.p2 = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p2"), LocationDataType.LOCATION_DATA_TYPE));
            return combatVenue;
        }
    };

    private Location p1;
    private Location p2;

    public CombatVenue(@NotNull VenueType<? extends CombatVenue> type, @NotNull UUID uuid, @NotNull String name, @NotNull Location location) {
        super(type, uuid, name, location);
        this.p1 = location;
        this.p2 = location;
    }

    public Location getP1Location() {
        return p1;
    }

    public void setP1Location(@NotNull Location p1) {
        this.p1 = p1;
    }

    public Location getP2Location() {
        return p2;
    }

    public void setP2Location(@NotNull Location p2) {
        this.p2 = p2;
    }
}
