package me.pwo.evilprisoncore.multipliers.model;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierSource;
import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;

import java.util.concurrent.TimeUnit;

public class Multiplier {
    protected final Integer id;
    protected double multiplier;
    protected long startTime;
    protected long endTime = 0L;
    protected MultiplierType multiplierType;
    protected MultiplierSource multiplierSource;
    protected boolean isPermanent;

    public Integer getId() {
        return id;
    }

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

    public MultiplierSource getMultiplierSource() {
        return multiplierSource;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public Multiplier(Integer id, double multiplier, long endTime, MultiplierType multiplierType, MultiplierSource multiplierSource) {
        this.id = id;
        this.multiplier = multiplier;
        this.startTime = System.currentTimeMillis();
        addDuration(TimeUnit.SECONDS, endTime);
        this.isPermanent = endTime == -1L;
        this.multiplierType = multiplierType;
        this.multiplierSource = multiplierSource;
    }

    public String getTimeLeftString() {
        if (this.isPermanent) return "&6Permanent";
        if (System.currentTimeMillis() > this.endTime)
            return "&cExpired";
        long l1 = this.endTime - System.currentTimeMillis();
        long l2 = l1 / 86400000L;
        l1 -= l2 * 86400000L;
        long l3 = l1 / 3600000L;
        l1 -= l3 * 3600000L;
        long l4 = l1 / 60000L;
        l1 -= l4 * 60000L;
        long l5 = l1 / 1000L;
        return "&f" + l2 + "d " + l3 + "h " + l4 + "m " + l5 + "s" + "";
    }

    public void setMultiplier(double paramDouble1) {
        this.multiplier = paramDouble1;
    }

    public void addMultiplier(double paramDouble1) {
        this.multiplier += paramDouble1;
    }

    public boolean isExpired() {
        if (this.isPermanent) return false;
        return (System.currentTimeMillis() > this.endTime);
    }

    public void setEndTime(long paramLong) {
        this.startTime = System.currentTimeMillis();
        this.endTime = paramLong;
        this.isPermanent = this.endTime == -1L;
    }

    public void addDuration(TimeUnit paramTimeUnit, long paramInt) {
        this.startTime = System.currentTimeMillis();
        this.endTime = (this.endTime == 0L) ? (System.currentTimeMillis() + paramTimeUnit.toMillis(paramInt)) : (this.endTime + paramTimeUnit.toMillis(paramInt));
    }

    public void reset() {
        this.multiplier = 0.0D;
        this.startTime = 0L;
        this.endTime = 0L;
    }
}
