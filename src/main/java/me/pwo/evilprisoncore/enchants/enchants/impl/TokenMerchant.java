package me.pwo.evilprisoncore.enchants.enchants.impl;

import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilPrisonEnchantment;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class TokenMerchant extends EvilPrisonEnchantment {
    private double chance;
    private long minTokens;
    private long maxTokens;

    public TokenMerchant(Enchants enchants) {
        super(enchants, 11);
    }

    @Override
    public void onEquip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onUnequip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level, double random) {
        if (this.chance * level >= random) {
            long amount = ThreadLocalRandom.current().nextLong(this.minTokens, this.maxTokens);
            Tokens.getInstance().getApi().addTokens(e.getPlayer(), amount, true);
            PlayerUtils.sendMessage(e.getPlayer(), "&e&l+ %amount% &6&lTokens &7(Token Merchant)"
                    .replaceAll("%amount%", Utils.formatNumber(amount, 1)), false);
        }
    }

    @Override
    public void reload() {
        this.chance = this.enchants.getConfig().getDouble("enchants." + this.id + ".Chance");
        this.minTokens = this.enchants.getConfig().getLong("enchants." + this.id + ".Min-Tokens");
        this.maxTokens = this.enchants.getConfig().getLong("enchants." + this.id + ".Max-Tokens");
    }
}
