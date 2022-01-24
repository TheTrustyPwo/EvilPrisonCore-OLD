package me.pwo.evilprisoncore.blocks.blockrewards;

import me.lucko.helper.Commands;
import me.pwo.evilprisoncore.EvilPrisonModule;
import me.pwo.evilprisoncore.blocks.Blocks;
import me.pwo.evilprisoncore.blocks.blockrewards.gui.BlockRewardsGUI;
import me.pwo.evilprisoncore.blocks.blockrewards.manager.BlockRewardsManager;
import me.pwo.evilprisoncore.blocks.blockrewards.model.BlockReward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.List;

public class BlockRewards implements EvilPrisonModule {
    private static BlockRewards instance;
    private final Blocks blocks;
    private BlockRewardsManager blockRewardsManager;
    private FileConfiguration blockRewardsConfig;
    private final LinkedHashMap<Integer, BlockReward> blockRewards = new LinkedHashMap<>();
    private boolean enabled;

    public BlockRewards(Blocks blocks) {
        this.blocks = blocks;
    }

    public BlockRewardsManager getBlockRewardsManager() {
        return blockRewardsManager;
    }

    public Blocks getBlocks() {
        return blocks;
    }

    public static BlockRewards getInstance() {
        return instance;
    }

    public LinkedHashMap<Integer, BlockReward> getBlockRewards() {
        return blockRewards;
    }

    @Override
    public void enable() {
        instance = this;
        this.blockRewardsManager = new BlockRewardsManager(this);
        this.blockRewardsConfig = this.blocks.getPlugin().getFileUtils().getConfig("block-rewards.yml").copyDefaults(true).save().get();
        this.enabled = true;
        loadBlockRewards();
        registerCommands();
    }

    private void loadBlockRewards() {
        ConfigurationSection section = this.blockRewardsConfig.getConfigurationSection("block-rewards");
        for (String string : section.getKeys(false)) {
            int id = Integer.parseInt(string);
            long blocksRequired = section.getLong(string + ".blocks-required");
            List<String> rewards = section.getStringList(string + ".rewards");
            List<String> commandsToExecute = section.getStringList(string + ".commands");
            this.blockRewards.put(id, new BlockReward(blocksRequired, rewards, commandsToExecute));
        }
        this.blocks.getPlugin().getLogger().info("Loaded " + this.blockRewards.keySet().size() + " Block Rewards!");
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(context -> (new BlockRewardsGUI(context.sender(), 1)).open()).registerAndBind(this.blocks.getPlugin(), "blockrewards", "blockreward");
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void reload() {
        this.blocks.getPlugin().getFileUtils().getConfig("block-rewards.yml").reload();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getName() {
        return "Block Rewards";
    }
}
