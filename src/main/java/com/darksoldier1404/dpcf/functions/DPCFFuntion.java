package com.darksoldier1404.dpcf.functions;

import com.darksoldier1404.dpcf.chunk.ChunkCacheManager;
import com.darksoldier1404.dpcf.data.SeedData;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.ConfigUtils;
import com.darksoldier1404.dppc.utils.NBT;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static com.darksoldier1404.dpcf.CustomFarming.*;

@SuppressWarnings("static-access")
public class DPCFFuntion {
    public static void init() {
        plugin.data = ConfigUtils.createCustomData(plugin, "data");
    }

    public static void reloadConfig(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_reload"));
    }

    public static void loadSeedData() {
        Set<SeedData> seedDataSet = new HashSet<>();

        if (data.isConfigurationSection("Data")) {
            for (String uuid : data.getConfigurationSection("Data").getKeys(false)) {
                String seed = data.getString("Data." + uuid + ".Seed");
                String owner = data.getString("Data." + uuid + ".Owner");
                String world = data.getString("Data." + uuid + ".World");
                int x = data.getInt("Data." + uuid + ".X");
                int y = data.getInt("Data." + uuid + ".Y");
                int z = data.getInt("Data." + uuid + ".Z");
                boolean isGrow = data.getBoolean("Data." + uuid + ".isGrow");
                int remainingTime = data.getInt("Data." + uuid + ".RemainingTime");
                boolean isBlockedGrowByUnloadedChunk = data.getBoolean("Data." + uuid + ".isBlockedGrowByUnloadedChunk");
                boolean isRightClickBreak = data.getBoolean("Data." + uuid + ".isRightClickBreak");
                seedDataSet.add(new SeedData(uuid, UUID.fromString(owner), seed, world, x, y, z, isGrow, remainingTime, isBlockedGrowByUnloadedChunk, isRightClickBreak));
            }
        }
        plugin.seedDataSet = seedDataSet;
    }

    public static void saveSeedData() {

        data.set("Data", null);

        for (SeedData seedData : plugin.seedDataSet) {
            String uuid = seedData.getUUID();
            ConfigurationSection section = data.createSection("Data." + uuid);
            seedData.serialize(section);
        }
        ConfigUtils.saveCustomData(plugin, data, "data");
    }

    public static void initTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (SeedData seedData : plugin.seedDataSet) {
                World world = Bukkit.getWorld(seedData.getWorld());
                if (seedData.isBlockedGrowByUnloadedChunk()) {
                    if (world == null) continue;
                    if (ChunkCacheManager.isChunkLoaded(world, seedData.getX(), seedData.getZ())) {
                        seedData.setBlockedGrowByUnloadedChunk(false);
                        placeItemAtLocation(world, seedData.getX(), seedData.getY(), seedData.getZ(), getSeedDataAsBlockData(seedData.getSeed()));
                    } else {
                        continue;
                    }
                }
                if (seedData.getRemainingTime() <= 0) {
                    seedData.setGrow(true);
                    if (ChunkCacheManager.isChunkLoaded(world, seedData.getX(), seedData.getZ())) {
                        placeItemAtLocation(world, seedData.getX(), seedData.getY(), seedData.getZ(), getSeedDataAsBlockData(seedData.getSeed()));
                    } else {
                        seedData.setBlockedGrowByUnloadedChunk(true);
                    }
                } else {
                    seedData.setRemainingTime(seedData.getRemainingTime() - 1);
                }
            }
        }, 0L, 20L);
    }

    @Nullable
    public static ItemStack getSeedDataAsBlockData(String seed) {
        YamlConfiguration data = getSeed(seed);
        return data.getItemStack("Seeds." + seed + ".Material");
    }

    // command functions
    public static void addSeed(CommandSender sender, String seed) {
        if (checkSeed(seed)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_reload"));
            return;
        }
        YamlConfiguration data = new YamlConfiguration();
        data.set("Seeds.Name", seed);
        data.set("Seeds." + seed + ".SeedGrowTime", 120);
        plugin.saveDataContainer();
        plugin.seeds.put(seed, data);
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedCreated"));
    }

    public static void removeSeed(CommandSender sender, String seed) {
        if (!checkSeed(seed)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        new File(plugin.getDataFolder(), "seeds/" + seed + ".yml").delete();
        plugin.seeds.remove(seed);
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedRemoved"));
    }

    public static void setGrowTime(CommandSender sender, String seed, String growTime) {
        if (!checkSeed(seed)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        if (!growTime.matches("[0-9]+")) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_NumberFormatException"));
            return;
        }
        YamlConfiguration data = getSeed(seed);
        data.set("Seeds." + seed + ".SeedGrowTime", Integer.parseInt(growTime));
        plugin.seeds.put(seed, data);
        plugin.saveDataContainer();
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedGrowthTimeSet"));
    }

    public static boolean checkSeed(String seed) {
        return plugin.seeds.containsKey(seed);
    }

    public static YamlConfiguration getSeed(String seed) {
        if (!checkSeed(seed)) {
            return null;
        }
        return plugin.seeds.get(seed);
    }

    public static boolean isSeed(String world, int x, int y, int z) {
        for (SeedData seedData : plugin.seedDataSet) {
            if (seedData.getWorld().equalsIgnoreCase(world) && seedData.getX() == x && seedData.getY() == y && seedData.getZ() == z) {
                return true;
            }
        }
        return false;
    }

    public static SeedData getSeedData(String world, int x, int y, int z) {
        for (SeedData seedData : plugin.seedDataSet) {
            if (seedData.getX() == x && seedData.getY() == y && seedData.getZ() == z && seedData.getWorld().equalsIgnoreCase(world)) {
                return seedData;
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack getSeedItem(String seed) {
        YamlConfiguration data = getSeed(seed);
        ItemStack item = data.getItemStack("Seeds." + seed + ".ItemMaterial");
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.setDisplayName(plugin.getLang().getWithArgs("item_seed_name", seed));
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getLang().get("item_seed_lore_1"));
        lore.add(plugin.getLang().getWithArgs("item_seed_lore_2", String.valueOf(data.getInt("Seeds." + seed + ".SeedGrowTime"))));
        meta.setLore(lore);
        item.setItemMeta(meta);
        item = NBT.setStringTag(item, "dpcf_seed", seed);
        return item;
    }

    public static void giveSeedItem(CommandSender sender, String seed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_playerOnly"));
            return;
        }
        Player p = (Player) sender;
        if (!checkSeed(seed)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        ItemStack item = getSeedItem(seed);
        if (item == null) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_seedItemNotExists"));
            return;
        }
        p.getInventory().addItem(item);
    }

    public static void listSeed(CommandSender sender) {
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedList"));
        for (String seed : plugin.seeds.keySet()) {
            YamlConfiguration data = getSeed(seed);
            if (data == null) {
                continue;
            }
            String name = seed;
            String material = data.getItemStack("Seeds." + seed + ".ItemMaterial") == null ? "None" : data.getItemStack("Seeds." + seed + ".ItemMaterial").getType().toString();
            String growTime = String.valueOf(data.getInt("Seeds." + seed + ".SeedGrowTime", 0));
            String limit = String.valueOf(data.getInt("Seeds." + seed + ".Limit", 0));
            String worldLimit = "";
            if (data.isConfigurationSection("Seeds." + seed + ".WorldLimit")) {
                for (String world : data.getConfigurationSection("Seeds." + seed + ".WorldLimit").getKeys(false)) {
                    worldLimit += world + ", ";
                }
                if (!worldLimit.isEmpty()) {
                    worldLimit = worldLimit.substring(0, worldLimit.length() - 2);
                }
            } else {
                worldLimit = "None";
            }
            sender.sendMessage(plugin.getLang().getWithArgs("func_seedListContext", name, material, growTime, limit, worldLimit));
        }
    }

    public static void setSeedBlock(CommandSender sender, String seed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_playerOnly"));
            return;
        }
        Player p = (Player) sender;
        if (!checkSeed(seed)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        YamlConfiguration data = getSeed(seed);
        DInventory inv = new DInventory(plugin.getLang().getWithArgs("inv_seed_crops_title", seed), 27, plugin);
        ItemStack pane = NBT.setStringTag(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "dpcf_pane", "true");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }
        inv.setItem(13, data.getItemStack("Seeds." + seed + ".Material"));
        Tuple<String, String> tuple = new Tuple<>("dpcf_material", seed);
        inv.setObj(tuple);
        p.openInventory(inv.getInventory());
    }

    public static void saveSeedBlock(String seed, DInventory inv, Player p) {
        ItemStack item = inv.getItem(13);
        YamlConfiguration data = getSeed(seed);
        if (item == null || item.getType() == Material.AIR) {
            data.set("Seeds." + seed + ".Material", null);
            plugin.saveDataContainer();
            plugin.seeds.put(seed, data);
            return;
        }
        try {
            item.getType().createBlockData();
        } catch (Exception e) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("inv_seed_crops_BlockTypeException"));
            return;
        }
        data.set("Seeds." + seed + ".Material", item);
        plugin.saveDataContainer();
        plugin.seeds.put(seed, data);
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("inv_seed_crops_BlockTypeSet"));
    }

    public static void setDrops(CommandSender sender, String seed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_playerOnly"));
            return;
        }
        Player p = (Player) sender;
        if (!checkSeed(seed)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        DInventory inv = new DInventory(plugin.getLang().getWithArgs("inv_seed_drops_title", seed), 27, plugin);
        YamlConfiguration data = getSeed(seed);
        if (data.isConfigurationSection("Seeds." + seed + ".Drops")) {
            for (String key : data.getConfigurationSection("Seeds." + seed + ".Drops").getKeys(false)) {
                ItemStack item = data.getItemStack("Seeds." + seed + ".Drops." + key + ".Item");
                int slot = Integer.parseInt(key);
                inv.setItem(slot, item);
            }
        }
        Tuple<String, String> tuple = new Tuple<>("dpcf_drops", seed);
        inv.setObj(tuple);
        p.openInventory(inv.getInventory());
    }

    public static void saveDrops(String seed, DInventory inv, Player p) {
        YamlConfiguration data = getSeed(seed);
        for (int i = 0; i < 27; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                data.set("Seeds." + seed + ".Drops." + i + ".Item", item);
            } else {
                data.set("Seeds." + seed + ".Drops." + i, null);
            }
        }
        plugin.saveDataContainer();
        plugin.seeds.put(seed, data);
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("inv_seed_drops_DropItemSet"));
    }

    public static void openChanceSettings(CommandSender sender, String seed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_playerOnly"));
            return;
        }
        Player p = (Player) sender;
        if (!checkSeed(seed)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        DInventory inv = new DInventory(plugin.getLang().getWithArgs("inv_seed_chance_title", seed), 27, plugin);
        YamlConfiguration data = getSeed(seed);
        if (data.isConfigurationSection("Seeds." + seed + ".Drops")) {
            for (String key : data.getConfigurationSection("Seeds." + seed + ".Drops").getKeys(false)) {
                ItemStack item = data.getItemStack("Seeds." + seed + ".Drops." + key + ".Item");
                int slot = Integer.parseInt(key);
                inv.setItem(slot, item);
            }
        }
        Tuple<String, String> tuple = new Tuple<>("dpcf_chance", seed);
        inv.setObj(tuple);
        p.openInventory(inv.getInventory());
    }

    public static void setChanceWithChat(Player p, String seed, int slot) {
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_enter_chance"));
        p.closeInventory();
        plugin.chanceSet.put(p.getUniqueId(), new Tuple<>(slot, seed));
    }

    public static void setChance(Player p, String chance, String seed, int slot) {
        if (!chance.matches("[0-9]+")) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_NumberFormatException"));
            return;
        }
        if (Integer.parseInt(chance) > 100) {
            p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_chance_over"));
            return;
        }
        YamlConfiguration data = getSeed(seed);
        data.set("Seeds." + seed + ".Drops." + slot + ".Chance", Integer.parseInt(chance));
        plugin.saveDataContainer();
        plugin.seeds.put(seed, data);
        plugin.chanceSet.remove(p.getUniqueId());
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_chance_set"));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            openChanceSettings(p, seed);
        }, 1L);
    }

    public static void drop(SeedData seedData, boolean isForced) {
        if (seedData == null) {
            return;
        }
        World world = Bukkit.getWorld(seedData.getWorld());
        int x = seedData.getX();
        int y = seedData.getY();
        int z = seedData.getZ();
        if (!seedData.isGrow()) {
            ItemStack item = getSeedItem(seedData.getSeed());
            if (item != null) {
                world.dropItem(world.getBlockAt(x, y, z).getLocation(), item);
            }
            plugin.seedDataSet.remove(seedData);
            return;
        }
        YamlConfiguration data = getSeed(seedData.getSeed());
        for (int i = 0; i < 27; i++) {
            ItemStack item = data.getItemStack("Seeds." + seedData.getSeed() + ".Drops." + i + ".Item");
            int chance = data.getInt("Seeds." + seedData.getSeed() + ".Drops." + i + ".Chance");
            if (item != null && chance > 0) {
                if (Math.random() * 100 < chance) {
                    world.dropItem(world.getBlockAt(x, y, z).getLocation(), item);
                }
            }
        }
        ItemStack seed = getSeedItem(seedData.getSeed());
        if (seed != null) {
            String sdc = data.getString("Seeds." + seedData.getSeed() + ".SeedDropCount", "0-1");
            if (sdc.contains("-")) {
                String[] split = sdc.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                seed.setAmount((int) (Math.random() * (max - min + 1)) + min);
            } else {
                seed.setAmount(Integer.parseInt(sdc));
            }
            world.dropItem(world.getBlockAt(x, y, z).getLocation(), seed);
        }
        plugin.seedDataSet.remove(seedData);
        if (isForced) {
            world.getBlockAt(x, y, z).setType(Material.AIR);
        }
    }

    public static void placeItemAtLocation(World world, int x, int y, int z, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        Block targetBlock = world.getBlockAt(x, y, z);
        Material itemType = item.getType();
        if (itemType.isBlock()) {
            BlockData blockData = item.getType().createBlockData();
            targetBlock.setBlockData(blockData);
            if (targetBlock instanceof Ageable) {
                Ageable ageable = (Ageable) targetBlock.getBlockData();
                ageable.setAge(100000);
                targetBlock.setBlockData(ageable);
                return;
            }
            if (itemType == Material.PLAYER_HEAD || itemType == Material.SKELETON_SKULL || itemType == Material.ZOMBIE_HEAD || itemType == Material.CREEPER_HEAD || itemType == Material.WITHER_SKELETON_SKULL || itemType == Material.DRAGON_HEAD) {
                if (targetBlock.getState() instanceof Skull) {
                    Skull skull = (Skull) targetBlock.getState();
                    if (item.getItemMeta() instanceof SkullMeta) {
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        skull.setOwnerProfile(skullMeta.getOwnerProfile());
                        targetBlock.setBlockData(skull.getBlockData());
                        skull.update();
                    }
                }
            }
        }
    }

    public static void countSeed(CommandSender sender) {
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().getWithArgs("func_seedCount", String.valueOf(plugin.seedDataSet.size())));
    }

    public static void setLimit(CommandSender sender, String name, String limit) {
        if (!limit.matches("[0-9]+")) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_NumberFormatException"));
            return;
        }
        if (!checkSeed(name)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        YamlConfiguration data = getSeed(name);
        data.set("Seeds." + name + ".Limit", Integer.parseInt(limit));
        plugin.saveDataContainer();
        plugin.seeds.put(name, data);
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_setLimit"));
    }

    public static void initPlayer(Player p) {
        plugin.udata.put(p.getUniqueId(), ConfigUtils.initUserData(plugin, p.getUniqueId().toString()));
    }


    public static void savePlayer(Player p) {
        ConfigUtils.saveCustomData(plugin, plugin.udata.get(p.getUniqueId()), p.getUniqueId().toString(), "data");
    }

    public static void countSeedPlace(UUID uuid, String seed) {
        YamlConfiguration data = plugin.udata.get(uuid);
        int count = data.getInt("Count.Seed." + seed);
        if (count < 0) count = 0;
        data.set("Count.Seed." + seed, count + 1);
        plugin.udata.put(uuid, data);
    }

    public static void countSeedBreak(UUID uuid, String seed) {
        YamlConfiguration data = plugin.udata.get(uuid);
        int count = data.getInt("Count.Seed." + seed);
        if (count <= 0) {
            data.set("Count.Seed." + seed, 0);
        } else {
            data.set("Count.Seed." + seed, count - 1);
        }
        plugin.udata.put(uuid, data);
    }

    public static int getSeedCount(Player p, String seed) {
        YamlConfiguration data = plugin.udata.get(p.getUniqueId());
        return data.getInt("Count.Seed." + seed);
    }

    public static int getSeedLimit(String seed) {
        return getSeed(seed).getInt("Seeds." + seed + ".Limit");
    }

    public static void setWorldLimit(CommandSender sender, String seedName, String worldName) {
        if (!checkSeed(seedName)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        YamlConfiguration data = getSeed(seedName);
        List<String> worlds = data.getStringList("Seeds." + seedName + ".WorldLimit");
        if (worlds.contains(worldName)) {
            worlds.remove(worldName);
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().getWithArgs("func_worldlimit_remove", worldName));
        } else {
            worlds.add(worldName);
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().getWithArgs("func_worldlimit_add", worldName));
        }
        data.set("Seeds." + seedName + ".WorldLimit", worlds);
        ConfigUtils.saveCustomData(plugin, data, seedName, "seeds");
        plugin.seeds.put(seedName, data);
    }

    public static boolean isWorldLimit(String seed, String world) {
        if (!checkSeed(seed)) {
            return false;
        }
        YamlConfiguration data = getSeed(seed);
        List<String> worlds = data.getStringList("Seeds." + seed + ".WorldLimit");
        for(String w : worlds) {
            if (w.equalsIgnoreCase(world)) {
                return true;
            }
        }
        return false;
    }

    public static void setSeedDropCount(CommandSender sender, String name, String count) {
        if (!checkSeed(name)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        YamlConfiguration data = getSeed(name);
        data.set("Seeds." + name + ".SeedDropCount", Integer.parseInt(count));
        plugin.saveDataContainer();
        plugin.seeds.put(name, data);
        sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_setSeedDropCount"));
    }

    public static void setSeedItem(CommandSender sender, String name) {
        if (!checkSeed(name)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_seedNotExists"));
            return;
        }
        Player p = (Player) sender;
        YamlConfiguration data = getSeed(name);
        DInventory inv = new DInventory(plugin.getLang().getWithArgs("inv_seed_item_title", name), 27, plugin);
        ItemStack pane = NBT.setStringTag(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "dpcf_pane", "true");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, pane);
        }
        ItemStack item = data.getItemStack("Seeds." + name + ".ItemMaterial");
        if (item != null) {
            item.setAmount(1);
            inv.setItem(13, item);
        } else {
            inv.setItem(13, null);
        }
        Tuple<String, String> tuple = new Tuple<>("dpcf_seeditem", name);
        inv.setObj(tuple);
        p.openInventory(inv.getInventory());
    }

    public static void saveSeedItem(String name, DInventory inv, Player p) {
        ItemStack item = inv.getItem(13);
        YamlConfiguration data = getSeed(name);
        if (item == null || item.getType() == Material.AIR) {
            data.set("Seeds." + name + ".ItemMaterial", null);
        } else {
            data.set("Seeds." + name + ".ItemMaterial", item);
        }
        plugin.saveDataContainer();
        plugin.seeds.put(name, data);
        p.sendMessage(plugin.getPrefix() + plugin.getLang().get("func_cmd_seedItemSaved"));
    }
}
