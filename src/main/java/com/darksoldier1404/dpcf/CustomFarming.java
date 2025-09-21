package com.darksoldier1404.dpcf;

import com.darksoldier1404.dpcf.chunk.ChunkCacheManager;
import com.darksoldier1404.dpcf.commands.DPCFCommand;
import com.darksoldier1404.dpcf.data.SeedData;
import com.darksoldier1404.dpcf.events.DPCFEvent;
import com.darksoldier1404.dpcf.functions.DPCFFuntion;
import com.darksoldier1404.dppc.data.DPlugin;
import com.darksoldier1404.dppc.data.DataContainer;
import com.darksoldier1404.dppc.data.DataType;
import com.darksoldier1404.dppc.utils.PluginUtil;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class CustomFarming extends DPlugin {
    public static CustomFarming plugin;
    public static YamlConfiguration data;
    public static Set<SeedData> seedDataSet;
    public static final Map<UUID, Tuple<Integer, String>> chanceSet = new HashMap<>();
    public static DataContainer<UUID, YamlConfiguration> udata;
    public static DataContainer<String, YamlConfiguration> seeds;

    public CustomFarming() {
        super(true);
        plugin = this;
        init();
    }

    public CustomFarming getInstance() {
        return plugin;
    }

    @Override
    public void onLoad() {
        PluginUtil.addPlugin(this, 25971);
        udata = loadDataContainer(new DataContainer<UUID, YamlConfiguration>(this, DataType.YAML, "data"), null);
        seeds = loadDataContainer(new DataContainer<String, YamlConfiguration>(this, DataType.YAML, "seeds"), null);
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
        saveDataContainer();
        DPCFFuntion.saveSeedData();
        for (Player p : Bukkit.getOnlinePlayers()) {
            DPCFFuntion.savePlayer(p);
        }
    }
}
