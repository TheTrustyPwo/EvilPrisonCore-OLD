package me.pwo.evilprisoncore.blocks.blockrewards.gui;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.text3.Text;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.blocks.blockrewards.BlockRewards;
import me.pwo.evilprisoncore.blocks.blockrewards.model.BlockReward;
import me.pwo.evilprisoncore.utils.SkullUtils;
import me.pwo.evilprisoncore.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockRewardsGUI extends Gui {
    private static final MenuScheme REWARDS = new MenuScheme()
            .mask("111111111")
            .mask("000000001")
            .mask("111111111")
            .mask("100000000")
            .mask("111111111")
            .mask("000000000");
    private final int page;
    private final int maxPages;

    public BlockRewardsGUI(Player player, int page) {
        super(player, 6, Text.colorize("&8Block Rewards"));
        this.page = page;
        this.maxPages = (int) Math.ceil((float) BlockRewards.getInstance().getBlockRewards().keySet().size() / 29);
    }

    @Override
    public void redraw() {
        if (isFirstDraw())
            for (byte slot = 0; slot < getHandle().getSize(); slot++)
                setItem(slot, ItemStackBuilder.of(Material.STAINED_GLASS_PANE).data(7).buildItem().build());
        // Page Number
        setItem(49, ItemStackBuilder.of(Material.WATCH)
                .name("&e&lPage #%amount%".replaceAll("%amount%", String.valueOf(page)))
                .lore(new String[] { "&7You are on page %current%/%max%"
                        .replaceAll("%current%", String.valueOf(page))
                        .replaceAll("%max%", String.valueOf(maxPages)) })
                .buildItem().build());
        // Prev Page
        setItem(48, ItemStackBuilder.of(SkullUtils.BACK_BUTTON.clone())
                .name("&c&l<- Previous Page")
                .lore(new String[] { "&7Click to view previous page" })
                .buildItem().bind(e -> (new BlockRewardsGUI(getPlayer(), Math.max(this.page - 1, 1))).open(), ClickType.LEFT).build());
        // Next page
        setItem(50, ItemStackBuilder.of(SkullUtils.FORWARD_BUTTON.clone())
                .name("&a&lNext Page ->")
                .lore(new String[] { "&7Click to view next page" })
                .buildItem().bind(e -> (new BlockRewardsGUI(getPlayer(), Math.min(this.page + 1, maxPages))).open(), ClickType.LEFT).build());
        // Rewards
        MenuPopulator menuPopulator = REWARDS.newPopulator(this);
        long blocks = Blocks.getInstance().getApi().getPlayerBlocks(getPlayer());
        int lastClaimedTier = BlockRewards.getInstance().getBlockRewardsManager().getPlayerBlockRewardTier(getPlayer());
        for (int i = 1; i <= 29; i++) {
            int tier = i + (page - 1) * 29;
            BlockReward tierReward = BlockRewards.getInstance().getBlockRewards().getOrDefault(tier, null);
            if (tierReward == null) break;
            if (blocks >= tierReward.getBlocksRequired()) {
                // Unlocked Tier
                if (tier > lastClaimedTier) {
                    // Unclaimed
                    menuPopulator.accept(ItemStackBuilder.of(Material.NETHER_STAR).amount(tier)
                            .name("&e&lTier %tier%".replaceAll("%tier%", Utils.getRomanNumber(tier)))
                            .lore(getRewardLore(tierReward, RewardProgress.UNCLAIMED)).buildItem().bind(e -> {
                                if (!(tier > lastClaimedTier + 1)) {
                                    tierReward.runCommands(getPlayer());
                                    BlockRewards.getInstance().getBlockRewardsManager().setBlockRewardTier(getPlayer(), tier);
                                } redraw();
                            }, ClickType.LEFT).build());
                } else {
                    // Claimed
                    menuPopulator.accept(ItemStackBuilder.of(Material.INK_SACK).data(10).amount(tier)
                            .name("&a&lTier %tier%".replaceAll("%tier%", Utils.getRomanNumber(tier)))
                            .lore(getRewardLore(tierReward, RewardProgress.CLAIMED)).buildItem().build());
                }
            } else if (blocks - BlockRewards.getInstance().getBlockRewards().getOrDefault(tier - 1,
                    new BlockReward(0, null, null)).getBlocksRequired() >= 0) {
                // In progress
                menuPopulator.accept(ItemStackBuilder.of(Material.INK_SACK).data(11).amount(tier)
                        .name("&e&lTier %tier%".replaceAll("%tier%", Utils.getRomanNumber(tier)))
                        .lore(getRewardLore(tierReward, RewardProgress.IN_PROGRESS)).buildItem().build());
            } else {
                // Locked
                menuPopulator.accept(ItemStackBuilder.of(Material.INK_SACK).data(1).amount(tier)
                        .name("&c&lTier %tier%".replaceAll("%tier%", Utils.getRomanNumber(tier)))
                        .lore(getRewardLore(tierReward, RewardProgress.LOCKED)).buildItem().build());
            }
        }
    }

    private List<String> getRewardLore(BlockReward blockReward, RewardProgress rewardProgress) {
        List<String> lore = new ArrayList<>();
        switch (rewardProgress) {
            case CLAIMED: {
                List<String> list = Arrays.asList(" ", "&2Rewards", "%rewards%", " ", "&a&lCLAIMED");
                for (String string : list) {
                    if (string.equalsIgnoreCase("%rewards%")) {
                        blockReward.getRewards().forEach((reward) -> lore.add(" &2&l| " + reward)); continue;
                    } lore.add(string);
                } return lore;
            } case UNCLAIMED: {
                List<String> list = Arrays.asList(" ", "&6Rewards", "%rewards%", " ", "&a&lLEFT-CLICK &ato claim!");
                for (String string : list) {
                    if (string.equalsIgnoreCase("%rewards%")) {
                        blockReward.getRewards().forEach((reward) -> lore.add(" &6&l| " + reward)); continue;
                    } lore.add(string);
                } return lore;
            } case IN_PROGRESS: {
                List<String> list = Arrays.asList(" ", "&6Rewards", "%rewards%", " ", "%progress% &7(%current%/%max%)", "&e&lIN PROGRESS");
                long current = Blocks.getInstance().getApi().getPlayerBlocks(getPlayer());
                long max = blockReward.getBlocksRequired();
                String progress = Utils.createProgressBar("|", 20, current, max);
                for (String string : list) {
                    if (string.equalsIgnoreCase("%rewards%")) {
                        blockReward.getRewards().forEach((reward) -> lore.add(" &6&l| " + reward)); continue;
                    } lore.add(string
                            .replaceAll("%progress%", progress)
                            .replaceAll("%current%", String.valueOf(current))
                            .replaceAll("%max%", String.valueOf(max)));
                } return lore;
            } case LOCKED: {
                List<String> list = Arrays.asList(" ", "&4Rewards", "%rewards%", " ", "&c&lLOCKED");
                for (String string : list) {
                    if (string.equalsIgnoreCase("%rewards%")) {
                        blockReward.getRewards().forEach((reward) -> lore.add(" &4&l| " + reward)); continue;
                    } lore.add(string);
                } return lore;
            }
        } return null;
    }

    private enum RewardProgress {
        CLAIMED, UNCLAIMED, IN_PROGRESS, LOCKED
    }
}
