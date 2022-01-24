package me.pwo.evilprisoncore.utils.compat;

import org.bukkit.Bukkit;

public final class MinecraftVersion {
    private static String serverVersion;
    private static V current;

    public static V getCurrent() {
        return current;
    }

    public enum V {
        v1_18(18, false),
        v1_17(17),
        v1_16(16),
        v1_15(15),
        v1_14(14),
        v1_13(13),
        v1_12(12),
        v1_11(11),
        v1_10(10),
        v1_9(9),
        v1_8(8),
        v1_7(7),
        v1_6(6),
        v1_5(5),
        v1_4(4),
        v1_3_AND_BELOW(3);

        private final int minorVersionNumber;

        private final boolean tested;

        public boolean isTested() {
            return this.tested;
        }

        V(int minorVersionNumber) {
            this.minorVersionNumber = minorVersionNumber;
            this.tested = true;
        }

        V(int minorVersionNumber, boolean tested) {
            this.minorVersionNumber = minorVersionNumber;
            this.tested = tested;
        }

        private static V parse(int param1Int) {
            for (V v : values()) {
                if (v.minorVersionNumber == param1Int)
                    return v;
            }
            return null;
        }

        public String toString() {
            return "1." + this.minorVersionNumber;
        }
    }

    public static boolean equals(V paramV) {
        return (compareWith(paramV) == 0);
    }

    public static boolean olderThan(V paramV) {
        return (compareWith(paramV) < 0);
    }

    public static boolean newerThan(V paramV) {
        return (compareWith(paramV) > 0);
    }

    public static boolean atLeast(V paramV) {
        return (equals(paramV) || newerThan(paramV));
    }

    private static int compareWith(V paramV) {
        try {
            return (getCurrent()).minorVersionNumber - paramV.minorVersionNumber;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
    }

    public static String getServerVersion() {
        return serverVersion.equals("craftbukkit") ? "" : serverVersion;
    }

    static {
        try {
            String str1 = (Bukkit.getServer() == null) ? "" : Bukkit.getServer().getClass().getPackage().getName();
            String str2 = str1.substring(str1.lastIndexOf('.') + 1);
            boolean bool = !"craftbukkit".equals(str2) && !"".equals(str1);
            serverVersion = str2;
            if (bool) {
                byte b = 0;
                for (char c : str2.toCharArray()) {
                    b++;
                    if (b > 2 && c == 'R')
                        break;
                }
                String str = str2.substring(1, b - 2).replace("_", ".");
                current = V.parse(Integer.parseInt(str.split("\\.")[1]));
            } else {
                current = V.v1_3_AND_BELOW;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
