package com.thetruecolonel.truecustomdrops;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class BuildConfigurations extends YamlConfiguration {
	private static BuildConfigurations blockConfig;
	private static BuildConfigurations mobConfig;
	
	public static BuildConfigurations getBlockConfig () { return blockConfig == null ? blockConfig = new BuildConfigurations(blockConfig) : blockConfig; }
	
	public static BuildConfigurations getMobConfig () { return mobConfig == null ? mobConfig = new BuildConfigurations(mobConfig) : mobConfig; }
	
	private TrueCustomDropsSpigot plugin;
	private File blockConfigFile;
	private File mobConfigFile;
	
	public BuildConfigurations (BuildConfigurations config) {
		plugin = TrueCustomDropsSpigot.getPlugin(TrueCustomDropsSpigot.class);
		if (config == blockConfig) {
			blockConfigFile = new File(plugin.getDataFolder(), "block-drops.yml");
			saveDefault(blockConfig);
			reload(blockConfig);
		} else if (config == mobConfig) {
			mobConfigFile = new File(plugin.getDataFolder(), "mob-drops.yml");
			saveDefault(mobConfig);
			reload(mobConfig);
		}
	}
	
	public void reload (BuildConfigurations config) {
		if (config == blockConfig) {
			try {
				super.load(blockConfigFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (config == mobConfig) {
			try {
				super.load(mobConfigFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void save (BuildConfigurations config) {
		if (config == blockConfig) {
			try {
				super.save(blockConfigFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (config == mobConfig) {
			try {
				super.save(mobConfigFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void saveDefault (BuildConfigurations config) {
		if (config == blockConfig) {
			if (!blockConfigFile.exists()) {
				plugin.saveResource("block-drops.yml", false);
			}
		} else if (config == mobConfig) {
			if (!mobConfigFile.exists()) {
				plugin.saveResource("mob-drops.yml", false);
			}
		}
	}
}
