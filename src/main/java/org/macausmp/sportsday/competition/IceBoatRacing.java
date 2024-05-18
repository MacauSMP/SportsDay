package org.macausmp.sportsday.competition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class IceBoatRacing extends AbstractTrackEvent {
    private static final Material SPRING_BLOCK = getMaterial("ice_boat_racing.spring_block");
    private final HashMap<UUID, Boat> boatMap = new HashMap<>();

    public IceBoatRacing() {
        super("ice_boat_racing");
    }

    @Override
    protected void onSetup() {
        getLocation().getWorld().getEntitiesByClass(Boat.class).forEach(Boat::remove);
        getContestants().forEach(data -> boatMap.put(data.getUUID(), boat(data.getPlayer())));
    }

    @Override
    protected void onStart() {}

    @Override
    protected void onEnd(boolean force) {
        boatMap.values().forEach(Boat::remove);
    }

    @Override
    protected void onPractice(@NotNull Player player) {
        boatMap.put(player.getUniqueId(), boat(player));
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if ((check(p) && getStatus() == Status.STARTED || inPractice(p, this)) && boatMap.containsKey(p.getUniqueId()))
            bounce(p);
    }

    private void bounce(@NotNull Player p) {
        Location loc = p.getLocation().clone();
        loc.setY(loc.getY() - 0.5f);
        if (loc.getBlock().getType() == SPRING_BLOCK && !SPRING_BLOCK.isAir())
            boatMap.get(p.getUniqueId()).setVelocity(new Vector(0f, 1.0f, 0f));
    }

    @Override
    protected void onCompletedLap(@NotNull Player p) {
        boatMap.get(p.getUniqueId()).remove();
        boatMap.put(p.getUniqueId(), boat(p));
    }

    @Override
    protected void onRaceFinish(@NotNull Player p) {
        boatMap.get(p.getUniqueId()).remove();
        boatMap.remove(p.getUniqueId());
    }

    @EventHandler
    public void onDismount(@NotNull EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p && e.getDismounted() instanceof Boat) {
            if (check(p) || inPractice(p, this))
                e.setCancelled(true);
            else
                boatMap.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            if (check(p) || inPractice(p, this)) {
                if (b.getPassengers().isEmpty())
                    boatMap.put(p.getUniqueId(), b);
                else
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if ((check(p) || inPractice(p, this)) && boatMap.containsKey(p.getUniqueId())) {
            boatMap.get(p.getUniqueId()).remove();
            boatMap.put(p.getUniqueId(), boat(p));
        }
    }

    @EventHandler
    public void onFall(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if ((check(p) || inPractice(p, this))
                && boatMap.containsKey(p.getUniqueId())
                && e.getCause().equals(EntityDamageEvent.DamageCause.FALL))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull VehicleDestroyEvent e) {
        if (!e.getVehicle().getPassengers().isEmpty())
            e.setCancelled(true);
    }

    /**
     * Create a new boat at player's spawnpoint and put player on it.
     * @param p the player
     * @return a new boat containing the player
     */
    private @NotNull Boat boat(@NotNull Player p) {
        Boat boat = getWorld().spawn(Objects.requireNonNull(p.getBedSpawnLocation()), Boat.class);
        Boat.Type type = PlayerCustomize.getBoatType(p);
        if (type != null)
            boat.setBoatType(type);
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }

    private boolean check(Player p) {
        return Competitions.getCurrentEvent() == this
                && Competitions.isContestant(p)
                && !getLeaderboard().contains(Competitions.getContestant(p.getUniqueId()));
    }
}
