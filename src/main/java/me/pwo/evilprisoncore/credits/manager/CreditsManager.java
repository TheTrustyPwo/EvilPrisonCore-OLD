package me.pwo.evilprisoncore.credits.manager;

import de.tr7zw.nbtapi.NBTItem;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.credits.Credits;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CreditsManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final String CREDIT_ITEM_NBT_TAG_IDENTIFIER = "EvilPrison-Credits-Item-Value";
    private final Credits credits;
    private static final List<String> creditsTopFormat = Arrays.asList(
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------",
            "&e&lCREDITS TOP",
            "{FOR_EACH_PLAYER} &f&l#%position%. &e%player% &8&7%credits% Credits",
            "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"
    );
    private final HashMap<UUID, Long> creditsCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Credits = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public CreditsManager(Credits credits) {
        this.credits = credits;
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().run(() -> {
                    this.credits.getPlugin().getPluginDatabase().addIntoCredits(e.getPlayer());
                    this.creditsCache.put(e.getPlayer().getUniqueId(),
                            this.credits.getPlugin().getPluginDatabase().getPlayerCredits(e.getPlayer()));
                })).bindWith(credits.getPlugin());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> savePlayerData(e.getPlayer(), true, true)).bindWith(credits.getPlugin());
        loadPlayerDataOnEnable();
        updateCreditsTop();
    }

    public void stopUpdating() {
        this.credits.getPlugin().getLogger().info("Stopping updating Top 10");
        this.task.close();
    }

    private void updateCreditsTop() {
        this.updating = true;
        this.task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            Players.all().forEach((player) -> this.savePlayerData(player, false, false));
            this.top10Credits = new LinkedHashMap<>();
            this.top10Credits = (LinkedHashMap<UUID, Long>) this.credits.getPlugin().getPluginDatabase().getTop10Credits();
            this.updating = false;
        }, 30L, TimeUnit.SECONDS, 10, TimeUnit.MINUTES);
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean runAsync) {
        if (runAsync) {
            Schedulers.async().run(() -> {
                this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(player, this.creditsCache.getOrDefault(player.getUniqueId(), 0L));
                if (removeFromCache) this.creditsCache.remove(player.getUniqueId());
            });
        } else {
            this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(player, this.creditsCache.getOrDefault(player.getUniqueId(), 0L));
            if (removeFromCache) this.creditsCache.remove(player.getUniqueId());
        }
    }

    public void savePlayerDataOnDisable() {
        Schedulers.sync().run(() -> {
            for (UUID uuid : this.creditsCache.keySet())
                this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(Players.getOfflineNullable(uuid), this.creditsCache.get(uuid));
            this.creditsCache.clear();
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(this::loadPlayerData);
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> this.creditsCache.put(player.getUniqueId(), this.credits.getPlugin().getPluginDatabase().getPlayerCredits(player)));
    }

    public void setCredits(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            if (player.isOnline()) this.creditsCache.put(player.getUniqueId(), amount);
            else this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(player, amount);
        });
    }

    public void giveCredits(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long playerCredits = getPlayerCredits(player);
            if (player.isOnline())
                this.creditsCache.replace(player.getUniqueId(), this.creditsCache.getOrDefault(player.getUniqueId(), 0L) + amount);
            else this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(player, amount + playerCredits);
        });
    }

    public void removeCredits(OfflinePlayer player, long amount) {
        Schedulers.async().run(() -> {
            long newAmount = getPlayerCredits(player) - amount;
            if (player.isOnline()) this.creditsCache.put(player.getUniqueId(), newAmount);
            else this.credits.getPlugin().getPluginDatabase().updatePlayerCredits(player, amount);
        });
    }

    public void withdrawCredits(Player player, long amount) {
        Schedulers.async().run(() -> {
            removeCredits(player, amount);
            double value = Utils.round((amount *
                    this.credits.getConfig().getDouble("GiftCards-Exchange-Rate") *
                    this.credits.getCreditsManager().getPayoutMultiplier()), 2);
            JSONObject data = createGiftCard(Utils.round(value, 2), "Credits Withdrawal: " + player.getName());
            String code = data.getString("code").replaceAll("(.{4})", "$1 ").trim();
            int id = data.getInt("id");
            ItemStack itemStack = ItemStackBuilder.of(Material.PAPER)
                    .name("&8»&e»&6» &6&lEvil &e&lGiftCard &6«&e«&8«")
                    .lore(Arrays.asList(
                            "&eID: &f%id%".replaceAll("%id%", String.valueOf(id)),
                            "&eOwner: &f%player%".replaceAll("%player%", player.getName()),
                            "&eValue: &f%value%".replaceAll("%value%", String.valueOf(value)),
                            "&eCreated At: &f%time%".replaceAll("%time%", DATE_FORMAT.format(new Date())),
                            "&7&o(( Right-Click to reveal the code! ))",
                            "&7&o(( Shift Right-Click to copy! ))"))
                    .enchant(Enchantment.DURABILITY)
                    .flag(ItemFlag.HIDE_ENCHANTS).build();
            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString(CREDIT_ITEM_NBT_TAG_IDENTIFIER, code);
            player.getInventory().addItem(nbtItem.getItem());
            this.credits.getPlugin().getServer().broadcastMessage(Text.colorize("&6%player% &ehas just withdrawn &4☀&c%credits% &efor &6$%value% GiftCards&e!")
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%credits%", String.valueOf(amount))
                    .replaceAll("%value%", String.valueOf(value)));
        });
    }

    public void viewCode(Player player) {
        NBTItem nbtItem = new NBTItem(player.getItemInHand());
        if (nbtItem.hasKey(CREDIT_ITEM_NBT_TAG_IDENTIFIER)) {
            String code = nbtItem.getString(CREDIT_ITEM_NBT_TAG_IDENTIFIER);
            if (player.isSneaking()) {
                TextComponent component = new TextComponent(Text.colorize("&8»&e»&6» &6Click Me! &6«&e«&8«"));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, code));
                player.spigot().sendMessage(component);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(Text.colorize("&8»&e»&6» &6%code% &6«&e«&8«")
                                .replaceAll("%code%", code)));
            }
        }
    }

    public long getPlayerCredits(OfflinePlayer player) {
        if (!player.isOnline()) return this.credits.getPlugin().getPluginDatabase().getPlayerCredits(player);
        return this.creditsCache.getOrDefault(player.getUniqueId(), 0L);
    }

    public JSONObject createGiftCard(double amount, String note) {
        HttpResponse<JsonNode> response = Unirest.post("https://plugin.tebex.io/gift-cards")
                .header("X-Tebex-Secret", this.credits.getConfig().getString("API-Key"))
                .field("amount", String.valueOf(amount))
                .field("note", note)
                .asJson();
        return response.getBody().getObject().getJSONObject("data");
    }

    public double getPayoutMultiplier() {
        long seasonStart = this.credits.getConfig().getLong("Season-Start");
        long seasonEnd = this.credits.getConfig().getLong("Season-End");
        long startingPayoutMulti = this.credits.getConfig().getLong("Starting-Payout-Multi");
        long endingPayoutMulti = this.credits.getConfig().getLong("Ending-Payout-Multi");
        long now = Instant.now().getEpochSecond();
        return Utils.round((float) ((now - seasonStart) / (
                (seasonEnd - seasonStart) / (endingPayoutMulti - startingPayoutMulti)
        ) + startingPayoutMulti), 2);
    }

    public double getCreditsExchangeRate() {
        return Utils.round(this.credits.getConfig().getDouble("GiftCards-Exchange-Rate") * getPayoutMultiplier(), 2);
    }

    public String getSeasonAge() {
        long length = Instant.now().getEpochSecond() - this.credits.getConfig().getLong("Season-Start");
        int days = (int) TimeUnit.SECONDS.toDays(length);
        long hours = TimeUnit.SECONDS.toHours(length) -
                TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(length) -
                TimeUnit.DAYS.toMinutes(days) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.SECONDS.toSeconds(length) -
                TimeUnit.DAYS.toSeconds(days) -
                TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    public void sendCreditsTop(CommandSender sender) {
        if (this.updating) {
            PlayerUtils.sendMessage(sender, "&c&lLeaderboard is currently updating...");
            return;
        }
        for (String str : creditsTopFormat) {
            if (str.startsWith("{FOR_EACH_PLAYER}")) {
                str = str.replace("{FOR_EACH_PLAYER} ", "");
                for (byte position = 0; position < 10; position++) {
                    try {
                        String player;
                        UUID uUID = (UUID)this.top10Credits.keySet().toArray()[position];
                        OfflinePlayer offlinePlayer = Players.getOfflineNullable(uUID);
                        if (offlinePlayer == null) {
                            player = "Unknown Player";
                        } else {
                            player = offlinePlayer.getName();
                        }
                        long credits = this.top10Credits.get(uUID);
                        PlayerUtils.sendMessage(sender, str.replace("%position%", String.valueOf(position + 1))
                                .replace("%player%", player)
                                .replace("%credits%", String.valueOf(credits)));
                    } catch (Exception exception) {
                        break;
                    }
                }
                continue;
            }
            PlayerUtils.sendMessage(sender, str);
        }
    }
}
