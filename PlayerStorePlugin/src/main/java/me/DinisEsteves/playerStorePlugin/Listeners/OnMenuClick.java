package me.DinisEsteves.playerStorePlugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnMenuClick implements Listener {

    private final Map<UUID, String> waitingForInput = new HashMap<>();
    private static Map<Location, Shop> shops = new HashMap<>();
    private Inventory current = null;
    private long buy_price_set = 0;
    private long sell_price_set = 0;
    OnPlacingSignInChest onPlacingSignInChest = new OnPlacingSignInChest();

    class Shop {
        private String owner;
        private Location signLocation;
        private Location chestLocation;
        private ItemStack item;
        private long buyPrice;
        private long sellPrice;

        public Shop(String owner, Location signLocation, Location chestLocation, ItemStack item, long buyPrice, long sellPrice) {
            this.owner = owner;
            this.signLocation = signLocation;
            this.chestLocation = chestLocation;
            this.item = item;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
        public String getOwner() {
            return owner;
        }

        public Location getSignLocation() {
            return signLocation;
        }

        public Location getChestLocation() {
            return chestLocation;
        }

        public ItemStack getItem() {
            return item;
        }

        public long getBuyPrice() {
            return buyPrice;
        }

        public long getSellPrice() {
            return sellPrice;
        }
    }
    public void createShop(Player player, Location signLocation, Location chestLocation, ItemStack item, long buyPrice, long sellPrice) {
        Shop shop = new Shop(player.getName(), signLocation, chestLocation, item, buyPrice, sellPrice);
        shops.put(signLocation, shop);
    }

    public Shop getShop(Location key) {
        if (shops.containsKey(key)) {
            return shops.get(key);
        } return null;
    }

    private String formatPrice(long price) {
        if (price >= 1000000000000L) {
            return price / 1000000000000L + "T";
        } else if (price >= 1000000000) {
            return price / 1000000000 + "B";
        } else if (price >= 1000000) {
            return price / 1000000 + "M";
        } else if (price >= 1000) {
            return price / 1000 + "k";
        } else {
            return String.valueOf(price);
        }
    }

    public void createSign(Player player, ItemStack item) {
        Block targetBlock = player.getTargetBlock(null, 4);

        if (targetBlock != null && targetBlock.getType() == Material.CHEST) {
            Directional directional = (Directional) targetBlock.getBlockData();
            BlockFace chestFacing = directional.getFacing();
            Block signBlock = targetBlock.getRelative(chestFacing);

            signBlock.setType(Material.OAK_WALL_SIGN);

            if (signBlock.getState() instanceof Sign) {
                Sign sign = (Sign) signBlock.getState();
                WallSign wallSign = (WallSign) sign.getBlockData();
                wallSign.setFacing(chestFacing);
                sign.setBlockData(wallSign);
                sign.setLine(0, ChatColor.UNDERLINE + player.getName() + "'s Shop");
                sign.setLine(1, ChatColor.GRAY + "[" + item.getMaxStackSize() + "]");

                String buyPrice = formatPrice(buy_price_set);
                String sellPrice = formatPrice(sell_price_set);

                if (buy_price_set != 0 && sell_price_set != 0) {
                    sign.setLine(2, ChatColor.GREEN + "B " + buyPrice + ChatColor.BLACK + " |" + ChatColor.YELLOW + " S " + sellPrice);
                } else if (buy_price_set == 0) {
                    sign.setLine(2, ChatColor.YELLOW + "S " + sellPrice);
                } else {
                    sign.setLine(2, ChatColor.GREEN + "B " + buyPrice);
                }
                sign.setLine(3, ChatColor.BLUE + item.getType().name().replace("_", " ").toLowerCase());
                sign.update();
                createShop(player, sign.getLocation(), targetBlock.getLocation(), item, buy_price_set, sell_price_set);
            }
        } else {
            System.out.println("Target block is not a chest or is null.");
        }
    }

    public Inventory createStoreMenu(Player player) {

        Inventory menu = Bukkit.createInventory(player, 18, ChatColor.BLACK + "Store Setup");

        // Buttons
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemStack itemPlaceOlder = new ItemStack(Material.BEDROCK, 1);
        ItemStack buyPrice = new ItemStack(Material.LIME_DYE, 1);
        ItemStack sellPrice = new ItemStack(Material.RED_DYE, 1);

        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Cancel");
        cancel.setItemMeta(cancelMeta);

        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Confirm");
        confirm.setItemMeta(confirmMeta);

        ItemMeta PlaceOlderMeta = itemPlaceOlder.getItemMeta();
        PlaceOlderMeta.setDisplayName("Place the item you want to sell here");
        itemPlaceOlder.setItemMeta(PlaceOlderMeta);

        ItemMeta buyMeta = buyPrice.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GREEN + "Set Buy Price");
        buyPrice.setItemMeta(buyMeta);

        ItemMeta sellMeta = sellPrice.getItemMeta();
        sellMeta.setDisplayName(ChatColor.RED + "Set Sell Price");
        sellPrice.setItemMeta(sellMeta);

        menu.setItem(2, sellPrice);
        menu.setItem(4, itemPlaceOlder);
        menu.setItem(6, buyPrice);
        menu.setItem(9, cancel);
        menu.setItem(17, confirm);

        return menu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Open Store Confirm Menu
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Do you want to open a store?")) {
            Player player = (Player) event.getWhoClicked();

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            switch (currentItem.getType()) {
                case EMERALD_BLOCK:
                    player.closeInventory();
                    player.openInventory(createStoreMenu(player));
                    break;
                case REDSTONE_BLOCK:
                    player.closeInventory();
                    break;
                default:
                    break;
            }
        }

        // Store Setup Menu
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Store Setup")) {
            Player player = (Player) event.getWhoClicked();

            // Check if the click is in the top inventory
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            switch (currentItem.getType()) {
                case EMERALD_BLOCK:
                    ItemStack itemStack = event.getClickedInventory().getItem(4);
                    Material item = itemStack.getType();

                    if (item == Material.BEDROCK || item == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Must Choose An Item To Sell");
                    } else if (buy_price_set + sell_price_set == 0) {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You Must Set At Least One Price");
                    } else {
                        createSign(player, itemStack);
                        sell_price_set = 0;
                        buy_price_set = 0;
                        player.closeInventory();
                    }
                    break;
                case REDSTONE_BLOCK:
                    player.closeInventory();
                    buy_price_set = 0;
                    sell_price_set = 0;
                    break;
                case LIME_DYE:
                    current = player.getOpenInventory().getTopInventory();
                    player.closeInventory();
                    waitingForInput.put(player.getUniqueId(), "LIME_DYE");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the buy price:");
                    break;
                case RED_DYE:
                    current = player.getOpenInventory().getTopInventory();
                    player.closeInventory();
                    waitingForInput.put(player.getUniqueId(), "RED_DYE");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the sell price:");
                    break;
                default:
                    break;
            }
        }

        // Confirm Menu
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Close The Store?")) {
            Player player = (Player) event.getWhoClicked();

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            switch (currentItem.getType()) {
                case EMERALD_BLOCK:
                    player.closeInventory();

                    if (shops.containsKey(player.getTargetBlock(null, 5).getLocation())) {
                        shops.remove(player.getTargetBlock(null, 5).getLocation());
                        player.getTargetBlock(null, 5).setType(Material.AIR);
                    }
                    break;
                case REDSTONE_BLOCK:
                    player.closeInventory();
                    break;
                default:
                    break;
            }
        }

    }

    @EventHandler
    public void onInventoryPlace(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Store Setup")) {
            Player player = (Player) event.getWhoClicked();

            // Allow moving items around in the player's inventory
            if (event.getClickedInventory() == player.getInventory()) return;

            // Restrict placing items only in the 4th slot of the chest inventory
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                if (event.getSlot() == 4 && event.getCursor() != null) {
                    ItemStack cursorItem = event.getCursor().clone();
                    event.getInventory().setItem(4, cursorItem);
                    event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLACK + "Store Setup")) {
            if (event.getRawSlots().contains(4) && event.getCursor() != null) {
                if (event.getRawSlots().size() == 1 && event.getRawSlots().contains(4)) {
                    ItemStack cursorItem = event.getCursor().clone();
                    event.getInventory().setItem(4, cursorItem);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (waitingForInput.containsKey(playerId)) {
            String context = waitingForInput.get(playerId);
            String message = event.getMessage();

            if ("LIME_DYE".equals(context)) {
                try {
                    // Process the player's input
                    long buy_price = Long.parseLong(message);
                    if (buy_price > 0) {
                        player.sendMessage(ChatColor.GREEN + "Buy Price Set To: " + message + "$");
                        buy_price_set = buy_price;
                        ItemStack buy = new ItemStack(Material.PAPER, 1);
                        ItemMeta buy_meta = buy.getItemMeta();
                        buy_meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Buy Price: " + buy_price_set + "$");
                        buy.setItemMeta(buy_meta);
                        current.setItem(6, buy);
                    } else {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid Value: Must be greater than 0");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid Value: Please enter a valid number");
                } finally {
                    // Remove the player from the waiting list
                    waitingForInput.remove(playerId);
                    player.openInventory(current);
                    current = null;
                }
                event.setCancelled(true);
            } else {
                try {
                    // Process the player's input
                    long sell_price = Long.parseLong(message);
                    if (sell_price > 0) {
                        player.sendMessage(ChatColor.GREEN + "Sell Price Set To: " + message + "$");
                        sell_price_set = sell_price;
                        ItemStack sell = new ItemStack(Material.PAPER, 1);
                        ItemMeta sell_meta = sell.getItemMeta();
                        sell_meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Sell Price: " + sell_price_set + "$");
                        sell.setItemMeta(sell_meta);
                        current.setItem(2, sell);
                    } else {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid Value: Must be greater than 0");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Invalid Value: Please enter a valid number");
                } finally {
                    // Remove the player from the waiting list
                    waitingForInput.remove(playerId);
                    player.openInventory(current);
                    current = null;
                }
                event.setCancelled(true);
            }
        }
    }
}
