package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.event.EventGUI;
import org.macausmp.sportsday.gui.competition.event.SumoGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.*;
import java.util.function.Consumer;

public class Sumo extends FieldEvent {
    public static final Setting<Boolean> ENABLE_WEAPON = new Setting<>("enable_weapon", Boolean.class);
    public static final Setting<Integer> WEAPON_TIME = new Setting<>("weapon_time", Integer.class);
    public static final Setting<Location> P1_LOCATION = new Setting<>("p1-location", Location.class);
    public static final Setting<Location> P2_LOCATION = new Setting<>("p2-location", Location.class);
    private final Set<ContestantData> alive = new HashSet<>();
    private final List<ContestantData> queue = new ArrayList<>();
    private SumoStage[] stages;
    private int stageIndex = 0;

    public Sumo() {
        super("sumo", Material.COD);
    }

    @Override
    public EventGUI<? extends SportingEvent> getEventGUI() {
        return new SumoGUI(this);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onSetup() {
        alive.clear();
        alive.addAll(getContestants());
        queue.clear();
        queue.addAll(alive);
        int stage = Math.max(33 - Integer.numberOfLeadingZeros(getContestants().size() - 1), 3);
        stages = new SumoStage[stage];
        stageIndex = 0;
        for (int i = 1; i <= stage; i++) {
            int j = SumoStage.Stage.values().length;
            stages[stage - i] = new SumoStage(stage - i + 1, SumoStage.Stage.values()[i < j ? j - i : 0]);
        }
        int size = switch (stages[1].getStage()) {
            case SEMI_FINAL -> queue.size() - 4;
            case QUARTER_FINAL -> queue.size() - 8;
            default -> queue.size() / 2;
        };
        for (int i = 0; i < size; i++) {
            SumoMatch match = stages[0].newMatch();
            match.setPlayer(getFromQueue());
            match.setPlayer(getFromQueue());
        }
        stages[stages.length - 2].newMatch(); // Third place
        stages[stages.length - 1].newMatch(); // Final
        stageSetup();
    }

    private void stageSetup() {
        TranslatableComponent.Builder builder = Component.translatable("event.sumo.current_stage")
                .arguments(getSumoStage().getName()).toBuilder();
        for (int i = 0; i < getSumoStage().getMatchList().size();) {
            SumoMatch m = getSumoStage().getMatchList().get(i);
            builder.appendNewline().append(Component.translatable("event.sumo.queue")
                    .arguments(Component.text(++i), m.getFirstPlayerName(), m.getSecondPlayerName()));
        }
        Bukkit.broadcast(builder.build());
        SumoGUI.updateGUI();
    }

    @Override
    public void onStart() {
        nextMatch();
    }

    @Override
    public void onEnd() {
        for (int i = 0, length = stages.length; i < length; i++) {
            SumoStage stage = stages[i];
            for (SumoMatch match : stage.getMatchList()) {
                if (getSumoStage().getStage() == SumoStage.Stage.SEMI_FINAL)
                    continue;
                getLeaderboard().addFirst(Competitions.getContestant(match.getLoser()));
                if (i >= stages.length - 2)
                    getLeaderboard().addFirst(Competitions.getContestant(match.getWinner()));
            }
        }
        TranslatableComponent.Builder builder = Component.translatable("event.result").toBuilder();
        for (int i = 0, k = 1, size = 34 - Integer.numberOfLeadingZeros(getContestants().size() - 1); i < size; i++) {
            if (i < 4) {
                ContestantData data = getLeaderboard().get(i);
                builder.appendNewline().append(Component.translatable("event.sumo.rank")
                        .arguments(Component.text(i + 1), Component.text(data.getName())));
                data.addScore(switch (i) {
                    case 0 -> 10;
                    case 1 -> 8;
                    case 2 -> 6;
                    case 3 -> 5;
                    default -> 0;
                });
            } else {
                StringJoiner joiner = new StringJoiner(", ");
                for (int s = 2 << k, e = 2 << k + 1; s < e; s++) {
                    if (s > getLeaderboard().size() - 1)
                        break;
                    ContestantData data = getLeaderboard().get(i);
                    joiner.add(data.getName());
                    if (i < 6)
                        data.addScore(6 - i);
                }
                int s = (2 << k) + 1, e = Math.min(2 << (++k), getLeaderboard().size());
                builder.appendNewline().append(Component.translatable("event.sumo.rank")
                        .arguments(Component.text(s + (s != e ? "-" + e : "")), Component.text(joiner.toString())));
            }
        }
        Bukkit.broadcast(builder.build());
    }

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (Competitions.getCurrentEvent() == this && getStatus() == Status.STARTED && Competitions.isContestant(p)) {
            SumoMatch match = getSumoStage().getCurrentMatch();
            if (match == null || !match.contain(p.getUniqueId()))
                return;
            if (match.getStatus() == SumoMatch.MatchStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() == Material.WATER
                    && match.getStatus() == SumoMatch.MatchStatus.STARTED) {
                match.setResult(p.getUniqueId());
                onMatchEnd();
            }
        }
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            if (Competitions.getCurrentEvent() == this) {
                SumoMatch match = getSumoStage().getCurrentMatch();
                if (match != null && match.getStatus() == SumoMatch.MatchStatus.STARTED
                        && match.contain(player.getUniqueId()) && match.contain(damager.getUniqueId())) {
                    e.setDamage(0);
                    return;
                }
            }
            if (SportingEvent.inPractice(player, this) && SportingEvent.inPractice(damager, this)) {
                e.setDamage(0);
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (Competitions.getCurrentEvent() == this && getStatus() == Status.STARTED && Competitions.isContestant(p)) {
            SumoMatch match = getSumoStage().getCurrentMatch();
            UUID uuid = p.getUniqueId();
            ContestantData d = Competitions.getContestant(uuid);
            if (!alive.contains(d))
                return;
            if (match != null && match.contain(uuid)) {
                match.setResult(uuid);
                onMatchEnd();
            }
        }
    }

    public void onDisqualification(@NotNull ContestantData contestant) {
        super.onDisqualification(contestant);
        alive.remove(contestant);
        queue.remove(contestant);
        SumoMatch match = getSumoStage().getCurrentMatch();
        UUID uuid = contestant.getUUID();
        if (match != null && match.contain(uuid)) {
            match.setResult(uuid);
            onMatchEnd();
        }
    }

    private @NotNull UUID getFromQueue() {
        return queue.remove(new Random().nextInt(queue.size())).getUUID();
    }

    @Override
    public void onMatchStart() {
        SumoMatch match = getSumoStage().getCurrentMatch();
        match.setStatus(SumoMatch.MatchStatus.COMING);
        SumoGUI.updateGUI();
        OfflinePlayer p1 = match.getFirstPlayer();
        OfflinePlayer p2 = match.getSecondPlayer();
        if (!Competitions.isContestant(p1) || !Competitions.isContestant(p2)) {
            match.setResult((Competitions.isContestant(p1) ? p2 : p1).getUniqueId());
            onMatchEnd();
            return;
        }
        if (!p1.isOnline() || !p2.isOnline()) {
            match.setResult((p1.isOnline() ? p2 : p1).getUniqueId());
            onMatchEnd();
            return;
        }
        ((Player) p1).teleport(getSetting(P1_LOCATION));
        ((Player) p2).teleport(getSetting(P2_LOCATION));
        match.forEachPlayer(p -> p.getInventory().clear());
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                if (i != 0)
                    Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_start_countdown")
                            .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                if (i-- == 0) {
                    match.setStatus(SumoMatch.MatchStatus.STARTED);
                    Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_start"));
                    giveWeapon();
                    SumoGUI.updateGUI();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private void giveWeapon() {
        if (getSetting(ENABLE_WEAPON)) {
            addRunnable(new BukkitRunnable() {
                int i = getSetting(WEAPON_TIME);
                @Override
                public void run() {
                    SumoMatch match = getSumoStage().getCurrentMatch();
                    if (match == null || match.isEnd()) {
                        cancel();
                        return;
                    }
                    if (i <= 15 && i % 5 == 0 && i > 0)
                        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.knockback_stick.countdown")
                                .arguments(Component.text(i)).color(NamedTextColor.YELLOW));
                    if (i-- == 0) {
                        match.forEachPlayer(p -> p.getInventory().setItem(EquipmentSlot.HAND, weapon(p)));
                        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.knockback_stick.given"));
                        cancel();
                    }
                }
            }.runTaskTimer(PLUGIN, 0L, 20L));
        }
    }

    private @NotNull ItemStack weapon(@NotNull Player p) {
        ItemStack weapon = ItemUtil.setBind(ItemUtil.item(PlayerCustomize.getWeaponSkin(p), null, "item.sportsday.kb_stick"));
        weapon.editMeta(meta -> {
            meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
        });
        return weapon;
    }

    @Override
    public void onMatchEnd() {
        SumoMatch match = getSumoStage().getCurrentMatch();
        if (match.getLoser() != null)
            getWorld().strikeLightningEffect(Objects.requireNonNull(Bukkit.getPlayer(match.getLoser())).getLocation());
        Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.match_end"));
        Bukkit.broadcast(Component.translatable("event.sumo.match_winner")
                .arguments(Objects.requireNonNull(Bukkit.getPlayer(match.getWinner())).displayName()).color(NamedTextColor.YELLOW));
        match.forEachPlayer(p -> p.getInventory().clear());
        // eliminate loser
        if (getSumoStage().getStage() != SumoStage.Stage.SEMI_FINAL) {
            alive.removeIf(data -> data.getUUID().equals(match.getLoser()));
        } else {
            stages[stages.length - 2].getMatchList().getFirst().setPlayer(match.getLoser());
            stages[stages.length - 1].getMatchList().getFirst().setPlayer(match.getWinner());
        }
        // If this stage is not over
        if (getSumoStage().hasNextMatch()) {
            SumoMatch m = getSumoStage().getNextMatch();
            Bukkit.broadcast(Component.translatable("event.sumo.next_queue")
                    .arguments(m.getFirstPlayerName(), m.getSecondPlayerName()));
            nextMatch();
        } else {
            if (getSumoStage().hasNextStage())
                nextSumoStage();
            else
                end();
        }
        SumoGUI.updateGUI();
    }

    @Override
    public void nextMatch() {
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                if (isPaused()) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.broadcast.pause"));
                    return;
                }
                Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_match_countdown")
                        .arguments(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    SumoMatch prev = getSumoStage().getCurrentMatch();
                    if (prev != null)
                        prev.forEachPlayer(p -> p.teleport(getLocation()));
                    getSumoStage().nextMatch();
                    onMatchStart();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    private void nextSumoStage() {
        // If this is not the first stage of the event, it means there are some players in the arena
        SumoMatch prev = getSumoStage().getCurrentMatch();
        if (prev != null)
            prev.forEachPlayer(p -> p.teleport(getLocation()));
        if (++stageIndex < stages.length - 2) {
            queue.clear();
            queue.addAll(alive);
            int size = switch (stages[stageIndex + 1].getStage()) {
                case THIRD_PLACE -> 2;
                case SEMI_FINAL -> 4;
                case QUARTER_FINAL -> queue.size() - 8;
                case ELIMINATE -> queue.size() / 2;
                default -> 0; // Actually unreachable
            };
            for (int i = 0; i < size; i++) {
                SumoMatch match = getSumoStage().newMatch();
                match.setPlayer(queue.removeFirst().getUUID());
                match.setPlayer(queue.removeFirst().getUUID());
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 7;
            @Override
            public void run() {
                if (i <= 5 && i > 0)
                    Bukkit.getServer().sendActionBar(Component.translatable("event.sumo.next_stage_countdown")
                            .arguments(Component.text(i)).color(NamedTextColor.GREEN));
                if (i-- == 0) {
                    nextMatch();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
    }

    public SumoStage[] getSumoStages() {
        return stages;
    }

    public SumoStage getSumoStage() {
        return stages[stageIndex];
    }

    @Override
    protected void onPractice(@NotNull Player player) {}

    private static final SumoMatchDataType SUMO_MATCH = new SumoMatchDataType();
    private static final SumoStageDataType SUMO_STAGE = new SumoStageDataType();

    @Override
    public void load(@NotNull PersistentDataContainer data) {
        init();
        alive.clear();
        queue.clear();
        Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "alive"),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING)))
                .forEach(uuid -> alive.add(Competitions.getContestant(UUID.fromString(uuid))));
        stageIndex = Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "current_stage"), PersistentDataType.INTEGER));
        stages = Objects.requireNonNull(data.get(new NamespacedKey(PLUGIN, "stages"),
                PersistentDataType.LIST.listTypeFrom(SUMO_STAGE))).toArray(SumoStage[]::new);
        TranslatableComponent.Builder builder = Component.translatable("event.sumo.current_stage")
                .arguments(getSumoStage().getName()).toBuilder();
        for (int i = getSumoStage().getCurrentMatchIndex() + 1, j = 0; i < getSumoStage().getMatchList().size(); i++) {
            SumoMatch m = getSumoStage().getMatchList().get(i);
            builder.appendNewline().append(Component.translatable("event.sumo.queue")
                    .arguments(Component.text(++j), m.getFirstPlayerName(), m.getSecondPlayerName()));
        }
        Bukkit.broadcast(builder.build());
        SumoGUI.updateGUI();
        start();
    }

    @Override
    public void save(@NotNull PersistentDataContainer data) {
        data.set(new NamespacedKey(PLUGIN, "alive"),
                PersistentDataType.LIST.listTypeFrom(PersistentDataType.STRING),
                alive.stream().map(d -> d.getUUID().toString()).toList());
        data.set(new NamespacedKey(PLUGIN, "current_stage"), PersistentDataType.INTEGER, stageIndex);
        data.set(new NamespacedKey(PLUGIN, "stages"), PersistentDataType.LIST.listTypeFrom(SUMO_STAGE),
                Arrays.stream(stages).toList());
    }

    public static class SumoMatch {
        private final int number;
        private final UUID[] contestants = new UUID[2];
        private MatchStatus status = MatchStatus.UPCOMING;
        private UUID winner;
        private UUID loser;

        protected SumoMatch(int number) {
            this.number = number;
        }

        protected void setPlayer(UUID uuid) {
            if (isSet())
                return;
            contestants[contestants[0] == null ? 0 : 1] = uuid;
        }

        public boolean isSet() {
            return contestants[0] != null && contestants[1] != null;
        }

        public boolean isEnd() {
            return status == MatchStatus.ENDED;
        }

        protected void setResult(UUID defeated) {
            if (isEnd())
                return;
            int i = indexOf(defeated);
            if (i == -1)
                return;
            winner = contestants[i ^ 1];
            loser = contestants[i];
            status = MatchStatus.ENDED;
        }

        public OfflinePlayer getFirstPlayer() {
            return Bukkit.getOfflinePlayer(contestants[0]);
        }

        public Component getFirstPlayerName() {
            return Component.text(Objects.requireNonNull(getFirstPlayer().getName()));
        }

        public OfflinePlayer getSecondPlayer() {
            return Bukkit.getOfflinePlayer(contestants[1]);
        }

        public Component getSecondPlayerName() {
            return Component.text(Objects.requireNonNull(getSecondPlayer().getName()));
        }

        public boolean contain(@NotNull UUID uuid) {
            if (!isSet())
                return false;
            return contestants[0].equals(uuid) || contestants[1].equals(uuid);
        }

        @MagicConstant(intValues = {-1, 0, 1})
        private int indexOf(UUID uuid) {
            if (!isSet())
                return -1;
            if (contestants[0].equals(uuid))
                return 0;
            if (contestants[1].equals(uuid))
                return 1;
            return -1;
        }

        public void forEachPlayer(@NotNull Consumer<Player> consumer) {
            if (!isSet())
                return;
            consumer.accept(Bukkit.getPlayer(contestants[0]));
            consumer.accept(Bukkit.getPlayer(contestants[1]));
        }

        public int getNumber() {
            return number;
        }

        public MatchStatus getStatus() {
            return status;
        }

        protected void setStatus(MatchStatus status) {
            this.status = status;
        }

        public UUID getWinner() {
            return winner;
        }

        public UUID getLoser() {
            return loser;
        }

        public enum MatchStatus implements ComponentLike {
            UPCOMING("competition.status.upcoming"),
            COMING("competition.status.coming"),
            STARTED("competition.status.started"),
            ENDED("competition.status.ended");

            private final Component name;

            MatchStatus(String code) {
                this.name = TextUtil.convert(Component.translatable(code));
            }

            @Override
            public @NotNull Component asComponent() {
                return name;
            }
        }
    }

    public static class SumoStage {
        private final int number;
        private final SumoStage.Stage stage;
        private final List<SumoMatch> matchList = new ArrayList<>();
        private int matchIndex = -1;
        private SumoMatch currentMatch;

        protected SumoStage(int number, SumoStage.Stage stage) {
            this.number = number;
            this.stage = stage;
        }

        public int getNumber() {
            return number;
        }

        public SumoStage.Stage getStage() {
            return stage;
        }

        public List<SumoMatch> getMatchList() {
            return matchList;
        }

        public boolean hasNextStage() {
            return stage != SumoStage.Stage.FINAL;
        }

        public SumoMatch getCurrentMatch() {
            return currentMatch;
        }

        public int getCurrentMatchIndex() {
            return matchIndex;
        }

        public void nextMatch() {
            currentMatch = matchList.get(++matchIndex);
        }

        protected SumoMatch newMatch() {
            SumoMatch match = new SumoMatch(matchList.size() + 1);
            matchList.add(match);
            return match;
        }

        public boolean hasNextMatch() {
            return matchIndex + 1 < matchList.size();
        }

        public SumoMatch getNextMatch() {
            return hasNextMatch() ? matchList.get(matchIndex + 1) : null;
        }

        public Component getName() {
            return stage.name;
        }

        public Material getIcon() {
            return stage.icon;
        }

        public enum Stage {
            ELIMINATE(Component.translatable("event.sumo.eliminate"), Material.IRON_BLOCK),
            QUARTER_FINAL(Component.translatable("event.sumo.quarter_final"), Material.LAPIS_BLOCK),
            SEMI_FINAL(Component.translatable("event.sumo.semi_final"), Material.REDSTONE_BLOCK),
            THIRD_PLACE(Component.translatable("event.sumo.third_place"), Material.COPPER_BLOCK),
            FINAL(Component.translatable("event.sumo.final"), Material.GOLD_BLOCK);

            final Component name;
            final Material icon;

            Stage(Component name, Material icon) {
                this.name = name;
                this.icon = icon;
            }
        }
    }

    private static final class SumoMatchDataType implements PersistentDataType<PersistentDataContainer, SumoMatch> {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SumoMatch> getComplexType() {
            return SumoMatch.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SumoMatch complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "number"), INTEGER, complex.number);
            if (complex.contestants[0] != null)
                pdc.set(new NamespacedKey(PLUGIN, "p1"), STRING, complex.contestants[0].toString());
            if (complex.contestants[1] != null)
                pdc.set(new NamespacedKey(PLUGIN, "p2"), STRING, complex.contestants[1].toString());
            boolean end = complex.isEnd();
            pdc.set(new NamespacedKey(PLUGIN, "end"), BOOLEAN, end);
            if (end)
                pdc.set(new NamespacedKey(PLUGIN, "loser"), STRING, complex.loser.toString());
            return pdc;
        }

        @Override
        public @NotNull SumoMatch fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            SumoMatch match = new SumoMatch(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "number"), INTEGER)));
            if (primitive.has(new NamespacedKey(PLUGIN, "p1")))
                match.setPlayer(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p1"), STRING))));
            if (primitive.has(new NamespacedKey(PLUGIN, "p2")))
                match.setPlayer(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "p2"), STRING))));
            if (Boolean.TRUE.equals(primitive.get(new NamespacedKey(PLUGIN, "end"), BOOLEAN)))
                match.setResult(UUID.fromString(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "loser"), STRING))));
            return match;
        }
    }

    private static final class SumoStageDataType implements PersistentDataType<PersistentDataContainer, SumoStage> {
        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SumoStage> getComplexType() {
            return SumoStage.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SumoStage complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            pdc.set(new NamespacedKey(PLUGIN, "number"), INTEGER, complex.number);
            pdc.set(new NamespacedKey(PLUGIN, "stage"), STRING, complex.stage.name());
            pdc.set(new NamespacedKey(PLUGIN, "index"), INTEGER,
                    complex.matchIndex - (complex.currentMatch == null || complex.currentMatch.isEnd() ? 0 : 1));
            pdc.set(new NamespacedKey(PLUGIN, "match_list"), LIST.listTypeFrom(SUMO_MATCH), complex.matchList);
            return pdc;
        }

        @Override
        public @NotNull SumoStage fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            int number = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "number"), INTEGER));
            SumoStage.Stage stage = SumoStage.Stage.valueOf(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "stage"), STRING)));
            SumoStage sumoStage = new SumoStage(number, stage);
            sumoStage.matchList.addAll(Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "match_list"),
                    LIST.listTypeFrom(SUMO_MATCH))));
            sumoStage.matchIndex = Objects.requireNonNull(primitive.get(new NamespacedKey(PLUGIN, "index"), INTEGER));
            if (sumoStage.matchIndex != -1)
                sumoStage.currentMatch = sumoStage.matchList.get(sumoStage.matchIndex);
            return sumoStage;
        }
    }
}
