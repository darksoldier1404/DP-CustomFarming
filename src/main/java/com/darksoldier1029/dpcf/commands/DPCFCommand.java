package com.darksoldier1029.dpcf.commands;

import com.darksoldier1029.dpcf.functions.DPCFFuntion;
import com.darksoldier1404.dppc.builder.command.CommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.darksoldier1029.dpcf.CustomFarming.plugin;
import static com.darksoldier1029.dpcf.CustomFarming.prefix;
import static com.darksoldier1029.dpcf.CustomFarming.lang;

public class DPCFCommand {
    private final CommandBuilder builder;

    public DPCFCommand() {
        builder = new CommandBuilder(prefix);

        builder.addSubCommand("create", "dpcf.create", lang.get("cmd_create"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.addSeed(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_create"));
        });

        builder.addSubCommand("remove", "dpcf.remove", lang.get("cmd_remove"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.removeSeed(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_remove"));
        });

        builder.addSubCommand("time", "dpcf.time", lang.get("cmd_time"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setGrowTime(p, args[1], args[2]);
            else p.sendMessage(prefix + lang.get("cmd_time"));
        });

        builder.addSubCommand("crops", "dpcf.crops", lang.get("cmd_crops"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.setSeedBlock(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_crops"));
        });

        builder.addSubCommand("drops", "dpcf.drops", lang.get("cmd_drops"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.setDrops(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_drops"));
        });

        builder.addSubCommand("seeditem", "dpcf.seeditem", lang.get("cmd_seeditem"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.setSeedItem(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_seeditem"));
        });

        builder.addSubCommand("seeddrop", "dpcf.seeddrop", lang.get("cmd_seeddrop"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setSeedDropCount(p, args[1], args[2]);
            else p.sendMessage(prefix + lang.get("cmd_seeddrop"));
        });

        builder.addSubCommand("chance", "dpcf.chance", lang.get("cmd_chance"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.openChanceSettings(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_chance"));
        });

        builder.addSubCommand("placelimit", "dpcf.placelimit", lang.get("cmd_placelimit"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setLimit(p, args[1], args[2]);
            else p.sendMessage(prefix + lang.get("cmd_placelimit"));
        });

        builder.addSubCommand("worldlimit", "dpcf.worldlimit", lang.get("cmd_worldlimit"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setWorldLimit(p, args[1], args[2]);
            else p.sendMessage(prefix + lang.get("cmd_worldlimit"));
        });

        builder.addSubCommand("get", "dpcf.get", lang.get("cmd_get"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.giveSeedItem(p, args[1]);
            else p.sendMessage(prefix + lang.get("cmd_get"));
        });

        builder.addSubCommand("list", "dpcf.list", lang.get("cmd_list"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.listSeed(p);
        });

        builder.addSubCommand("count", "dpcf.count", lang.get("cmd_count"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.countSeed(p);
        });

        builder.addSubCommand("reload", "dpcf.reload", lang.get("cmd_reload"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.reloadConfig(p);
        });

        List<String> cmds = new ArrayList<>(Arrays.asList("create", "remove", "time", "crops", "drops", "seeditem", "seeddrop", "chance", "placelimit", "worldlimit", "get", "list", "count", "reload"));
        for (String c : cmds) {
            builder.addTabCompletion(c, args -> {
                if (args.length == 2) return new ArrayList<>(plugin.seeds.keySet());
                else if (args.length == 3)
                    if (c.equalsIgnoreCase("worldlimit"))
                        return Bukkit.getWorlds().stream().map(world -> world.getName()).collect(Collectors.toList());
                return null;
            });
        }
    }

    public CommandExecutor getExecutor() {
        return builder;
    }
}
