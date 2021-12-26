package me.pwo.evilprisoncore.multipliers.model;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;

public class MultiplierSet {
    protected Multiplier moneyMulti;
    protected Multiplier tokenMulti;
    protected Multiplier gemsMulti;
    protected Multiplier expMulti;

    public MultiplierSet(Multiplier moneyMulti, Multiplier tokenMulti, Multiplier gemsMulti, Multiplier expMulti) {
        this.moneyMulti = moneyMulti;
        this.tokenMulti = tokenMulti;
        this.gemsMulti = gemsMulti;
        this.expMulti = expMulti;
    }

    public Multiplier getMulti(MultiplierType multiplierType) {
        switch (multiplierType) {
            case MONEY:
                return getMoneyMulti();
            case TOKENS:
                return getTokenMulti();
            case GEMS:
                return getGemsMulti();
            case EXP:
                return getExpMulti();
            default:
                return null;
        }
    }

    public Multiplier getMoneyMulti() {
        return moneyMulti;
    }

    public void setMoneyMulti(Multiplier moneyMulti) {
        this.moneyMulti = moneyMulti;
    }

    public Multiplier getTokenMulti() {
        return tokenMulti;
    }

    public void setTokenMulti(Multiplier tokenMulti) {
        this.tokenMulti = tokenMulti;
    }

    public Multiplier getGemsMulti() {
        return gemsMulti;
    }

    public void setGemsMulti(Multiplier gemsMulti) {
        this.gemsMulti = gemsMulti;
    }

    public Multiplier getExpMulti() {
        return expMulti;
    }

    public void setExpMulti(Multiplier expMulti) {
        this.expMulti = expMulti;
    }
}
