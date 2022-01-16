package me.pwo.evilprisoncore.multipliers.enums;

import dev.dbassett.skullcreator.SkullCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MultiplierSource {
    MONEY_PET,
    TOKENS_PET,
    GEMS_PET,
    EXP_PET,
    BOOSTER,
    MISCELLANEOUS;

    public static int getPriority(MultiplierSource multiplierSource) {
        switch (multiplierSource) {
            case MONEY_PET: return 1;
            case BOOSTER: return 2;
            case MISCELLANEOUS: return 3;
            default: return 100;
        }
    }

    public static ItemStack getIcon(MultiplierSource multiplierSource) {
        switch (multiplierSource) {
            case MONEY_PET: return SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk0MmU2YzM4YzNiYjQzY2FhMzhiYTViNDlhNDgwNDdmY2VlYzcwMGNlN2NiMWE3YzFkZTU2OTQxMGYzNWIzOSJ9fX0=");
            case BOOSTER: return null;
            case MISCELLANEOUS: return SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQxNDE1YmExZWRiZTUxZWJjOTA5N2U4ZGJhNTI2MWI4MWZmNTUxODU1Y2I1YmZmYzIxODNlM2I5MjhmIn19fQ==");
            default: return new ItemStack(Material.PAPER);
        }
    }
}
