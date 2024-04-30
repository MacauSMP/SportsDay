package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.CustomizeGraffitiSpray;
import org.macausmp.sportsday.customize.CustomizeMusickit;
import org.macausmp.sportsday.customize.CustomizeParticleEffect;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomizeMenuGUI extends PluginGUI {
    private final Player player;

    public CustomizeMenuGUI(Player player) {
        super(45, Component.translatable("gui.customize.title"));
        this.player = player;
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(10, clothing());
        getInventory().setItem(12, boatType());
        getInventory().setItem(14, weaponSkin());
        getInventory().setItem(16, projectileTrail());
        getInventory().setItem(28, walkingEffect());
        getInventory().setItem(31, graffitiSpray());
        getInventory().setItem(34, musickit());
    }

    @ButtonHandler
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        switch (e.getSlot()) {
            case 10 -> p.openInventory(new ClothingCustomizeGUI(p).getInventory());
            case 12 -> p.openInventory(new BoatTypeGUI(p).getInventory());
            case 14 -> p.openInventory(new WeaponSkinGUI(p).getInventory());
            case 16 -> p.openInventory(new ProjectileTrailGUI(p).getInventory());
            case 28 -> p.openInventory(new WalkingEffectGUI(p).getInventory());
            case 31 -> p.openInventory(new GraffitiSprayGUI(p).getInventory());
            case 34 -> p.openInventory(new MusickitGUI(p).getInventory());
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack clothing() {
        ItemStack stack = customize(Material.LEATHER_CHESTPLATE, "clothing");
        Material head = getType(PlayerCustomize.getCloth(player, EquipmentSlot.HEAD));
        Material chest = getType(PlayerCustomize.getCloth(player, EquipmentSlot.CHEST));
        Material legs = getType(PlayerCustomize.getCloth(player, EquipmentSlot.LEGS));
        Material feet = getType(PlayerCustomize.getCloth(player, EquipmentSlot.FEET));
        if (head != null || chest != null || legs != null || feet != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(Component.text())));
            if (head != null) lore.add(getTypeName(head));
            if (chest != null) lore.add(getTypeName(chest));
            if (legs != null) lore.add(getTypeName(legs));
            if (feet != null) lore.add(getTypeName(feet));
            stack.lore(lore);
        }
        return stack;
    }

    private Material getType(PlayerCustomize.Cloth cloth) {
        return cloth != null ? cloth.getMaterial() : null;
    }

    private @NotNull ItemStack boatType() {
        ItemStack stack = customize(Material.OAK_BOAT, "boat_type");
        Boat.Type type = PlayerCustomize.getBoatType(player);
        if (type == null) type = Boat.Type.OAK;
        List<Component> lore = new ArrayList<>();
        Component c = getTypeName(Objects.requireNonNull(Material.getMaterial(type.name() + "_BOAT")));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(Component.text())).append(c));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack weaponSkin() {
        ItemStack stack = customize(Material.BONE, "weapon_skin");
        Material type = PlayerCustomize.getWeaponSkin(player);
        if (type == null) type = Material.BLAZE_ROD;
        List<Component> lore = new ArrayList<>();
        Component c = getTypeName(type);
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(Component.text())).append(c));
        stack.lore(lore);
        return stack;
    }

    private @NotNull Component getTypeName(@NotNull Material type) {
        return Component.translatable(type.translationKey()).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
    }

    private @NotNull ItemStack projectileTrail() {
        ItemStack stack = customize(Material.ARROW, "projectile_trail");
        CustomizeParticleEffect effect = PlayerCustomize.getProjectileTrail(player);
        List<Component> lore = new ArrayList<>();
        Component c = effect != null ? effect.getName() : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack walkingEffect() {
        ItemStack stack = customize(Material.NETHER_STAR, "walking_effect");
        CustomizeParticleEffect effect = PlayerCustomize.getWalkingEffect(player);
        List<Component> lore = new ArrayList<>();
        Component c = effect != null ? effect.getName() : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack graffitiSpray() {
        ItemStack stack = customize(Material.PAINTING, "graffiti_spray");
        CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(player);
        List<Component> lore = new ArrayList<>();
        Component c = graffiti != null ? graffiti.getName() : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack musickit() {
        ItemStack stack = customize(Material.JUKEBOX, "musickit");
        CustomizeMusickit musickit = PlayerCustomize.getMusickit(player);
        List<Component> lore = new ArrayList<>();
        Component c = musickit != null ? musickit.getName() : TextUtil.text(Component.translatable("gui.text.none"));
        lore.add(TextUtil.text(Component.translatable("gui.customize.selected").args(c)));
        stack.lore(lore);
        return stack;
    }

    private @NotNull ItemStack customize(Material material, String customize) {
        return ItemUtil.item(material, "customize_" + customize, Component.translatable("gui.customize." + customize + ".title").color(NamedTextColor.YELLOW));
    }
}
