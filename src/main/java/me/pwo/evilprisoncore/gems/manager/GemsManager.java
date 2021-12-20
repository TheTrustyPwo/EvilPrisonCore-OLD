package me.pwo.evilprisoncore.gems.manager;

import de.tr7zw.nbtapi.NBTItem;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.gems.Gems;
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
public class GemsManager {
    private static final String tokenItemNBTTagIdentifier = "EvilPrison-Gems-Item-Value";
    private final Gems gems;
    private static final List<String> gemsTopFormat = Arrays.asList(
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
            "&e&lGEMS TOP",
            "{FOR_EACH_PLAYER} &f&l#%position%. &e%player% &8&7%gems% Gems",
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"
    );
    private final HashMap<UUID, Long> gemsCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Gems = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public GemsManager(Gems gems) {
        this.gems = gems;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.gems.getPlugin().getPluginDatabase().addIntoGems(e.getPlayer());
                    this.gemsCache.put(e.getPlayer().getUniqueId(),
                            this.gems.getPlugin().getPluginDatabase().getPlayerGems(e.getPlayer()));
                })).bindWith(gems.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerData(e.getPlayer(), true, true)).bindWith(gems.getPlugin());
        loadPlayerDataOnEnable();
        updateGemsTop();
    }

    public void stopUpdating() {
        this.gems.getPlugin().getLogger().info("Stopping updating Top 10");
        this.task.close();
    }

    private void updateGemsTop() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            Players.all().forEach((player) -> this.savePlayerData(player, false, false));
            this.top10Gems = new LinkedHashMap<>();
            this.gems.getPlugin().debug("Starting updating GemsTop");
            this.top10Gems = (LinkedHashMap<UUID, Long>) this.gems.getPlugin().getPluginDatabase().getTop10Gems();
            this.gems.getPlugin().debug("GemsTop updated!");
            this.updating = false;
        }, 30L, TimeUnit.SECONDS, 10, TimeUnit.MINUTES);
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean runAsync) {
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.gems.getPlugin().getPluginDatabase().updatePlayerGems(player, this.gemsCache.getOrDefault(player.getUniqueId(), 0L));
                if (removeFromCache) this.gemsCache.remove(player.getUniqueId());
            });
        } else {
            this.gems.getPlugin().getPluginDatabase().updatePlayerGems(player, this.gemsCache.getOrDefault(player.getUniqueId(), 0L));
            if (removeFromCache) this.gemsCache.remove(player.getUniqueId());
        }
    }

    public void savePlayerDataOnDisable() {
        Schedulers.sync().run(() -> {
            for (UUID uuid : this.gemsCache.keySet())
                this.gems.getPlugin().getPluginDatabase().updatePlayerGems(Players.getOfflineNullable(uuid), this.gemsCache.get(uuid));
            this.gemsCache.clear();
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(this::loadPlayerData);
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> this.gemsCache.put(player.getUniqueId(), this.gems.getPlugin().getPluginDatabase().getPlayerGems(player)));
    }

    public void setGems(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            if (player.isOnline()) this.gemsCache.put(player.getUniqueId(), amount);
            else this.gems.getPlugin().getPluginDatabase().updatePlayerGems(player, amount);
        });
    }

    public void giveGems(OfflinePlayer player, long amount, boolean applyMultiplier) {
        Schedulers.async().run(() -> {
            long playerGems = getPlayerGems(player);
            // boolean multiEnabled = this.gems.getPlugin().isModuleEnabled("Multipliers");
            // if (multiEnabled && player.isOnline() && applyMultiplier)
            //     amount = (long)this.gems.getPlugin().getMultipliers().getApi().getTotalToDeposit((Player)player, amount, MultiplierType.GEMS);
            if (player.isOnline())
                this.gemsCache.replace(player.getUniqueId(), this.gemsCache.getOrDefault(player.getUniqueId(), 0L) + amount);
            else this.gems.getPlugin().getPluginDatabase().updatePlayerGems(player, amount + playerGems);
        });
    }

    public void removeGems(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long newAmount = getPlayerGems(player) - amount;
            if (player.isOnline()) this.gemsCache.put(player.getUniqueId(), newAmount);
            else this.gems.getPlugin().getPluginDatabase().updatePlayerGems(player, amount);
        });
    }

    public void withdrawGems(Player player, long value, int amount) {
        Schedulers.async().run(() -> {
            long total = value * amount;
            removeGems(player, total);
            player.getInventory().addItem(createTokenItem(value, amount));
        });
    }

    public void redeemGems(Player player, ItemStack itemStack, boolean claimAll) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey(tokenItemNBTTagIdentifier)) {
            long value = nbtItem.getLong(tokenItemNBTTagIdentifier);
            int amount = itemStack.getAmount();
            int newAmount = claimAll ? 0 : (itemStack.getAmount() == 1 ? 0 : itemStack.getAmount() - 1);
            if (newAmount == 0) player.setItemInHand(null);
            else itemStack.setAmount(newAmount);
            giveGems(player, value * amount, false);
            PlayerUtils.sendMessage(player, "&e&l(!) &eYou redeemed &f%gems% &etoken(s)"
                    .replace("%gems%", String.valueOf(value * amount)));
        }
    }

    public long getPlayerGems(OfflinePlayer player) {
        if (!player.isOnline()) return this.gems.getPlugin().getPluginDatabase().getPlayerGems(player);
        return this.gemsCache.getOrDefault(player.getUniqueId(), 0L);
    }

    private ItemStack createTokenItem(long value, int amount) {
        ItemStack itemStack = ItemStackBuilder.of(Material.MAGMA_CREAM)
                .amount(amount)
                .name("&e&l%gems% GEMS".replace("%gems%", String.valueOf(value)))
                .lore(Arrays.asList("&7Right-Click to Redeem"))
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS).build();
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setLong(tokenItemNBTTagIdentifier, value);
        return nbtItem.getItem();
    }

    public void sendGemsTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, "&c&lLeaderboard is currently updating...");
            return;
        }
        for (String str : gemsTopFormat) {
            if (str.startsWith("{FOR_EACH_PLAYER}")) {
                str = str.replace("{FOR_EACH_PLAYER} ", "");
                for (byte position = 0; position < 10; position++) {
                    try {
                        String player;
                        UUID uUID = (UUID)this.top10Gems.keySet().toArray()[position];
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(uUID);
                        if (offlinePlayer == null) {
                            player = "Unknown Player";
                        } else {
                            player = offlinePlayer.getName();
                        }
                        long gems = this.top10Gems.get(uUID);
                        PlayerUtils.sendMessage(sender, str.replace("%position%", String.valueOf(position + 1))
                                .replace("%player%", player)
                                .replace("%gems%", String.valueOf(gems)));
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
                PlayerUtils.sendMessage(sender, "&eYour Gems: %gems%"
                        .replace("%gems%", String.valueOf(getPlayerGems(player))));
            } else {
                PlayerUtils.sendMessage(sender, "&e%player%'s Gems: %gems%"
                        .replace("%gems%", String.valueOf(getPlayerGems(player)))
                        .replace("%player%", player.getName()));
            }
        });
    }
}