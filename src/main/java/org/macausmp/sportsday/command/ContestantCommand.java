package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;

public class ContestantCommand extends PluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/contestant (join|leave|score|list) <argument>"));
            return;
        }
        switch (args[0]) {
            case "join" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("/contestant join <player>"));
                    return;
                }
                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(Component.translatable("argument.player.unknown")
                            .color(NamedTextColor.RED));
                    return;
                }
                if (SportsDay.isContestant(p)) {
                    sender.sendMessage(Component.translatable("command.contestant.register.failed")
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
                    number = SportsDay.genNumber();
                }
                sender.sendMessage(SportsDay.join(p, number)
                        ? Component.translatable("command.contestant.register.success")
                        .arguments(p.displayName(), Component.text(number)).color(NamedTextColor.GREEN)
                        : Component.translatable("contestant.register.failed.number_occupied")
                        .arguments(Component.text(number)).color(NamedTextColor.RED));
            }
            case "leave" -> {
                if (args.length < 2) {
                    if (sender instanceof Player p) {
                        if (!SportsDay.leave(p))
                            sender.sendMessage(Component.translatable("contestant.unregister.failed")
                                    .color(NamedTextColor.RED));
                        return;
                    }
                    sender.sendMessage(Component.text("/contestant leave <player>"));
                    return;
                }
                OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                if (p.getName() == null || !p.hasPlayedBefore()) {
                    sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                    return;
                }
                sender.sendMessage(SportsDay.leave(p)
                        ? Component.translatable("command.contestant.unregister.success")
                        .arguments(Component.text(p.getName())).color(NamedTextColor.GREEN)
                        : Component.translatable("command.contestant.unregister.failed")
                        .arguments(Component.text(p.getName())).color(NamedTextColor.RED));
            }
            case "score" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("/contestant score <player> <new_score>"));
                    return;
                }
                OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                if (p.getName() == null || p.getFirstPlayed() == 0L) {
                    sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                    return;
                }
                if (!SportsDay.isContestant(p)) {
                    sender.sendMessage(Component.translatable("command.contestant.unregister.failed")
                            .arguments(Component.text(p.getName())));
                    return;
                }
                ContestantData data = SportsDay.getContestant(p.getUniqueId());
                if (args.length == 2) {
                    sender.sendMessage(Component.translatable("command.contestant.score.query")
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
                sender.sendMessage(Component.translatable("command.contestant.score.set.success")
                        .arguments(Component.text(p.getName()), Component.text(data.getScore()))
                        .color(NamedTextColor.GREEN));
            }
            case "list" -> {
                TextComponent.Builder builder = Component.text();
                SportsDay.getContestants().forEach(data -> builder.appendNewline()
                        .append(Component.text(data.getName() + " #" + data.getNumber())
                                .color(data.isOnline() ? NamedTextColor.YELLOW : NamedTextColor.GRAY)));
                sender.sendMessage(Component.translatable("command.contestant.list").color(NamedTextColor.YELLOW)
                        .arguments(builder));
            }
        }
    }

    @Override
    public String name() {
        return "contestant";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> l = new ArrayList<>();
        if (args.length == 1) {
            l.add("join");
            l.add("leave");
            l.add("score");
            l.add("list");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "join" -> Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !SportsDay.isContestant(p))
                        .forEach(p -> l.add(p.getName()));
                case "leave", "score" -> SportsDay.getContestants().forEach(d -> l.add(d.getName()));
            }
        }
        return l;
    }
}
