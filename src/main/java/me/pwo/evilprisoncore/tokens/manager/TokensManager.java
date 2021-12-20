package me.pwo.evilprisoncore.tokens.manager;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.tokens.Tokens;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class TokensManager {
    private static final String tokenItemNBTTagIdentifier = "EvilPrison-Tokens-Item-Value";
    private final Tokens tokens;
    private static final List<String> tokensTopFormat = Arrays.asList(
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
            "&e&lTOKENS TOP",
            "{FOR_EACH_PLAYER} &f&l#%position%. &e%player% &8&7%tokens% Tokens",
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"
    );
    private final HashMap<UUID, Long> tokensCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Tokens = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public TokensManager(Tokens tokens) {
        this.tokens = tokens;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.tokens.getPlugin().getPluginDatabase().addIntoTokens(e.getPlayer());
                    this.tokensCache.put(e.getPlayer().getUniqueId(),
                            this.tokens.getPlugin().getPluginDatabase().getPlayerTokens(e.getPlayer()));
                })).bindWith(tokens.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerData(e.getPlayer(), true, true)).bindWith(tokens.getPlugin());
        loadPlayerDataOnEnable();
        updateTokensTop();
    }

    public void stopUpdating() {
        this.tokens.getPlugin().getLogger().info("Stopping updating Top 10");
        this.task.close();
    }

    private void updateTokensTop() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            Players.all().forEach((player) -> this.savePlayerData(player, false, false));
            this.top10Tokens = new LinkedHashMap<>();
            this.tokens.getPlugin().debug("Starting updating TokensTop");
            this.top10Tokens = (LinkedHashMap<UUID, Long>) this.tokens.getPlugin().getPluginDatabase().getTop10Tokens();
            this.tokens.getPlugin().debug("TokensTop updated!");
            this.updating = false;
        }, 30L, TimeUnit.SECONDS, 10, TimeUnit.MINUTES);
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean runAsync) {
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(player, this.tokensCache.getOrDefault(player.getUniqueId(), 0L));
                if (removeFromCache) this.tokensCache.remove(player.getUniqueId());
            });
        } else {
            this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(player, this.tokensCache.getOrDefault(player.getUniqueId(), 0L));
            if (removeFromCache) this.tokensCache.remove(player.getUniqueId());
        }
    }

    public void savePlayerDataOnDisable() {
        Schedulers.sync().run(() -> {
            for (UUID uuid : this.tokensCache.keySet())
                this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(Players.getOfflineNullable(uuid), this.tokensCache.get(uuid));
            this.tokensCache.clear();
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(this::loadPlayerData);
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> this.tokensCache.put(player.getUniqueId(), this.tokens.getPlugin().getPluginDatabase().getPlayerTokens(player)));
    }

    public void setTokens(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            if (player.isOnline()) this.tokensCache.put(player.getUniqueId(), amount);
            else this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(player, amount);
        });
    }

    public void giveTokens(OfflinePlayer player, long amount, boolean applyMultiplier) {
        Schedulers.async().run(() -> {
            long playerTokens = getPlayerTokens(player);
            // boolean multiEnabled = this.tokens.getPlugin().isModuleEnabled("Multipliers");
            // if (multiEnabled && player.isOnline() && applyMultiplier)
            //     amount = (long)this.tokens.getPlugin().getMultipliers().getApi().getTotalToDeposit((Player)player, amount, MultiplierType.TOKENS);
            if (player.isOnline())
                this.tokensCache.replace(player.getUniqueId(), this.tokensCache.getOrDefault(player.getUniqueId(), 0L) + amount);
            else this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(player, amount + playerTokens);
        });
    }

    public void removeTokens(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long newAmount = getPlayerTokens(player) - amount;
            if (player.isOnline()) this.tokensCache.put(player.getUniqueId(), newAmount);
            else this.tokens.getPlugin().getPluginDatabase().updatePlayerTokens(player, amount);
        });
    }

    public void withdrawTokens(Player player, long value, int amount) {
        Schedulers.async().run(() -> {
            long total = value * amount;
            removeTokens(player, total);
            player.getInventory().addItem(createTokenItem(value, amount));
        });
    }

    public void redeemTokens(Player player, ItemStack itemStack, boolean claimAll) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey(tokenItemNBTTagIdentifier)) {
            long value = nbtItem.getLong(tokenItemNBTTagIdentifier);
            int amount = itemStack.getAmount();
            int newAmount = claimAll ? 0 : (itemStack.getAmount() == 1 ? 0 : itemStack.getAmount() - 1);
            if (newAmount == 0) player.setItemInHand(null);
            else itemStack.setAmount(newAmount);
            giveTokens(player, value * amount, false);
            PlayerUtils.sendMessage(player, "&e&l(!) &eYou redeemed &f%tokens% &etoken(s)"
                    .replace("%tokens%", String.valueOf(value * amount)));
        }
    }

    public long getPlayerTokens(OfflinePlayer player) {
        if (!player.isOnline()) return this.tokens.getPlugin().getPluginDatabase().getPlayerTokens(player);
        return this.tokensCache.getOrDefault(player.getUniqueId(), 0L);
    }

    private ItemStack createTokenItem(long value, int amount) {
        ItemStack itemStack = ItemStackBuilder.of(Material.MAGMA_CREAM)
                .amount(amount)
                .name("&e&l%tokens% TOKENS".replace("%tokens%", String.valueOf(value)))
                .lore(Arrays.asList("&7Right-Click to Redeem"))
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS).build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setLong(tokenItemNBTTagIdentifier, value);
        return nbtItem.getItem();
    }

    public void sendTokensTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, "&c&lLeaderboard is currently updating...");
            return;
        }
        for (String str : this.tokensTopFormat) {
            if (str.startsWith("{FOR_EACH_PLAYER}")) {
                str = str.replace("{FOR_EACH_PLAYER} ", "");
                for (byte position = 0; position < 10; position++) {
                    try {
                        String player;
                        UUID uUID = (UUID)this.top10Tokens.keySet().toArray()[position];
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(uUID);
                        if (offlinePlayer == null) {
                            player = "Unknown Player";
                        } else {
                            player = offlinePlayer.getName();
                        }
                        long tokens = this.top10Tokens.get(uUID);
                        PlayerUtils.sendMessage(sender, str.replace("%position%", String.valueOf(position + 1))
                                .replace("%player%", player)
                                .replace("%tokens%", String.valueOf(tokens)));
                    } catch (Exception exception) {
                        break;
                    }
                }
                continue;
            }
            PlayerUtils.sendMessage(sender, str);
        }
    }

    public void sendInfoMessage(CommandSender sender, OfflinePlayer player) {
        Schedulers.async().run(() -> {
            if (sender == player) {
                PlayerUtils.sendMessage(sender, "&eYour Tokens: %tokens%"
                        .replace("%tokens%", String.valueOf(getPlayerTokens(player))));
            } else {
                PlayerUtils.sendMessage(sender, "&e%player%'s Tokens: %tokens%"
                        .replace("%tokens%", String.valueOf(getPlayerTokens(player)))
                        .replace("%player%", player.getName()));
            }
        });
    }
}
