package me.DinisEsteves.playerStorePlugin.Listeners;

import me.DinisEsteves.playerStorePlugin.PlayerStorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnShopClick implements Listener {
    OnMenuClick onMenuClick = new OnMenuClick();

    public Inventory confirmWindow(Player player) {
        Inventory menu = Bukkit.createInventory(player, 9, ChatColor.BLACK + "Close The Store?");

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

    public boolean isInventoryFull(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return false; // Found an empty slot, chest is not full
            }
        }
        return true; // No empty slots found, chest is full
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {

            if (clickedBlock.getState() instanceof Chest) {
                BlockState state = clickedBlock.getState();
                Directional directional = (Directional) state.getBlockData();
                BlockFace facing = directional.getFacing();
                Block blockFront = clickedBlock.getRelative(facing);
                System.out.println(directional);
                System.out.println(facing);
                System.out.println(blockFront);

                if (blockFront.getState() instanceof Sign && onMenuClick.getShop(blockFront.getState().getLocation()) != null) {
                    OnMenuClick.Shop shop = onMenuClick.getShop(blockFront.getState().getLocation());
                    if (shop.getOwner() != event.getPlayer().getName()) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Can't Open That Chest! You Are Not The Owner!");
                    }
                }
            }

            OnMenuClick.Shop shop = onMenuClick.getShop(clickedBlock.getLocation());
            if (clickedBlock.getType() == Material.OAK_WALL_SIGN && shop != null) {
                Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
                // Buy Action
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    event.setCancelled(true);

                    // Check if the customer is the owner of the store
                    if (shop.getOwner() == event.getPlayer().getName()) {
                        event.getPlayer().openInventory(confirmWindow(event.getPlayer()));
                        return;
                    }

                    // Check if the Shop is Selling items
                    if (shop.getBuyPrice() == 0) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This Shop Is Not Selling Items");
                        return;
                    }

                    // Check if the player has free slots
                    if (isInventoryFull(event.getPlayer().getInventory())) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You're Inventory Is Full!");
                        return;
                    }

                    int packAmount = 0;
                    ItemStack buyItem = shop.getItem();
                    for (ItemStack item : chest.getInventory().getContents()) {
                        if (item != null && item.isSimilar(buyItem) && item.getAmount() == item.getMaxStackSize()) {
                            packAmount++;
                        }
                    }

                    // Sell Operation
                    if (packAmount != 0) {

                        // check if the Customer has enough money to buy the item
                        if (shop.getBuyPrice() > PlayerStorePlugin.getEconomy().getBalance(event.getPlayer())) {
                            event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Don't Have Enough Money To Buy The Item");
                            return;
                        }

                        PlayerStorePlugin.getEconomy().withdrawPlayer(event.getPlayer(), shop.getBuyPrice());
                        PlayerStorePlugin.getEconomy().depositPlayer(shop.getOwner(), shop.getBuyPrice());

                        event.getPlayer().getInventory().addItem(new ItemStack(shop.getItem().getType(), shop.getItem().getMaxStackSize()));
                        chest.getInventory().removeItem(new ItemStack(shop.getItem().getType(), shop.getItem().getMaxStackSize()));

                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The Shop Is Out Of Stock!");
                        return;
                    }




                // Sell Action
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);

                    // check if the Customer is Not the Owner
                    if (shop.getOwner() == event.getPlayer().getName()) {
                        event.getPlayer().openInventory(confirmWindow(event.getPlayer()));
                        return;
                    }

                    // check if the shop is selling
                    if (shop.getSellPrice() == 0) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This Shop Is Not Buying Items");
                        return;
                    }
                    ItemStack sellItem = shop.getItem();
                    int itemCount = 0;
                    Inventory playerInventory = event.getPlayer().getInventory();
                    for (ItemStack item : playerInventory.getContents()) {
                        if (item != null && item.isSimilar(sellItem)) {
                            itemCount += item.getAmount();
                        }
                    }

                    // checking if the customer has the item
                    if (itemCount == 0) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Don't Have That Item!");
                        return;
                    }

                    long money = (long) ((itemCount * shop.getSellPrice()) / (double) sellItem.getMaxStackSize());
                    double ownerBalance = PlayerStorePlugin.getEconomy().getBalance(shop.getOwner());

                    // checking if the Shop Owner has enough money to Pay the Customer
                    if (ownerBalance < money) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The Store Owner Has Not Enough Money To Pay You");
                        return;
                    }

                    // checking if the chest is full
                    if (isInventoryFull(chest.getInventory())) {
                        event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The Store Chest Is Full");
                        return;
                    }

                    event.getPlayer().sendMessage(ChatColor.YELLOW + "You Sold " + itemCount + " " + shop.getItem().getType().name().replace("_", " ").toLowerCase()+ " for " + money + "$");
                    playerInventory.removeItem(new ItemStack(sellItem.getType(), itemCount));

                    PlayerStorePlugin.getEconomy().depositPlayer(event.getPlayer(), money);
                    PlayerStorePlugin.getEconomy().withdrawPlayer(shop.getOwner(), money);

                    // Add items to the shop's chest
                    BlockState state = shop.getChestLocation().getBlock().getState();
                    if (state instanceof Chest) {
                        Inventory chestInventory = chest.getInventory();
                        while (itemCount > 0) {
                            int stackSize = Math.min(itemCount, sellItem.getMaxStackSize());
                            chestInventory.addItem(new ItemStack(sellItem.getType(), stackSize));
                            itemCount -= stackSize;
                        }
                    }
                }
            }
        }
    }
}