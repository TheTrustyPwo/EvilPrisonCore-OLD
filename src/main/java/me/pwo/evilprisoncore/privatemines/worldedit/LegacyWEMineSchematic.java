package me.pwo.evilprisoncore.privatemines.worldedit;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.schematic.SchematicFormat;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class LegacyWEMineSchematic extends MineSchematic<Schematic> {
    protected LegacyWEMineSchematic(String name, List<String> description, File file, ItemStack icon) {
        super(name, description, file, icon);
    }

    public Schematic getSchematic() {
        if (!this.file.exists())
            throw new IllegalStateException("File " + this.file.getAbsolutePath() + "  does not exist");
        SchematicFormat format = SchematicFormat.getFormat(this.file);
        try {
            return format.load(this.file);
        } catch (IOException | DataException e) {
            e.printStackTrace();
            return null;
        }
    }
}
