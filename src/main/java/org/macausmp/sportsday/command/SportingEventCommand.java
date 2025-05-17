package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.TextUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.*;

public class SportingEventCommand extends PluginCommand {
    private static final SportsDay PLUGIN = SportsDay.getInstance();

    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/event (start|load|save|pause|unpause|terminate|info) <argument>"));
            return;
        }
        switch (args[0]) {
            case "start" -> {
                if (args.length < 2) {
                    StringJoiner joiner = new StringJoiner("|", "(", ")");
                    SportsRegistry.SPORT.stream().forEach(s -> joiner.add(s.getKey().getKey()));
                    sender.sendMessage("/event start " + joiner + " <venue>");
                    return;
                }
                Sport sport = SportsRegistry.SPORT.get(new NamespacedKey(PLUGIN, args[1]));
                if (sport == null) {
                    sender.sendMessage(Component.translatable("sport.name.unknown").color(NamedTextColor.RED));
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage("/event start " + sport.getKey().getKey() + " <venue>");
                    return;
                }
                for (Venue venue : sport.getVenues()) {
                    if (venue.getName().equals(args[2])) {
                        SportsDay.startEvent(sender, sport, venue);
                        return;
                    }
                }
            }
            case "save" -> SportsDay.saveEvent(sender);
            case "load" -> SportsDay.loadEvent(sender, null);
            case "pause" -> SportsDay.pause(sender);
            case "unpause" -> SportsDay.unpause(sender);
            case "terminate" -> SportsDay.terminate(sender);
            case "info" -> {
                sender.sendMessage(Component.translatable("event.info"));
                SportingEvent event = SportsDay.getCurrentEvent();
                boolean hasEvent = event != null;
                sender.sendMessage(Component.translatable("event.current").color(NamedTextColor.GREEN)
                        .arguments(hasEvent ? event : TextUtil.convert(Component.translatable("gui.text.none"))));
                if (hasEvent)
                    sender.sendMessage(Component.translatable("event.status").color(NamedTextColor.GREEN)
                            .arguments(event.getStatus()));
                sender.sendMessage(Component.translatable("event.contestants.total").color(NamedTextColor.GREEN)
                        .arguments(Component.text(SportsDay.getContestants().size()).color(NamedTextColor.YELLOW)));
                sender.sendMessage(Component.translatable("event.contestants.online").color(NamedTextColor.GREEN)
                        .arguments(Component.text(SportsDay.getOnlineContestants().size()).color(NamedTextColor.YELLOW)));
                List<String> pl = new ArrayList<>();
                SportsDay.getContestants().stream().sorted(Comparator.comparingInt(ContestantData::getNumber))
                        .forEach(d -> pl.add(d.getName()));
                sender.sendMessage(Component.translatable("event.contestants.list").color(NamedTextColor.GREEN)
                        .arguments(Component.text(Arrays.toString(pl.toArray())).color(NamedTextColor.YELLOW)));
                List<String> el = new ArrayList<>();
                SportsRegistry.SPORT.stream().filter(s -> s.getSetting(Sport.Settings.ENABLE))
                        .forEach(e -> el.add(e.getKey().getKey().toUpperCase()));
                sender.sendMessage(Component.translatable("event.enabled").color(NamedTextColor.GREEN)
                        .arguments(Component.text(Arrays.toString(el.toArray())).color(NamedTextColor.YELLOW)));
            }
            default -> sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
        }
    }

    @Override
    public String name() {
        return "event";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> l = new ArrayList<>();
        if (args.length == 1) {
            l.add("start");
            l.add("save");
            l.add("load");
            l.add("pause");
            l.add("unpause");
            l.add("terminate");
            l.add("info");
        } else if (args.length == 2) {
            if (args[0].equals("start")) {
                SportsRegistry.SPORT.stream().filter(s -> s.getSetting(Sport.Settings.ENABLE))
                        .forEach(s -> l.add(s.getKey().getKey()));
            }
        } else if (args.length == 3) {
            if (args[0].equals("start")) {
                Sport sport = SportsRegistry.SPORT.get(new NamespacedKey(PLUGIN, args[1]));
                if (sport != null)
                    sport.getVenues().forEach(v -> l.add(v.getName()));
            }
        }
        return l;
    }
}
