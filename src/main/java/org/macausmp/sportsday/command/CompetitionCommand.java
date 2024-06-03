package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ContestantData;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.util.TextUtil;

import java.util.*;

public class CompetitionCommand extends PluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "start" -> {
                    if (args.length >= 2) {
                        Competitions.start(sender, args[1]);
                        return;
                    }
                    StringJoiner joiner = new StringJoiner("|", "(", ")");
                    Competitions.EVENTS.keySet().forEach(joiner::add);
                    sender.sendMessage("/competition start " + joiner);
                }
                case "end" -> Competitions.forceEnd(sender);
                case "join" -> {
                    if (args.length >= 2) {
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p == null) {
                            sender.sendMessage(Component.translatable("argument.player.unknown")
                                    .color(NamedTextColor.RED));
                            return;
                        }
                        if (Competitions.isContestant(p)) {
                            sender.sendMessage(Component.translatable("command.competition.register.failed")
                                    .arguments(p.displayName()).color(NamedTextColor.RED));
                            return;
                        }
                        int number;
                        if (args.length >= 3) {
                            try {
                                number = Integer.parseInt(args[2]);
                                if (number < 0) {
                                    sender.sendMessage(Component.translatable("argument.number.negative")
                                            .color(NamedTextColor.RED));
                                    return;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("parsing.int.invalid")
                                        .arguments(Component.text(args[2])).color(NamedTextColor.RED));
                                return;
                            }
                        } else {
                            number = Competitions.genNumber();
                        }
                        sender.sendMessage(Competitions.join(p, number)
                                ? Component.translatable("command.competition.register.success")
                                .arguments(p.displayName(), Component.text(number)).color(NamedTextColor.GREEN)
                                : Component.translatable("competition.register.failed.number_occupied")
                                .arguments(Component.text(number)).color(NamedTextColor.RED));
                    } else {
                        sender.sendMessage(Component.text("/competition join <player>"));
                    }
                }
                case "leave" -> {
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || !p.hasPlayedBefore()) {
                            sender.sendMessage(Component.translatable("argument.player.unknown")
                                    .color(NamedTextColor.RED));
                            return;
                        }
                        sender.sendMessage(Competitions.leave(p)
                                ? Component.translatable("command.competition.unregister.success")
                                .arguments(Component.text(p.getName())).color(NamedTextColor.GREEN)
                                : Component.translatable("command.competition.unregister.failed")
                                .arguments(Component.text(p.getName())).color(NamedTextColor.RED));
                    } else {
                        if (sender instanceof Player p) {
                            if (!Competitions.leave(p))
                                sender.sendMessage(Component.translatable("competition.unregister.failed")
                                        .color(NamedTextColor.RED));
                            return;
                        }
                        sender.sendMessage(Component.text("/competition leave <player>"));
                    }
                }
                case "score" -> {
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown")
                                    .color(NamedTextColor.RED));
                            return;
                        }
                        if (Competitions.isContestant(p)) {
                            ContestantData data = Competitions.getContestant(p.getUniqueId());
                            if (args.length == 2) {
                                sender.sendMessage(Component.translatable("command.competition.score.query")
                                        .arguments(Component.text(p.getName()), Component.text(data.getScore()))
                                        .color(NamedTextColor.GREEN));
                                return;
                            }
                            int score;
                            try {
                                score = Integer.parseInt(args[2]);
                                if (score < 0) {
                                    sender.sendMessage(Component.translatable("argument.score.negative").color(NamedTextColor.RED));
                                    return;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("parsing.int.invalid")
                                        .arguments(Component.text(args[2])).color(NamedTextColor.RED));
                                return;
                            }
                            data.setScore(score);
                            sender.sendMessage(Component.translatable("command.competition.score.set.success")
                                    .arguments(Component.text(p.getName()), Component.text(data.getScore()))
                                    .color(NamedTextColor.GREEN));
                            return;
                        }
                        sender.sendMessage(Component.translatable("command.competition.unregister.failed")
                                .arguments(Component.text(p.getName())));
                    } else {
                        sender.sendMessage(Component.text("/competition score <player> <new_score>"));
                    }
                }
                case "info" -> {
                    sender.sendMessage(Component.translatable("competition.info"));
                    IEvent event = Competitions.getCurrentEvent();
                    boolean b = event != null;
                    sender.sendMessage(Component.translatable("competition.current").color(NamedTextColor.GREEN)
                            .arguments(b ? event.getName() : TextUtil.convert(Component.translatable("gui.text.none"))));
                    if (b)
                        sender.sendMessage(Component.translatable("competition.status").color(NamedTextColor.GREEN)
                                .arguments(event.getStatus().getName()));
                    sender.sendMessage(Component.translatable("competition.contestants.total").color(NamedTextColor.GREEN)
                            .arguments(Component.text(Competitions.getContestants().size()).color(NamedTextColor.YELLOW)));
                    List<String> pl = new ArrayList<>();
                    Competitions.getContestants().stream().sorted(Comparator.comparingInt(ContestantData::getNumber))
                            .forEach(d -> pl.add(d.getName()));
                    sender.sendMessage(Component.translatable("competition.contestants.list").color(NamedTextColor.GREEN)
                            .arguments(Component.text(Arrays.toString(pl.toArray())).color(NamedTextColor.YELLOW)));
                    List<String> el = new ArrayList<>();
                    Competitions.EVENTS.values().stream().filter(IEvent::isEnable)
                            .forEach(e -> el.add(e.getID().toUpperCase()));
                    sender.sendMessage(Component.translatable("competition.enabled").color(NamedTextColor.GREEN)
                            .arguments(Component.text(Arrays.toString(el.toArray())).color(NamedTextColor.YELLOW)));
                }
                case "load" -> Competitions.loadEventData(sender);
                case "save" -> Competitions.saveEventData(sender);
                default -> sender.sendMessage(Component.translatable("command.unknown.argument")
                        .color(NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("/competition (start|end|join|leave|score|info) <argument>"));
        }
    }

    @Override
    public String name() {
        return "competition";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> l = new ArrayList<>();
        if (args.length == 1) {
            l.add("start");
            l.add("end");
            l.add("load");
            l.add("save");
            l.add("join");
            l.add("leave");
            l.add("score");
            l.add("info");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "start" -> Competitions.EVENTS.values().stream().filter(IEvent::isEnable).forEach(c -> l.add(c.getID()));
                case "join" -> Bukkit.getOnlinePlayers().stream().filter(p -> !Competitions.isContestant(p)).forEach(p -> l.add(p.getName()));
                case "leave", "score" -> Competitions.getContestants().forEach(d -> l.add(d.getName()));
            }
        }
        return l;
    }
}
