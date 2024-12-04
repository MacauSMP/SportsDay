package org.macausmp.sportsday.customize;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.*;
import java.util.function.Function;

public final class PlayerCustomize {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Map<Material, TrimMaterial> TRIM_MATERIAL = new LinkedHashMap<>();
    private static final Map<String, TrimPattern> TRIM_PATTERN = new LinkedHashMap<>();
    private static final NamespacedKey CLOTHING = new NamespacedKey(PLUGIN, "clothing");
    private static final NamespacedKey BOAT_TYPE = new NamespacedKey(PLUGIN, "boat_type");
    private static final NamespacedKey WEAPON_SKIN = new NamespacedKey(PLUGIN, "weapon_skin");
    private static final NamespacedKey VICTORY_DANCE = new NamespacedKey(PLUGIN, "victory_dance");
    private static final NamespacedKey PROJECTILE_TRAIL = new NamespacedKey(PLUGIN, "projectile_trail");
    private static final NamespacedKey WALKING_EFFECT = new NamespacedKey(PLUGIN, "walking_effect");
    private static final NamespacedKey GRAFFITI_SPRAY = new NamespacedKey(PLUGIN, "graffiti_spray");
    private static final NamespacedKey MUSICKIT = new NamespacedKey(PLUGIN, "musickit");
    static {
        TRIM_MATERIAL.put(Material.QUARTZ, TrimMaterial.QUARTZ);
        TRIM_MATERIAL.put(Material.IRON_INGOT, TrimMaterial.IRON);
        TRIM_MATERIAL.put(Material.NETHERITE_INGOT, TrimMaterial.NETHERITE);
        TRIM_MATERIAL.put(Material.REDSTONE, TrimMaterial.REDSTONE);
        TRIM_MATERIAL.put(Material.COPPER_INGOT, TrimMaterial.COPPER);
        TRIM_MATERIAL.put(Material.GOLD_INGOT, TrimMaterial.GOLD);
        TRIM_MATERIAL.put(Material.EMERALD, TrimMaterial.EMERALD);
        TRIM_MATERIAL.put(Material.DIAMOND, TrimMaterial.DIAMOND);
        TRIM_MATERIAL.put(Material.LAPIS_LAZULI, TrimMaterial.LAPIS);
        TRIM_MATERIAL.put(Material.AMETHYST_SHARD, TrimMaterial.AMETHYST);
        final Registry<TrimPattern> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
        registry.forEach(pattern -> TRIM_PATTERN.put(Objects.requireNonNull(registry.getKey(pattern)).value().toUpperCase(), pattern));
    }

    public static @Unmodifiable @NotNull List<Material> getTrimMaterial() {
        return List.copyOf(TRIM_MATERIAL.keySet());
    }

    public static @Unmodifiable List<Material> getTrimPattern() {
        return TRIM_PATTERN.keySet().stream()
                .map(k -> Objects.requireNonNull(Material.getMaterial(k + "_ARMOR_TRIM_SMITHING_TEMPLATE")))
                .toList();
    }

    public static void suitUp(@NotNull Player player) {
        player.getInventory().setHelmet(cloth(player, EquipmentSlot.HEAD));
        player.getInventory().setChestplate(cloth(player, EquipmentSlot.CHEST));
        player.getInventory().setLeggings(cloth(player, EquipmentSlot.LEGS));
        player.getInventory().setBoots(cloth(player, EquipmentSlot.FEET));
    }

    private static @Nullable ItemStack cloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        Cloth cloth = getCloth(player, slot);
        return cloth != null ? ItemUtil.setBind(getClothItemStack(cloth)) : null;
    }

    public static @Nullable Cloth getCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        if (!player.getPersistentDataContainer().has(CLOTHING, PersistentDataType.TAG_CONTAINER))
            return null;
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        return container.get(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE);
    }

    public static @NotNull ItemStack getClothItemStack(@NotNull Cloth cloth) {
        ItemStack item = new ItemStack(cloth.material);
        item.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        });
        if (cloth.colorable())
            item.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(cloth.color));
        if (cloth.trimMaterial.equals(Cloth.NONE))
            return item;
        TrimMaterial tm = TRIM_MATERIAL.get(Material.getMaterial(cloth.trimMaterial));
        TrimPattern tp = TRIM_PATTERN.get(cloth.trimPattern);
        if (tm != null && tp != null)
            item.editMeta(ArmorMeta.class, meta -> meta.setTrim(new ArmorTrim(tm, tp)));
        return item;
    }

    public static void setClothMaterial(@NotNull Player player, @NotNull Material type) {
        if (!player.getPersistentDataContainer().has(CLOTHING, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer container = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
        }
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth original = getCloth(player, type.getEquipmentSlot());
        if (original == null)
            original = new Cloth();
        original.material = type;
        container.set(new NamespacedKey(PLUGIN, type.getEquipmentSlot().name().toLowerCase()), ClothDataType.INSTANCE, original);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        container.remove(new NamespacedKey(PLUGIN, slot.name().toLowerCase()));
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothColor(@NotNull Player player, @NotNull EquipmentSlot slot, @Nullable Color color) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.color = color != null ? color : Bukkit.getItemFactory().getDefaultLeatherColor();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimMaterial(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = type.name();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimPattern(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimPattern = type.name().substring(0, type.name().length() - 29);
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetClothTrim(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(CLOTHING, PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = PlayerCustomize.Cloth.NONE;
        cloth.trimPattern = PlayerCustomize.Cloth.NONE;
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(CLOTHING, PersistentDataType.TAG_CONTAINER, container);
    }

    public static @NotNull EntityType getBoatType(@NotNull Player player) {
        return Optional.ofNullable(get(player.getPersistentDataContainer(), BOAT_TYPE, EntityType::valueOf)).orElse(EntityType.OAK_BOAT);
    }

    public static void setBoatType(@NotNull Player player, @NotNull Material type) {
        set(player.getPersistentDataContainer(), BOAT_TYPE, type.name());
    }

    public static @NotNull Material getWeaponSkin(@NotNull Player player) {
        return Optional.ofNullable(get(player.getPersistentDataContainer(), WEAPON_SKIN, Material::getMaterial)).orElse(Material.BLAZE_ROD);
    }

    public static void setWeaponSkin(@NotNull Player player, @NotNull Material type) {
        set(player.getPersistentDataContainer(), WEAPON_SKIN, type.name());
    }

    public static @Nullable VictoryDance getVictoryDance(@NotNull Player player) {
        return get(player.getPersistentDataContainer(), VICTORY_DANCE, VictoryDance::valueOf);
    }

    public static void setVictoryDance(@NotNull Player player, VictoryDance victoryDance) {
        set(player.getPersistentDataContainer(), VICTORY_DANCE, victoryDance != null ? victoryDance.name() : null);
    }

    public static @Nullable ParticleEffect getProjectileTrail(@NotNull Player player) {
        return get(player.getPersistentDataContainer(), PROJECTILE_TRAIL, ParticleEffect::valueOf);
    }

    public static void setProjectileTrail(@NotNull Player player, ParticleEffect effect) {
        set(player.getPersistentDataContainer(), PROJECTILE_TRAIL, effect != null ? effect.name() : null);
    }

    public static @Nullable ParticleEffect getWalkingEffect(@NotNull Player player) {
        return get(player.getPersistentDataContainer(), WALKING_EFFECT, ParticleEffect::valueOf);
    }

    public static void setWalkingEffect(@NotNull Player player, ParticleEffect effect) {
        set(player.getPersistentDataContainer(), WALKING_EFFECT, effect != null ? effect.name() : null);
    }

    public static @Nullable GraffitiSpray getGraffitiSpray(@NotNull Player player) {
        return get(player.getPersistentDataContainer(), GRAFFITI_SPRAY, GraffitiSpray::valueOf);
    }

    public static void setGraffitiSpray(@NotNull Player player, GraffitiSpray graffiti) {
        set(player.getPersistentDataContainer(), GRAFFITI_SPRAY, graffiti != null ? graffiti.name() : null);
    }

    public static @Nullable Musickit getMusickit(@NotNull Player player) {
        return get(player.getPersistentDataContainer(), MUSICKIT, Musickit::valueOf);
    }

    public static void setMusickit(@NotNull Player player, Musickit musickit) {
        set(player.getPersistentDataContainer(), MUSICKIT, musickit != null ? musickit.name() : null);
    }

    private static <T> @Nullable T get(@NotNull PersistentDataContainer pdc, @NotNull NamespacedKey key, @NotNull Function<String, T> function) {
        String value = pdc.get(key, PersistentDataType.STRING);
        if (value == null)
            return null;
        try {
            return function.apply(value);
        } catch (IllegalArgumentException e) {
            pdc.remove(key);
            return null;
        }
    }

    private static void set(@NotNull PersistentDataContainer pdc, @NotNull NamespacedKey key, @Nullable String value) {
        if (value != null)
            pdc.set(key, PersistentDataType.STRING, value);
        else
            pdc.remove(key);
    }

    public static class Cloth {
        public static final String NONE = "none";
        private Material material;
        private String trimMaterial = NONE;
        private String trimPattern = NONE;
        private Color color = Bukkit.getItemFactory().getDefaultLeatherColor();

        private Cloth() {}

        public Material getMaterial() {
            return material;
        }

        public String getTrimMaterial() {
            return trimMaterial;
        }

        public String getTrimPattern() {
            return trimPattern;
        }

        public Color getColor() {
            return color;
        }

        public boolean colorable() {
            return material != null && Bukkit.getItemFactory().getItemMeta(material) instanceof ColorableArmorMeta;
        }
    }

    private static class ClothDataType implements PersistentDataType<PersistentDataContainer, Cloth> {
        private static final ClothDataType INSTANCE = new ClothDataType();
        private static final NamespacedKey MATERIAL = new NamespacedKey(PLUGIN, "material");
        private static final NamespacedKey TRIM_MATERIAL = new NamespacedKey(PLUGIN, "trim-material");
        private static final NamespacedKey TRIM_PATTERN = new NamespacedKey(PLUGIN, "trim-pattern");
        private static final NamespacedKey COLOR = new NamespacedKey(PLUGIN, "color");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Cloth> getComplexType() {
            return Cloth.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Cloth complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(MATERIAL, PersistentDataType.STRING, complex.material.name());
            container.set(TRIM_MATERIAL, PersistentDataType.STRING, complex.trimMaterial);
            container.set(TRIM_PATTERN, PersistentDataType.STRING, complex.trimPattern);
            container.set(COLOR, PersistentDataType.INTEGER, complex.color.asARGB());
            return container;
        }

        @Override
        public @NotNull Cloth fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            Cloth cloth = new Cloth();
            cloth.material = Material.getMaterial(Objects.requireNonNull(primitive.get(MATERIAL, PersistentDataType.STRING)));
            cloth.trimMaterial = primitive.get(TRIM_MATERIAL, PersistentDataType.STRING);
            cloth.trimPattern = primitive.get(TRIM_PATTERN, PersistentDataType.STRING);
            Integer color = primitive.get(COLOR, PersistentDataType.INTEGER);
            if (color != null)
                cloth.color = Color.fromARGB(color);
            return cloth;
        }
    }
}
