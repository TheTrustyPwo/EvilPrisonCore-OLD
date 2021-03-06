package me.pwo.evilprisoncore.utils;

import me.lucko.helper.text3.Text;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.Collections;

import static java.lang.String.join;

public class Utils {
    public static void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
        WorldServer worldServer = ((CraftWorld)world).getHandle();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        IBlockData iBlockData = Block.getByCombinedId(blockId + (data << 12));
        worldServer.setTypeAndData(blockPosition, iBlockData, applyPhysics ? 3 : 2);
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String getRomanNumber(int number) {
        return join("", Collections.nCopies(number, "I"))
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
    }

    public static String createProgressBar(String symbol, int length, double current, double max) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (current >= (max / length) * (i + 1)) {
                stringBuilder.append("&a").append(symbol);
            } else {
                stringBuilder.append("&c").append(symbol);
            }
        }
        return Text.colorize(stringBuilder.toString());
    }

    public static String formatNumber(double number, int type) {
        switch (type) {
            case 1:
                return String.format("%,.2f", number);
            default:
                return String.valueOf(number);
        }
    }

    public static String formatNumber(double number) {
        return String.format("%,.2f", number);
    }

    public static String formatNumber(long number, int type) {
        switch (type) {
            case 1:
                return String.format("%,d", number);
            default:
                return String.valueOf(number);
        }
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
}
