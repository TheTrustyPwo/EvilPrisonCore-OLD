package me.pwo.evilprisoncore.utils;

import org.bukkit.Bukkit;

public enum MinecraftVersion {
    Unknown(2147483647),
    MC1_7_R4(174),
    MC1_8_R3(183),
    MC1_9_R1(191),
    MC1_9_R2(192),
    MC1_10_R1(1101),
    MC1_11_R1(1111),
    MC1_12_R1(1121),
    MC1_13_R1(1131),
    MC1_13_R2(1132),
    MC1_14_R1(1141),
    MC1_15_R1(1150),
    MC1_16_R1(1160),
    MC1_16_R2(1161),
    MC1_16_R3(1163),
    MC1_17_R1(1170);

    private final int versionId;
    private static MinecraftVersion version;

    MinecraftVersion(int versionId) {
        this.versionId = versionId;
    }

    public static MinecraftVersion init() {
        if (version != null)
            return version;
        String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            version = valueOf(ver.replace("v", "MC"));
        } catch (IllegalArgumentException ex) {
            version = Unknown;
        }
        return version;
    }

    public static boolean isNew() {
        return (getVersionNumber() >= 1130);
    }

    public static MinecraftVersion getVersion() {
        return version;
    }

    public static int getVersionNumber() {
        return version.versionId;
    }

    public static MinecraftVersion getCurrentVersion() {
        return version;
    }

    public int getVersionId() {
        return this.versionId;
    }

    public static boolean isAtLeastVersion(MinecraftVersion version) {
        return (getVersion().getVersionId() >= version.getVersionId());
    }

    public static boolean isNewerThan(MinecraftVersion version) {
        return (getVersion().getVersionId() > version.getVersionId());
    }
}