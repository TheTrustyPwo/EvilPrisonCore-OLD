package me.pwo.evilprisoncore.multipliers.model;

import me.pwo.evilprisoncore.multipliers.enums.MultiplierType;

public class RankMultiplier {
    protected final double moneyMulti;
    protected final double tokensMulti;
    protected final double gemsMulti;
    protected final double expMulti;

    public RankMultiplier(double moneyMulti, double tokensMulti, double gemsMulti, double expMulti) {
        this.moneyMulti = moneyMulti;
        this.tokensMulti = tokensMulti;
        this.gemsMulti = gemsMulti;
        this.expMulti = expMulti;
    }

    public double getMoneyMulti() {
        return moneyMulti;
    }

    public double getTokensMulti() {
        return tokensMulti;
    }

    public double getGemsMulti() {
        return gemsMulti;
    }

    public double getExpMulti() {
        return expMulti;
    }

    public double getMulti(MultiplierType multiplierType) {
        switch (multiplierType) {
            case MONEY: return getMoneyMulti();
            case TOKENS: return getTokensMulti();
            case GEMS: return getGemsMulti();
            case EXP: return getExpMulti();
        } return 0.0D;
    }
}
