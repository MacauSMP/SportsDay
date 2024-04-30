package org.macausmp.sportsday.customize;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Boat;
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
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PlayerCustomize {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Map<Material, TrimMaterial> TRIM_MATERIAL = new HashMap<>();
    private static final Map<String, TrimPattern> TRIM_PATTERN = new HashMap<>();
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
        TRIM_PATTERN.put("SENTRY", TrimPattern.SENTRY);
        TRIM_PATTERN.put("DUNE", TrimPattern.DUNE);
        TRIM_PATTERN.put("COAST", TrimPattern.COAST);
        TRIM_PATTERN.put("WILD", TrimPattern.WILD);
        TRIM_PATTERN.put("WARD", TrimPattern.WARD);
        TRIM_PATTERN.put("EYE", TrimPattern.EYE);
        TRIM_PATTERN.put("VEX", TrimPattern.VEX);
        TRIM_PATTERN.put("TIDE", TrimPattern.TIDE);
        TRIM_PATTERN.put("SNOUT", TrimPattern.SNOUT);
        TRIM_PATTERN.put("RIB", TrimPattern.RIB);
        TRIM_PATTERN.put("SPIRE", TrimPattern.SPIRE);
        TRIM_PATTERN.put("WAYFINDER", TrimPattern.WAYFINDER);
        TRIM_PATTERN.put("SHAPER", TrimPattern.SHAPER);
        TRIM_PATTERN.put("SILENCE", TrimPattern.SILENCE);
        TRIM_PATTERN.put("RAISER", TrimPattern.RAISER);
        TRIM_PATTERN.put("HOST", TrimPattern.HOST);
    }

    public static void suitUp(@NotNull Player player) {
        player.getInventory().setHelmet(cloth(player, EquipmentSlot.HEAD));
        player.getInventory().setChestplate(cloth(player, EquipmentSlot.CHEST));
        player.getInventory().setLeggings(cloth(player, EquipmentSlot.LEGS));
        player.getInventory().setBoots(cloth(player, EquipmentSlot.FEET));
    }

    private static @Nullable ItemStack cloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        Cloth cloth = getCloth(player, slot);
        if (cloth == null) return null;
        ItemStack item = getClothItemStack(cloth);
        item.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        });
        return ItemUtil.setBind(item);
    }

    public static @Nullable Cloth getCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        if (!player.getPersistentDataContainer().has(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER)) return null;
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        return container.get(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE);
    }

    public static @NotNull ItemStack getClothItemStack(@NotNull Cloth cloth) {
        ItemStack item = new ItemStack(cloth.material);
        if (cloth.colorable()) item.editMeta(ColorableArmorMeta.class, meta -> meta.setColor(cloth.color));
        if (cloth.trimMaterial.equals(Cloth.NONE)) return item;
        TrimMaterial tm = TRIM_MATERIAL.get(Material.valueOf(cloth.trimMaterial));
        TrimPattern tp = TRIM_PATTERN.get(cloth.trimPattern);
        if (tm != null && tp != null) {
            item.editMeta(ArmorMeta.class, meta -> meta.setTrim(new ArmorTrim(tm, tp)));
        }
        return item;
    }

    public static void setClothMaterial(@NotNull Player player, @NotNull Material type) {
        if (!player.getPersistentDataContainer().has(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer container = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
        }
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        Cloth original = getCloth(player, type.getEquipmentSlot());
        if (original != null) {
            original.material = type;
        } else {
            original = new Cloth(type);
        }
        container.set(new NamespacedKey(PLUGIN, type.getEquipmentSlot().name().toLowerCase()), ClothDataType.INSTANCE, original);
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetCloth(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        container.remove(new NamespacedKey(PLUGIN, slot.name().toLowerCase()));
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothColor(@NotNull Player player, @NotNull EquipmentSlot slot, @Nullable Color color) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.color = color != null ? color : Bukkit.getItemFactory().getDefaultLeatherColor();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimMaterial(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = type.name();
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static void setClothTrimPattern(@NotNull Player player, @NotNull EquipmentSlot slot, @NotNull Material type) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimPattern = type.name().substring(0, type.name().length() - 29);
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static void resetClothTrim(@NotNull Player player, @NotNull EquipmentSlot slot) {
        PersistentDataContainer container = Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER));
        Cloth cloth = Objects.requireNonNull(getCloth(player, slot));
        cloth.trimMaterial = PlayerCustomize.Cloth.NONE;
        cloth.trimPattern = PlayerCustomize.Cloth.NONE;
        container.set(new NamespacedKey(PLUGIN, slot.name().toLowerCase()), ClothDataType.INSTANCE, cloth);
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "clothing"), PersistentDataType.TAG_CONTAINER, container);
    }

    public static Boat.@Nullable Type getBoatType(@NotNull Player player) {
        String type = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "boat_type"), PersistentDataType.STRING);
        return type != null ? Boat.Type.valueOf(type) : null;
    }

    public static void setBoatType(@NotNull Player player, @NotNull Material type) {
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "boat_type"), PersistentDataType.STRING, type.name().substring(0, type.name().length() - 5));
    }

    public static @Nullable Material getWeaponSkin(@NotNull Player player) {
        String type = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "weapon_skin"), PersistentDataType.STRING);
        return type != null ? Material.getMaterial(type) : null;
    }

    public static void setWeaponSkin(@NotNull Player player, @NotNull Material type) {
        player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "weapon_skin"), PersistentDataType.STRING, type.name());
    }

    public static @Nullable CustomizeParticleEffect getProjectileTrail(@NotNull Player player) {
        String effect = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "projectile_trail"), PersistentDataType.STRING);
        return effect != null ? CustomizeParticleEffect.valueOf(effect) : null;
    }

    public static void setProjectileTrail(@NotNull Player player, CustomizeParticleEffect effect) {
        if (effect != null) {
            player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "projectile_trail"), PersistentDataType.STRING, effect.name());
        } else {
            player.getPersistentDataContainer().remove(new NamespacedKey(PLUGIN, "projectile_trail"));
        }
    }

    public static @Nullable CustomizeParticleEffect getWalkingEffect(@NotNull Player player) {
        String effect = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "walking_effect"), PersistentDataType.STRING);
        return effect != null ? CustomizeParticleEffect.valueOf(effect) : null;
    }

    public static void setWalkingEffect(@NotNull Player player, CustomizeParticleEffect effect) {
        if (effect != null) {
            player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "walking_effect"), PersistentDataType.STRING, effect.name());
        } else {
            player.getPersistentDataContainer().remove(new NamespacedKey(PLUGIN, "walking_effect"));
        }
    }

    public static @Nullable CustomizeGraffitiSpray getGraffitiSpray(@NotNull Player player) {
        String graffiti = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "graffiti_spray"), PersistentDataType.STRING);
        return graffiti != null ? CustomizeGraffitiSpray.valueOf(graffiti) : null;
    }

    public static void setGraffitiSpray(@NotNull Player player, CustomizeGraffitiSpray graffiti) {
        if (graffiti != null) {
            player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "graffiti_spray"), PersistentDataType.STRING, graffiti.name());
        } else {
            player.getPersistentDataContainer().remove(new NamespacedKey(PLUGIN, "graffiti_spray"));
        }
    }

    public static @Nullable CustomizeMusickit getMusickit(@NotNull Player player) {
        String musickit = player.getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "musickit"), PersistentDataType.STRING);
        return musickit != null ? CustomizeMusickit.valueOf(musickit) : null;
    }

    public static void setMusickit(@NotNull Player player, CustomizeMusickit musickit) {
        if (musickit != null) {
            player.getPersistentDataContainer().set(new NamespacedKey(PLUGIN, "musickit"), PersistentDataType.STRING, musickit.name());
        } else {
            player.getPersistentDataContainer().remove(new NamespacedKey(PLUGIN, "musickit"));
        }
    }

    public static class Cloth {
        public static final String NONE = "none";
        private Material material;
        private String trimMaterial = NONE;
        private String trimPattern = NONE;
        private Color color = Bukkit.getItemFactory().getDefaultLeatherColor();

        private Cloth(Material material) {
            this.material = material;
        }

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
        public static final ClothDataType INSTANCE = new ClothDataType();

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
            container.set(new NamespacedKey(PLUGIN, "material"), PersistentDataType.STRING, complex.material.name());
            container.set(new NamespacedKey(PLUGIN, "trim-material"), PersistentDataType.STRING, complex.trimMaterial);
            container.set(new NamespacedKey(PLUGIN, "trim-pattern"), PersistentDataType.STRING, complex.trimPattern);
            if (complex.colorable()) container.set(new NamespacedKey(PLUGIN, "color"), PersistentDataType.INTEGER, complex.color.asARGB());
            return container;
        }

        @Override
        public @NotNull Cloth fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            Cloth cloth = new Cloth(Material.valueOf(primitive.get(new NamespacedKey(PLUGIN, "material"), PersistentDataType.STRING)));
            cloth.trimMaterial = primitive.get(new NamespacedKey(PLUGIN, "trim-material"), PersistentDataType.STRING);
            cloth.trimPattern = primitive.get(new NamespacedKey(PLUGIN, "trim-pattern"), PersistentDataType.STRING);
            Integer color = primitive.get(new NamespacedKey(PLUGIN, "color"), PersistentDataType.INTEGER);
            if (color != null) cloth.color = Color.fromARGB(color);
            return cloth;
        }
    }
}
