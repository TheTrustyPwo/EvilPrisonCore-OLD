package me.pwo.evilprisoncore.enchants.enchants.impl;

import com.sk89q.worldedit.regions.CuboidRegion;
import me.lucko.helper.Events;
import me.lucko.helper.time.Time;
import me.pwo.evilprisoncore.enchants.Enchants;
import me.pwo.evilprisoncore.enchants.enchants.EvilEnchant;
import me.pwo.evilprisoncore.privatemines.mine.Mine;
import me.pwo.evilprisoncore.privatemines.worldedit.WorldEditUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class LaserEnchant extends EvilEnchant {
    private static final List<Vector> vectors = Arrays.asList(
            new Vector(0, -1, 0),
            new Vector(1, -1, 0),
            new Vector(0, -1, 1),
            new Vector(-1, -1, 0),
            new Vector(0, -1, -1),
            new Vector(1, -1, 1),
            new Vector(1, -1, -1),
            new Vector(-1, -1, 1),
            new Vector(-1, -1, -1),
            new Vector(0, 0, 0),
            new Vector(1, 0, 0),
            new Vector(0, 0, 1),
            new Vector(-1, 0, 0),
            new Vector(0, 0, -1),
            new Vector(1, 0, 1),
            new Vector(1, 0, -1),
            new Vector(-1, 0, 1),
            new Vector(-1, 0, -1),
            new Vector(0, 1, 0),
            new Vector(1, 1, 0),
            new Vector(0, 1, 1),
            new Vector(-1, 1, 0),
            new Vector(0, 1, -1),
            new Vector(1, 1, 1),
            new Vector(1, 1, -1),
            new Vector(-1, 1, 1),
            new Vector(-1, 1, -1)
    );

    public LaserEnchant(Enchants enchants) {
        super(enchants, 16);
        Events.subscribe(PlayerDropItemEvent.class)
                .filter(e -> e.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    Mine mine = this.enchants.getPlugin().getPrivateMines().getApi().getMineByLocation(e.getPlayer().getLocation());
                    if (mine == null) return;
                    e.setCancelled(true);
                    List<Block> list = e.getPlayer().getLineOfSight(null, 50);
                    shootLaser(e.getPlayer().getEyeLocation(), list.get(list.size() - 1).getLocation(),
                            mine.getMineRegion());
                }).bindWith(this.getEnchants().getPlugin());
    }

    private void shootLaser(Location from, Location to, CuboidRegion cuboidRegion) {
        long l1 = Time.nowMillis();
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        Location fromNew = from.clone();
        while (from.distanceSquared(fromNew) <= 5000) {
            vectors.stream().filter(vector -> cuboidRegion.contains(WorldEditUtil.toWEVector(fromNew.clone().add(vector))))
                    .forEach(vector -> fromNew.getWorld().getBlockAt(fromNew.clone().add(vector)).setType(Material.AIR));
            from.getWorld().spawnParticle(Particle.REDSTONE, fromNew, 25, 0.25D, 0.25D, 0.25D);
            fromNew.add(direction);
        }
        long l2 = Time.nowMillis();
        this.enchants.getPlugin().debug(String.format("Laser::ShootLaser >> Took %d ms.", l2 - l1));
    }

    @Override
    public void onEquip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onUnequip(Player player, ItemStack itemStack, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level, double random) {

    }

    @Override
    public void reload() {

    }
}
