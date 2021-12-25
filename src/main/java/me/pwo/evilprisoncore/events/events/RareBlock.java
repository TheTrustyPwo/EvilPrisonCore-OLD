package me.pwo.evilprisoncore.events.events;

import me.lucko.helper.Commands;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text3.Text;
import me.lucko.helper.utils.Players;
import me.pwo.evilprisoncore.events.Events;
import me.pwo.evilprisoncore.utils.PlayerUtils;
import me.pwo.evilprisoncore.utils.TextUtils;
import me.pwo.evilprisoncore.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class RareBlock {
    private final Events events;
    private long blocksBroken;
    private long blocksRequired;
    private final Map<String, List<String>> rewards = new HashMap<>();
    private int totalRareBlocksFound= 0;
    private final List<UUID> playersWhoFoundRareBlocks = new ArrayList<>();
    private final Task eventTask;
    private Task sendTitleTask;
    private boolean active;

    public RareBlock(Events events) {
        this.events = events;
        loadRewards();
        this.active = false;
        this.eventTask = Schedulers.async().runRepeating(this::start, 10L, TimeUnit.SECONDS, 10L, TimeUnit.MINUTES);
        me.lucko.helper.Events.subscribe(BlockBreakEvent.class)
                .filter(e -> this.active)
                .filter(e -> (e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.events.getPlugin().isPickaxeSupported(e.getPlayer().getItemInHand().getType())))
                .filter(e -> (WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch((region) -> region.getId().toLowerCase().startsWith("mine-"))))
                .handler(e -> {
                    this.blocksBroken++;
                    if (this.blocksBroken >= blocksRequired) {
                        reward(e.getPlayer());
                        this.totalRareBlocksFound++;
                        if (!(this.playersWhoFoundRareBlocks.contains(e.getPlayer().getUniqueId())))
                            this.playersWhoFoundRareBlocks.add(e.getPlayer().getUniqueId());
                        this.blocksBroken = 0L;
                    }
                }).bindWith(this.events.getPlugin());
        Commands.create()
                .handler(context -> {

                }).registerAndBind(this.events.getPlugin(), "rareblock");
    }

    private void loadRewards() {
        ConfigurationSection section = this.events.getConfig().getConfigurationSection("RareBlock.Rewards");
        for (String string : section.getKeys(false)) this.rewards.put(string, section.getStringList(string));
    }

    private void start() {
        try {
            Bukkit.broadcastMessage(Text.colorize("&e&l(!) &6Rare Block &eevent will start in &e&n10s&e!"));
            TimeUnit.SECONDS.sleep(5);
            Bukkit.broadcastMessage(Text.colorize("&e&l(!) &6Rare Block &eevent will start in &e&n5s&e!"));
            TimeUnit.SECONDS.sleep(2);
            Bukkit.broadcastMessage(Text.colorize("&e&l(!) &6Rare Block &eevent will start in &e&n3s&e!"));
            TimeUnit.SECONDS.sleep(1);
            Bukkit.broadcastMessage(Text.colorize("&e&l(!) &6Rare Block &eevent will start in &e&n2s&e!"));
            TimeUnit.SECONDS.sleep(1);
            Bukkit.broadcastMessage(Text.colorize("&e&l(!) &6Rare Block &eevent will start in &e&n1s&e!"));
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.active = true;
        this.blocksRequired = this.events.getConfig().getLong("RareBlock.Blocks-Required");
        this.totalRareBlocksFound = 0;
        this.playersWhoFoundRareBlocks.clear();
        TextComponent component = new TextComponent(TextUtils.centerMessage("&7&o(( Click &f&o&nhere&7&o to see how it works! ))"));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events"));
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(TextUtils.centerMessage("&6&m-- &6&lRARE BLOCK &e&lEVENT HAS STARTED &6&m--"));
        Bukkit.broadcastMessage(TextUtils.centerMessage("&7&o(( Have a chance to mine a &f&oRare Block&7&o! ))"));
        Bukkit.spigot().broadcast(component);
        Bukkit.broadcastMessage(" ");
        this.sendTitleTask = Schedulers.async().runRepeating(() -> Players.all().forEach((player -> PlayerUtils.sendTitle(player,
                "&c&lRA&6&lRE &e&lBL&a&lOC&2&lK E&3&lVE&a&lNT",
                "&6%current% &f/ &6%required% &eBlocks"
                        .replaceAll("%current%", Utils.formatNumber(this.blocksBroken))
                        .replaceAll("%required%", Utils.formatNumber(this.blocksRequired)),
                0, 5, 0))), 0L, TimeUnit.NANOSECONDS, 50L, TimeUnit.MILLISECONDS);
        Schedulers.async().runLater(() -> {
            this.active = false;
            this.sendTitleTask.close();
            Bukkit.broadcastMessage(TextUtils.centerMessage("&6&m-- &6&lRARE BLOCK &e&lEVENT HAS ENDED &6&m--"));
            Bukkit.broadcastMessage(TextUtils.centerMessage("&eTotal rare blocks found: &6%amount% &a(%players% Players)"
                    .replaceAll("%amount%", Utils.formatNumber(this.totalRareBlocksFound))
                    .replaceAll("%players%", Utils.formatNumber(this.playersWhoFoundRareBlocks.size()))));
        }, 5L, TimeUnit.MINUTES);
    }

    private void reward(Player player) {
        List<String> list = new ArrayList<>(this.rewards.keySet());
        String rewardName = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        List<String> commands = this.rewards.get(rewardName);
        commands.forEach((cmd) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", player.getName())));
        Bukkit.broadcastMessage(TextUtils.centerMessage("&c&m---&6&m---&e&m---&a&m---&2&m---&3&m---&a&m---&5&m---&d&m---"));
        Bukkit.broadcastMessage(TextUtils.centerMessage("&c&lRA&6&lRE &e&lBL&a&lOC&2&lK F&3&lOU&a&lND"));
        Bukkit.broadcastMessage(TextUtils.centerMessage("&f%player% &7broken a rare block and received".replaceAll("%player%", player.getName())));
        Bukkit.broadcastMessage(TextUtils.centerMessage(rewardName));
        Bukkit.broadcastMessage(TextUtils.centerMessage("&c&m---&6&m---&e&m---&a&m---&2&m---&3&m---&a&m---&5&m---&d&m---"));
    }
}
