package org.macausmp.sportsday.training;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class SportsTrainingHandler implements Listener {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Map<UUID, Sport> TRAINING_SPORTS_MAP = new HashMap<>();
    protected final Sport sport;
    private final Map<UUID, Venue> trainingMap = new HashMap<>();

    public SportsTrainingHandler(Sport sport) {
        this.sport = sport;
    }

    public abstract void equip(@NotNull Player player);

    public void joinTraining(@NotNull Player player, @NotNull Venue venue) {
        UUID uuid = player.getUniqueId();
        if (TRAINING_SPORTS_MAP.containsKey(uuid))
            TRAINING_SPORTS_MAP.get(uuid).getTrainingHandler().leaveTraining(uuid);
        TRAINING_SPORTS_MAP.put(uuid, sport);
        trainingMap.put(uuid, venue);
        player.clearActivePotionEffects();
        player.getInventory().clear();
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
        PlayerCustomize.suitUp(player);
        player.getInventory().setItem(8, ItemUtil.LEAVE_TRAINING);
        player.teleport(venue.getLocation());
        player.setRespawnLocation(venue.getLocation(), true);
        equip(player);
        player.sendMessage(Component.translatable("training.teleported").arguments(sport, venue));
        player.playSound(Sound.sound(Key.key("minecraft:entity.bat.takeoff"), Sound.Source.MASTER, 1f, 1f));
    }

    public void leaveTraining(@NotNull UUID uuid) {
        trainingMap.remove(uuid);
        TRAINING_SPORTS_MAP.remove(uuid);
        Player player = PLUGIN.getServer().getPlayer(uuid);
        if (player != null) {
            player.clearActivePotionEffects();
            player.setFireTicks(0);
            player.setFreezeTicks(0);
            player.getInventory().clear();
            PlayerCustomize.suitUp(player);
            player.getInventory().setItem(0, ItemUtil.MENU);
            player.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            player.setRespawnLocation(player.getWorld().getSpawnLocation(), true);
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    public boolean isTraining(@NotNull Player player) {
        return trainingMap.containsKey(player.getUniqueId());
    }

    public Venue getTrainingVenue(@NotNull Player player) {
        return trainingMap.get(player.getUniqueId());
    }

    public void leaveAllTraining() {
        trainingMap.forEach((uuid, venue) -> leaveTraining(uuid));
    }

    public static Sport getTrainingSport(UUID uuid) {
        return TRAINING_SPORTS_MAP.get(uuid);
    }

    public static void leaveSportsTraining(UUID uuid) {
        Sport sports = SportsTrainingHandler.getTrainingSport(uuid);
        if (sports != null)
            sports.getTrainingHandler().leaveTraining(uuid);
    }

    public static void leaveAllSportsTraining() {
        SportsRegistry.SPORT.forEach(sports -> sports.getTrainingHandler().leaveAllTraining());
    }

    @EventHandler
    public void onLeavePractice(@NotNull PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isTraining(p))
            return;
        ItemStack item = e.getItem();
        if (item == null)
            return;
        if (!ItemUtil.equals(item, ItemUtil.LEAVE_TRAINING))
            return;
        SportsTrainingHandler.leaveSportsTraining(p.getUniqueId());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }
}
