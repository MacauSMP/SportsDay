package org.macausmp.sportsday.competition;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;

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
        if (!predicate.test(p) || !boatMap.containsKey(p.getUniqueId()))
            return;
        bounce(p);
    }

    private void bounce(@NotNull Player p) {
        Location loc = p.getLocation().clone();
        loc.setY(loc.getY() - 0.5f);
        if (loc.getBlock().getType() == SPRING_BLOCK && !SPRING_BLOCK.isAir())
            boatMap.get(p.getUniqueId()).setVelocity(new Vector(0f, 0.5f, 0f));
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
            if (predicate.test(p))
                e.setCancelled(true);
            else
                boatMap.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onMount(@NotNull EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && e.getMount() instanceof Boat b) {
            if (!predicate.test(p))
                return;
            if (b.getPassengers().isEmpty())
                boatMap.put(p.getUniqueId(), b);
            else
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p) || !boatMap.containsKey(p.getUniqueId()))
            return;
        boatMap.get(p.getUniqueId()).remove();
        boatMap.put(p.getUniqueId(), boat(p));
    }

    @EventHandler
    public void onFall(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;
        if (predicate.test(p) && boatMap.containsKey(p.getUniqueId())
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
        Boat boat = (Boat) getWorld().spawnEntity(Objects.requireNonNull(p.getRespawnLocation()), PlayerCustomize.getBoatType(p));
        boat.setInvulnerable(true);
        boat.addPassenger(p);
        return boat;
    }
}
