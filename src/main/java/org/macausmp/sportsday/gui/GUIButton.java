package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.competition.ITrackEvent;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.SkullTextureUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public final class GUIButton {
    public static final ItemStack COMPETITION_INFO = ItemUtil.item(Material.GOLD_BLOCK, "competition_info", "gui.info.title", "gui.info.lore");
    public static final ItemStack PLAYER_LIST = ItemUtil.item(Material.PAPER, "player_list", "gui.player_list.title", "gui.player_list.lore");
    public static final ItemStack START_COMPETITION = ItemUtil.head(SkullTextureUtil.START, "start_competitions", "gui.start.title", "gui.start.lore");
    public static final ItemStack END_COMPETITION = ItemUtil.item(Material.RED_CONCRETE, "end_competition", "gui.end.title", "gui.end.lore");
    public static final ItemStack COMPETITION_SETTINGS = ItemUtil.item(Material.REPEATER, "competition_settings", "gui.settings.title", "gui.settings.lore");
    @SuppressWarnings("deprecation")
    public static final ItemStack VERSION = ItemUtil.item(Material.OAK_SIGN, "version", Component.translatable("gui.plugin_version").args(Component.text(SportsDay.getInstance().getDescription().getVersion())));
    public static final ItemStack NEXT_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "next_page", "gui.page.next");
    public static final ItemStack PREVIOUS_PAGE = ItemUtil.item(Material.BLUE_STAINED_GLASS_PANE, "prev_page", "gui.page.prev");
    public static final ItemStack BACK = ItemUtil.item(Material.ARROW, "back", Component.translatable("gui.page.back"));
    public static final ItemStack ELYTRA_RACING = event(Material.ELYTRA, Competitions.ELYTRA_RACING);
    public static final ItemStack ICE_BOAT_RACING = event(Material.OAK_BOAT, Competitions.ICE_BOAT_RACING);
    public static final ItemStack JAVELIN_THROW = event(Material.TRIDENT, Competitions.JAVELIN_THROW);
    public static final ItemStack OBSTACLE_COURSE = event(Material.OAK_FENCE_GATE, Competitions.OBSTACLE_COURSE);
    public static final ItemStack PARKOUR = event(Material.LEATHER_BOOTS, Competitions.PARKOUR);
    public static final ItemStack SUMO = event(Material.COD, Competitions.SUMO);
    public static final ItemStack GUIDEBOOK = ItemUtil.item(Material.WRITABLE_BOOK, "guidebook", Component.translatable("gui.menu.guidebook.title").color(NamedTextColor.YELLOW), "gui.menu.guidebook.lore");
    public static final ItemStack HOME = ItemUtil.item(Material.RED_BED, "home", Component.translatable("gui.menu.home.title").color(NamedTextColor.YELLOW), "gui.menu.home.lore");
    public static final ItemStack PRACTICE = ItemUtil.item(Material.ARMOR_STAND, "practice", Component.translatable("gui.menu.practice.title").color(NamedTextColor.YELLOW), "gui.menu.practice.lore");
    public static final ItemStack PERSONAL_SETTINGS = ItemUtil.item(Material.REPEATER, "personal_settings", Component.translatable("gui.menu.personal_settings.title").color(NamedTextColor.YELLOW), "gui.menu.personal_settings.lore");
    public static final ItemStack CLOTHING = customize(Material.LEATHER_CHESTPLATE, "clothing");
    public static final ItemStack BOAT_TYPE = customize(Material.OAK_BOAT, "boat_type");
    public static final ItemStack WEAPON_SKIN = customize(Material.BONE, "weapon_skin");
    public static final ItemStack MUSICKIT = customize(Material.JUKEBOX, "musickit");
    public static final ItemStack PROJECTILE_TRAIL = customize(Material.ARROW, "projectile_trail");
    public static final ItemStack WALKING_EFFECT = customize(Material.NETHER_STAR, "walking_effect");
    public static final ItemStack GRAFFITI_SPRAY = customize(Material.PAINTING, "graffiti_spray");
    public static final ItemStack BOARD = ItemUtil.item(Material.BLACK_STAINED_GLASS_PANE, null, "");

    private static @NotNull ItemStack event(Material material, IEvent event) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(event.getName());
            if (event instanceof ITrackEvent e) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("event.track.laps").args(Component.text((e.getMaxLaps()))).color(NamedTextColor.YELLOW)));
                meta.lore(lore);
            }
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(ItemUtil.COMPETITION_ID, PersistentDataType.STRING, event.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack customize(Material material, String customize) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize." + customize + ".title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "customize_" + customize);
        });
        return stack;
    }
}
