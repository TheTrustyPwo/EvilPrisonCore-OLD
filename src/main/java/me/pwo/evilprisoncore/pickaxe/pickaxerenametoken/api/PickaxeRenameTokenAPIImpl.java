package me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.api;

import me.pwo.evilprisoncore.pickaxe.pickaxerenametoken.PickaxeRenameToken;
import org.bukkit.entity.Player;

public class PickaxeRenameTokenAPIImpl implements PickaxeRenameTokenAPI {
    private final PickaxeRenameToken pickaxeRenameToken;

    public PickaxeRenameTokenAPIImpl(PickaxeRenameToken pickaxeRenameToken) { this.pickaxeRenameToken = pickaxeRenameToken; }

    @Override
    public boolean hasRenameTokens(Player player) {
        return player.getInventory().containsAtLeast(pickaxeRenameToken.createRenameTokenItem(1), 1);
    }
}
