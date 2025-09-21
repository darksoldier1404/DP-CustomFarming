package com.darksoldier1404.dpcf.commands;

import com.darksoldier1404.dpcf.functions.DPCFFuntion;
import com.darksoldier1404.dppc.builder.command.CommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.darksoldier1404.dpcf.CustomFarming.plugin;

public class DPCFCommand {
    private final CommandBuilder builder;

    public DPCFCommand() {
        builder = new CommandBuilder(plugin);

        builder.addSubCommand("create", "dpcf.create", plugin.getLang().get("cmd_create"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.addSeed(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_create"));
            return true;
        });

        builder.addSubCommand("remove", "dpcf.remove", plugin.getLang().get("cmd_remove"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.removeSeed(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_remove"));
            return true;
        });

        builder.addSubCommand("time", "dpcf.time", plugin.getLang().get("cmd_time"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setGrowTime(p, args[1], args[2]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_time"));
            return true;
        });

        builder.addSubCommand("crops", "dpcf.crops", plugin.getLang().get("cmd_crops"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.setSeedBlock(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_crops"));
            return true;
        });

        builder.addSubCommand("drops", "dpcf.drops", plugin.getLang().get("cmd_drops"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.setDrops(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_drops"));
            return true;
        });

        builder.addSubCommand("seeditem", "dpcf.seeditem", plugin.getLang().get("cmd_seeditem"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.setSeedItem(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_seeditem"));
            return true;
        });

        builder.addSubCommand("seeddrop", "dpcf.seeddrop", plugin.getLang().get("cmd_seeddrop"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setSeedDropCount(p, args[1], args[2]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_seeddrop"));
            return true;
        });

        builder.addSubCommand("chance", "dpcf.chance", plugin.getLang().get("cmd_chance"), true, (p, args) -> {
            if (args.length == 2) DPCFFuntion.openChanceSettings(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_chance"));
            return true;
        });

        builder.addSubCommand("placelimit", "dpcf.placelimit", plugin.getLang().get("cmd_placelimit"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setLimit(p, args[1], args[2]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_placelimit"));
            return true;
        });

        builder.addSubCommand("worldlimit", "dpcf.worldlimit", plugin.getLang().get("cmd_worldlimit"), (p, args) -> {
            if (args.length == 3) DPCFFuntion.setWorldLimit(p, args[1], args[2]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_worldlimit"));
            return true;
        });

        builder.addSubCommand("get", "dpcf.get", plugin.getLang().get("cmd_get"), (p, args) -> {
            if (args.length == 2) DPCFFuntion.giveSeedItem(p, args[1]);
            else p.sendMessage(plugin.getPrefix() + plugin.getLang().get("cmd_get"));
            return true;
        });

        builder.addSubCommand("list", "dpcf.list", plugin.getLang().get("cmd_list"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.listSeed(p);
            return true;
        });

        builder.addSubCommand("count", "dpcf.count", plugin.getLang().get("cmd_count"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.countSeed(p);
            return true;
        });

        builder.addSubCommand("reload", "dpcf.reload", plugin.getLang().get("cmd_reload"), (p, args) -> {
            if (args.length == 1) DPCFFuntion.reloadConfig(p);
            return true;
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
