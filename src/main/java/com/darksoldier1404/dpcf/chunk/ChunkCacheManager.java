package com.darksoldier1404.dpcf.chunk;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;

public class ChunkCacheManager implements Listener {
        private static final Map<String, Boolean> chunkCache = new HashMap<>();

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            Chunk chunk = event.getChunk();
            String key = generateChunkKey(chunk.getWorld(), chunk.getX(), chunk.getZ());
            chunkCache.put(key, true);
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            Chunk chunk = event.getChunk();
            String key = generateChunkKey(chunk.getWorld(), chunk.getX(), chunk.getZ());
            chunkCache.remove(key);
        }

        private static String generateChunkKey(World world, int x, int z) {
            return world.getName() + ":" + (x >> 4) + ":" + (z >> 4);
        }

        public static boolean isChunkLoaded(World world, int x, int z) {
            String key = generateChunkKey(world, x, z);

            if (chunkCache.containsKey(key)) {
                return chunkCache.get(key);
            }

            boolean isLoaded = world.isChunkLoaded(x >> 4, z >> 4);
            chunkCache.put(key, isLoaded);
            return isLoaded;
        }
}
