package me.DinisEsteves.playerStorePlugin;
import me.DinisEsteves.playerStorePlugin.Listeners.OnMenuClick;
import me.DinisEsteves.playerStorePlugin.Listeners.OnPlacingSignInChest;
import me.DinisEsteves.playerStorePlugin.Listeners.OnShopClick;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public final class PlayerStorePlugin extends JavaPlugin {
    private static PlayerStorePlugin instance;
    private static Economy econ = null;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

     public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new OnPlacingSignInChest(), this);
        getServer().getPluginManager().registerEvents(new OnMenuClick(), this);
        getServer().getPluginManager().registerEvents(new PlayerWallet(), this);
        getServer().getPluginManager().registerEvents(new OnShopClick(), this);
        if (!setupEconomy() ) {
            System.out.println("There is no economy plugin installed");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static PlayerStorePlugin getInstance() {
        return instance;
    }
}
