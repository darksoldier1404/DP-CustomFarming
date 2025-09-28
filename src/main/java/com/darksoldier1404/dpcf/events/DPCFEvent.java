package com.darksoldier1404.dpcf.events;

import com.darksoldier1404.dpcf.CustomFarming;
import com.darksoldier1404.dpcf.data.SeedData;
import com.darksoldier1404.dpcf.functions.DPCFFuntion;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.NBT;
import com.darksoldier1404.dppc.utils.Tuple;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static com.darksoldier1404.dpcf.CustomFarming.plugin;

public class DPCFEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DPCFFuntion.initPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DPCFFuntion.savePlayer(e.getPlayer());
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if (DPCFFuntion.isSeed(b.getWorld().getName(), b.getX(), b.getY(), b.getZ())) {
                SeedData seedData = DPCFFuntion.getSeedData(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
                if (seedData == null) return;
                if (seedData.isGrow()) {
                    DPCFFuntion.countSeedBreak(UUID.fromString(seedData.getOwner()), DPCFFuntion.getSeedData(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()).getSeed());
                    DPCFFuntion.drop(seedData, true);
                } else {
                    p.sendTitle("§a" + seedData.getSeed(), plugin.getLang().getWithArgs("event_remained_time", String.valueOf(seedData.getRemainingTime())), 10, 40, 10);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (NBT.hasTagKey(item, "dpcf_seed")) {
            String seedName = NBT.getStringTag(item, "dpcf_seed");
            Player p = e.getPlayer();
            Block b = e.getBlock();
            if (DPCFFuntion.isWorldLimit(seedName, b.getWorld().getName())) {
                e.setCancelled(true);
                p.sendMessage(plugin.getPrefix() + plugin.getLang().get("event_wrong_world"));
                return;
            }
            int limit = DPCFFuntion.getSeedLimit(seedName);
            int current = DPCFFuntion.getSeedCount(p, seedName);
            if (limit > 0 && current >= limit) {
                e.setCancelled(true);
                p.sendMessage(plugin.getPrefix() + plugin.getLang().get("event_limit_seed"));
                return;
            }
            DPCFFuntion.countSeedPlace(p.getUniqueId(), seedName);
            int remainingTime = DPCFFuntion.getSeed(seedName).getInt("Seeds." + seedName + ".SeedGrowTime");
            SeedData seedData = new SeedData(seedName, p.getUniqueId(), b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), remainingTime);
            CustomFarming.seedDataSet.add(seedData);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        int x, y, z;
        x = b.getX();
        y = b.getY();
        z = b.getZ();
        if (DPCFFuntion.isSeed(b.getWorld().getName(), x, y, z)) {
            e.setDropItems(false);
            SeedData seedData = DPCFFuntion.getSeedData(b.getWorld().getName(), x, y, z);
            DPCFFuntion.countSeedBreak(UUID.fromString(seedData.getOwner()), seedData.getSeed());
            DPCFFuntion.drop(seedData, false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof DInventory) {
            DInventory inv = (DInventory) e.getInventory().getHolder();
            if (inv.isValidHandler(CustomFarming.plugin)) {
                if (inv.getObj() instanceof Tuple) {
                    Tuple<String, String> tuple = (Tuple<String, String>) inv.getObj();
                    String option = tuple.getA();
                    if (option.equalsIgnoreCase("dpcf_chance")) return;
                    String name = tuple.getB();
                    if (option.equalsIgnoreCase("dpcf_material")) {
                        DPCFFuntion.saveSeedBlock(name, inv, (Player) e.getPlayer());
                        return;
                    }
                    if (option.equalsIgnoreCase("dpcf_seeditem")) {
                        DPCFFuntion.saveSeedItem(name, inv, (Player) e.getPlayer());
                        return;
                    }
                    DPCFFuntion.saveDrops(name, inv, (Player) e.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getInventory().getHolder() instanceof DInventory) {
            DInventory inv = (DInventory) e.getInventory().getHolder();
            if (inv.isValidHandler(CustomFarming.plugin)) {
                if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;
                ItemStack item = e.getCurrentItem();
                if (NBT.hasTagKey(item, "dpcf_pane")) {
                    e.setCancelled(true);
                    return;
                }
                if (inv.getObj() instanceof Tuple) {
                    Tuple<String, String> tuple = (Tuple<String, String>) inv.getObj();
                    String option = tuple.getA();
                    if (option.equalsIgnoreCase("dpcf_chance")) {
                        e.setCancelled(true);
                        DPCFFuntion.setChanceWithChat(p, tuple.getB(), e.getSlot());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (CustomFarming.chanceSet.containsKey(p.getUniqueId())) {
            e.setCancelled(true);
            DPCFFuntion.setChance(p, e.getMessage(), CustomFarming.chanceSet.get(p.getUniqueId()).getB(), CustomFarming.chanceSet.get(p.getUniqueId()).getA());
        }
    }

    @EventHandler
    public void onBlockPhysic(BlockPhysicsEvent e) {
        if (DPCFFuntion.isSeed(e.getBlock().getWorld().getName(), e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreakByWater(BlockFromToEvent e) {
        Block b = e.getToBlock();
        int x, y, z;
        x = b.getX();
        y = b.getY();
        z = b.getZ();
        if (DPCFFuntion.isSeed(b.getWorld().getName(), x, y, z)) {
            if(e.getBlock().getType() == org.bukkit.Material.WATER) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBukkit(PlayerBucketEmptyEvent e) {
        Block b = e.getBlock();
        int x, y, z;
        x = b.getX();
        y = b.getY();
        z = b.getZ();
        if (DPCFFuntion.isSeed(b.getWorld().getName(), x, y, z)) {
            e.setCancelled(true);
        }
    }
}
