package com.darksoldier1029.dpcf.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.UUID;

public class SeedData {
    private final String uuid;
    private final String owner;
    private final String seed;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private boolean isGrow = false;
    private int remainingTime;
    private boolean isBlockedGrowByUnloadedChunk = false;
    private boolean isRightClickBreak = false;

    public SeedData(String uuid, UUID owner, String seed, String world, int x, int y, int z, boolean isGrow, int remainingTime, boolean isBlockedGrowByUnloadedChunk, boolean isRightClickBreak) {
        this.uuid = uuid;
        this.owner = owner.toString();
        this.seed = seed.intern();
        this.world = world.intern();
        this.x = x;
        this.y = y;
        this.z = z;
        this.isGrow = isGrow;
        this.remainingTime = remainingTime;
        this.isBlockedGrowByUnloadedChunk = isBlockedGrowByUnloadedChunk;
        this.isRightClickBreak = isRightClickBreak;
    }

    public SeedData(String seed, UUID owner, String world, int x, int y, int z, int remainingTime) {
        uuid = UUID.randomUUID().toString();
        this.owner = owner.toString();
        this.seed = seed.intern();
        this.world = world.intern();
        this.x = x;
        this.y = y;
        this.z = z;
        this.remainingTime = remainingTime;
    }

    public String getUUID() {
        return uuid;
    }

    public String getOwner() {
        return owner;
    }

    public String getSeed() {
        return seed;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isGrow() {
        return isGrow;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public boolean isBlockedGrowByUnloadedChunk() {
        return isBlockedGrowByUnloadedChunk;
    }

    public boolean isRightClickBreak() {
        return isRightClickBreak;
    }

    public void setGrow(boolean grow) {
        isGrow = grow;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setBlockedGrowByUnloadedChunk(boolean blockedGrowByUnloadedChunk) {
        isBlockedGrowByUnloadedChunk = blockedGrowByUnloadedChunk;
    }

    public void setRightClickBreak(boolean rightClickBreak) {
        isRightClickBreak = rightClickBreak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeedData seedData = (SeedData) o;
        return uuid.equals(seedData.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public void serialize(ConfigurationSection section) {
        section.set("Seed", this.seed);
        section.set("UUID", this.uuid);
        section.set("Owner", this.owner);
        section.set("World", this.world);
        section.set("X", this.x);
        section.set("Y", this.y);
        section.set("Z", this.z);
        section.set("isGrow", this.isGrow);
        section.set("RemainingTime", this.remainingTime);
        section.set("isBlockedGrowByUnloadedChunk", this.isBlockedGrowByUnloadedChunk);
        section.set("isRightClickBreak", this.isRightClickBreak);
    }
}
