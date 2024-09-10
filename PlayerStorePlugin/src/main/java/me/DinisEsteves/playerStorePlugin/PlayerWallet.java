package me.DinisEsteves.playerStorePlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class PlayerWallet implements Listener {

    public void createWallet(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard wallet = manager.getNewScoreboard();

        Economy economy = PlayerStorePlugin.getEconomy();

        Objective objective = wallet.registerNewObjective("wallet", "dummy", ChatColor.BOLD + "" + ChatColor.YELLOW + player.getName() + "'s Wallet");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score s1 = objective.getScore("");
        Score s2 = objective.getScore(" ");
        s2.setScore(1);
        s1.setScore(5);

        player.setScoreboard(wallet);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateWallet(player);
            }
        }.runTaskTimer(PlayerStorePlugin.getInstance(), 0L, 25L);
    }

    public void updateWallet(Player player) {
        Economy economy = PlayerStorePlugin.getEconomy();
        double currentBalance = economy.getBalance(player);

        Scoreboard wallet = player.getScoreboard();
        Objective objective = wallet.getObjective("wallet");
        if (objective != null) {
            // Reset the previous score
            for (String entry : wallet.getEntries()) {
                if (entry.contains("Balance: ")) {
                    wallet.resetScores(entry);
                }
            }

            // Set the new score
            String balance = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance_formatted%");
            Score score = objective.getScore(ChatColor.GREEN + "" + ChatColor.BOLD + "Balance: " + balance + "$");
            score.setScore(3);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createWallet(player);

    }
}
