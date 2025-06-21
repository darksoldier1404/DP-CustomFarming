package com.darksoldier1404.dpcf;

import com.darksoldier1404.dpcf.chunk.ChunkCacheManager;
import com.darksoldier1404.dpcf.commands.DPCFCommand;
import com.darksoldier1404.dpcf.data.SeedData;
import com.darksoldier1404.dpcf.events.DPCFEvent;
import com.darksoldier1404.dpcf.functions.DPCFFuntion;
import com.darksoldier1404.dppc.lang.DLang;
import com.darksoldier1404.dppc.utils.PluginUtil;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CustomFarming extends JavaPlugin {
    public static CustomFarming plugin;
    public static YamlConfiguration config;
    public static DLang lang;
    public static String prefix;
    public static YamlConfiguration data;
    public static Set<SeedData> seedDataSet;
    public static final Map<UUID, Tuple<Integer, String>> chanceSet = new HashMap<>();
    public static final Map<UUID, YamlConfiguration> udata = new HashMap<>();
    public static final Map<String, YamlConfiguration> seeds = new HashMap<>();

    public CustomFarming getInstance() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;
        PluginUtil.addPlugin(this, 25971);
    }

    public void onEnable() {
        DPCFFuntion.init();
        DPCFFuntion.loadSeedData();
        plugin.getServer().getPluginManager().registerEvents(new DPCFEvent(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChunkCacheManager(), plugin);
        DPCFFuntion.initTask();
        getCommand("dpcf").setExecutor(new DPCFCommand().getExecutor());
    }

    public void onDisable() {
        DPCFFuntion.saveSeedData();
        for (Player p : Bukkit.getOnlinePlayers()) {
            DPCFFuntion.savePlayer(p);
        }
    }
}
