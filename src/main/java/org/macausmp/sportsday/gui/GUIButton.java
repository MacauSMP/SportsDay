package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.util.SkullTextureUtil;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIButton {
    public static final ItemStack COMPETITION_INFO = competitionInfo();
    public static final ItemStack COMPETITION_INFO_SELECTED = addEffect(competitionInfo());
    public static final ItemStack PLAYER_LIST = playerlist();
    public static final ItemStack PLAYER_LIST_SELECTED = addEffect(playerlist());
    public static final ItemStack START_COMPETITION = startCompetition();
    public static final ItemStack START_COMPETITION_SELECTED = addEffect(startCompetition());
    public static final ItemStack END_COMPETITION = endCompetition();
    public static final ItemStack COMPETITION_SETTINGS = competitionSettings();
    public static final ItemStack COMPETITION_SETTINGS_SELECTED = addEffect(competitionSettings());
    public static final ItemStack VERSION = version();
    public static final ItemStack NEXT_PAGE = nextPage();
    public static final ItemStack PREVIOUS_PAGE = previousPage();
    public static final ItemStack ELYTRA_RACING = elytraRacing();
    public static final ItemStack ICE_BOAT_RACING = iceBoatRacing();
    public static final ItemStack JAVELIN_THROW = javelinThrow();
    public static final ItemStack OBSTACLE_COURSE = obstacleCourse();
    public static final ItemStack PARKOUR = parkour();
    public static final ItemStack SUMO = sumo();
    public static final ItemStack BOARD = board();

    public static @NotNull ItemStack competitionInfo() {
        ItemStack stack = new ItemStack(Material.GOLD_BLOCK);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.title.info").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.title.info_lore"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition_info");
        });
        return stack;
    }

    public static @NotNull ItemStack playerlist() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.title.player_list").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.title.player_list_lore"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "player_list");
        });
        return stack;
    }

    public static @NotNull ItemStack startCompetition() {
        @SuppressWarnings("SpellCheckingInspection") ItemStack stack = SkullTextureUtil.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0=");
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.title.start").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.title.start_lore"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "start_competitions");
        });
        return stack;
    }

    public static @NotNull ItemStack endCompetition() {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.title.end").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.title.end_lore"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "end_competition");
        });
        return stack;
    }

    public static @NotNull ItemStack competitionSettings() {
        ItemStack stack = new ItemStack(Material.REPEATER);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.title.settings").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.title.settings_lore"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition_settings");
        });
        return stack;
    }

    public static @NotNull ItemStack version() {
        ItemStack version = new ItemStack(Material.OAK_SIGN);
        version.editMeta(meta -> {
            //noinspection deprecation
            meta.displayName(Translation.translatable("gui.plugin_version").args(Component.text(SportsDay.getInstance().getDescription().getVersion())).decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "version");
        });
        return version;
    }

    public static @NotNull ItemStack nextPage() {
        ItemStack version = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        version.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.page.next"));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "next_page");
        });
        return version;
    }

    public static @NotNull ItemStack previousPage() {
        ItemStack version = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        version.editMeta(meta -> {
            meta.displayName(Translation.translatable("gui.page.prev"));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "prev_page");
        });
        return version;
    }

    public static @NotNull ItemStack elytraRacing() {
        ItemStack stack = new ItemStack(Material.ELYTRA);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ELYTRA_RACING.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.ELYTRA_RACING.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack iceBoatRacing() {
        ItemStack stack = new ItemStack(Material.OAK_BOAT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ICE_BOAT_RACING.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.ICE_BOAT_RACING.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack javelinThrow() {
        ItemStack stack = new ItemStack(Material.TRIDENT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.JAVELIN_THROW.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.JAVELIN_THROW.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack obstacleCourse() {
        ItemStack stack = new ItemStack(Material.OAK_FENCE_GATE);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.OBSTACLE_COURSE.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.OBSTACLE_COURSE.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack parkour() {
        ItemStack stack = new ItemStack(Material.LEATHER_BOOTS);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.PARKOUR.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.PARKOUR.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack sumo() {
        ItemStack stack = new ItemStack(Material.COD);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.SUMO.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.SUMO.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack board() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.text("")));
        return stack;
    }

    @Contract("_ -> param1")
    public static @NotNull ItemStack addEffect(@NotNull ItemStack stack) {
        stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return stack;
    }

    public static boolean isSameButton(@NotNull ItemStack button, @NotNull ItemStack button2) {
        if (!button.hasItemMeta() || !button2.hasItemMeta()) return false;
        String id1 = button.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING);
        String id2 = button2.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id1, id2);
    }

    public static boolean isButton(@NotNull ItemStack button) {
        return button.hasItemMeta() && button.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING) != null;
    }
}
