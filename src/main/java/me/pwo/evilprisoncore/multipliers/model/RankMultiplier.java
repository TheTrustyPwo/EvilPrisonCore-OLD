package me.pwo.evilprisoncore.multipliers.model;

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
}
