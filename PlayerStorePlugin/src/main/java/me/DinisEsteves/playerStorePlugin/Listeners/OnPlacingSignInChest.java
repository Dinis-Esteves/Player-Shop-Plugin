package me.DinisEsteves.playerStorePlugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnPlacingSignInChest implements Listener {

    public Location Chest_Location = null;

    public Location getChest() {
        return Chest_Location;
    }

    public Inventory createMenu(Player player) {

        Inventory menu = Bukkit.createInventory(player, 9, ChatColor.BLACK + "Do you want to open a store?");

        // Buttons
        ItemStack no = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack yes = new ItemStack(Material.EMERALD_BLOCK, 1);

        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "No");
        no.setItemMeta(noMeta);

        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Yes");
        yes.setItemMeta(yesMeta);

        menu.setItem(2, no);
        menu.setItem(6, yes);



        return menu;
    }


    @EventHandler
    public void onSignPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getBlockData() instanceof WallSign) {
            WallSign sign = (WallSign) block.getBlockData();

            if (block.getRelative(sign.getFacing().getOppositeFace()).getType() == Material.CHEST) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.openInventory(createMenu(player));
                Chest_Location = block.getRelative(sign.getFacing().getOppositeFace()).getLocation();
            }
        }
    }
}
