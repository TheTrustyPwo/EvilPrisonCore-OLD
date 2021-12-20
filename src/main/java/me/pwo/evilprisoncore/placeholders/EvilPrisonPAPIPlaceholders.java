package me.pwo.evilprisoncore.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pwo.evilprisoncore.EvilPrisonCore;
import me.pwo.evilprisoncore.ranks.model.RankModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class EvilPrisonPAPIPlaceholders extends PlaceholderExpansion {
    private final EvilPrisonCore plugin;

    public EvilPrisonPAPIPlaceholders(EvilPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "evilprison";
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        RankModel rank;
        if (player == null) return null;
        switch (placeholder.toLowerCase()) {
            case "tokens":
                return String.valueOf(this.plugin.getTokens().getTokensManager().getPlayerTokens(player));
            case "tokens_2":
                return String.format("%,d", this.plugin.getTokens().getTokensManager().getPlayerTokens(player));
            case "tokens_3":
                return formatNumber(this.plugin.getTokens().getTokensManager().getPlayerTokens(player));
            case "rank":
                return this.plugin.getRanks().getApi().getPlayerRank(player).getPrefix();
            case "next_rank":
                rank = this.plugin.getRanks().getApi().getNextPlayerRank(player);
                return (rank == null) ? "" : rank.getPrefix();
            case "next_rank_cost_raw":
                return String.valueOf(this.plugin.getRanks().getRankManager().getNextRankCost(player));
            case "next_rank_cost":
                return String.format("%,.2f", this.plugin.getRanks().getRankManager().getNextRankCost(player));
            case "next_rank_cost_formatted":
                return formatNumber(this.plugin.getRanks().getRankManager().getNextRankCost(player));
            case "rankup_progress":
                return String.format("%d%%", this.plugin.getRanks().getRankManager().getRankUpProgress(player));
        }
        return null;
    }

    static String formatNumber(double number) {
        if (number <= 1000.0D)
            return String.valueOf(number);
        ArrayList<String> arrayList = (ArrayList<String>) Arrays.asList("", "k", "M", "B", "T", "Q", "Qu", "S", "SP", "O", "N", "D" );
        double d = Math.floor(Math.floor(Math.log10(number) / 3.0D));
        number /= Math.pow(10.0D, d * 3.0D - 1.0D);
        number /= 10.0D;
        String str1 = arrayList.get((int)d);
        String str2 = String.valueOf(number);
        if (str2.replace(".", "").length() > 5)
            str2 = str2.substring(0, 5);
        return str2 + str1;
    }
}
