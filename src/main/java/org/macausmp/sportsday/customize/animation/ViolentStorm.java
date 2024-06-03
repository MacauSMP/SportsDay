package org.macausmp.sportsday.customize.animation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ViolentStorm extends Animation {
    private final Player player;
    private Location location;
    private final World world;
    private final Random random = new Random();

    public ViolentStorm(@NotNull Player player) {
        this.player = player;
        this.location = player.getLocation();
        this.world = player.getWorld();
        Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerWeather(WeatherType.DOWNFALL));
    }

    @Override
    public void run() {
        if (player.isOnline())
            location = player.getLocation();
        for (int i = 0; i < 10; i++) {
            Location loc = location.clone();
            loc.add(random.nextInt(256) - 128, 0, random.nextInt(256) - 128);
            world.strikeLightningEffect(loc.toHighestLocation());
        }
    }

    @Override
    public void stop() {
        Bukkit.getOnlinePlayers().forEach(Player::resetPlayerWeather);
    }
}
