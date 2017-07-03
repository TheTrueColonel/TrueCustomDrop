package com.thetruecolonel.truecustomdrops;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bstats.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.thetruecolonel.truecustomdrops.listeners.BlockListener;
import com.thetruecolonel.truecustomdrops.listeners.MobListener;

import net.milkbowl.vault.economy.Economy;

public class TrueCustomDropsSpigot extends JavaPlugin {
	public static FileConfiguration config;
	public static TrueCustomDropsSpigot plugin;

	public static final Logger log = Logger.getLogger("Minecraft");
	
	private File configf;
	
	public String version = getDescription().getVersion();
	public static Economy economy = null;
	public static Boolean useVault = false;
	
	@Override
	public void onEnable () {
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
		
		createFiles();
		
		plugin = this;
		
		if (getConfig().getBoolean("useVault")) {
			useVault = true;
			if (!setupEconomy()) {
				log.severe(String.format("[%s] - Disabled due to no Vault dependancy found!", getDescription().getName()));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		} else {
			log.warning("\"useVault\" in config.yml is set to false! You will not be able to reward money to anyone!");
			log.warning("If you would like to reward money as a drop, please set \"useVault\" to true and restart your server!");
		}
		
		getServer().getPluginManager().registerEvents(new BlockListener(economy), this);
		getServer().getPluginManager().registerEvents(new MobListener(economy), this);
	}
	
	@Override
	public void onDisable () { plugin = null; }
	
	private void createFiles () {
		configf = new File(getDataFolder(), "config.yml");
		
		if (!configf.exists()) {
			configf.getParentFile().mkdirs();
			final FileConfiguration config = getConfig();
			
			config.addDefault("useVault", false);
			
			config.options().copyDefaults(true);
			saveConfig();
		}
		
		config = new YamlConfiguration();
		try {
            config.load(configf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private boolean setupEconomy () {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}
	
	@Override
	public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("drops")) {
			if (sender instanceof Player) {
				if (args.length == 0) {
					if (sender.hasPermission("truecustomdrops.info")) {
						sender.sendMessage("[TrueCustomDrops] Version: v" + version);
					} else {
						sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
					return true;
				}
			}
		}
		
		return false;
	}
}
