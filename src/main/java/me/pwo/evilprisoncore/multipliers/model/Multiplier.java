package me.pwo.evilprisoncore.multipliers.model;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;

import java.util.concurrent.TimeUnit;

public class Multiplier {
    protected double multiplier;
    protected long startTime;
    protected long endTime;
    protected MultiplierType multiplierType;

    public double getMultiplier() {
        return this.multiplier;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public MultiplierType getMultiplierType() {
        return this.multiplierType;
    }

    public Multiplier(double multiplier, long endTime, MultiplierType multiplierType) {
        this.multiplier = multiplier;
        this.startTime = System.currentTimeMillis();
        this.endTime = endTime;
        this.multiplierType = multiplierType;
    }

    public String getTimeLeftString() {
        if (System.currentTimeMillis() > this.endTime)
            return "";
        long l1 = this.endTime - System.currentTimeMillis();
        long l2 = l1 / 86400000L;
        l1 -= l2 * 86400000L;
        long l3 = l1 / 3600000L;
        l1 -= l3 * 3600000L;
        long l4 = l1 / 60000L;
        l1 -= l4 * 60000L;
        long l5 = l1 / 1000L;
        return "&7(&f" + l2 + "d " + l3 + "h " + l4 + "m " + l5 + "s" + "&7)";
    }

    public void setMultiplier(double paramDouble1) {
        this.multiplier = paramDouble1;
    }

    public void addMultiplier(double paramDouble1) {
        this.multiplier += paramDouble1;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() > this.endTime);
    }

    public void setEndTime(long paramLong) {
        this.startTime = System.currentTimeMillis();
        this.endTime = paramLong;
    }

    public void addDuration(TimeUnit paramTimeUnit, int paramInt) {
        this.startTime = System.currentTimeMillis();
        this.endTime = (this.endTime == 0L) ? (System.currentTimeMillis() + paramTimeUnit.toMillis(paramInt)) : (this.endTime + paramTimeUnit.toMillis(paramInt));
    }

    public void reset() {
        this.multiplier = 0.0D;
        this.startTime = 0L;
        this.endTime = 0L;
    }
}
