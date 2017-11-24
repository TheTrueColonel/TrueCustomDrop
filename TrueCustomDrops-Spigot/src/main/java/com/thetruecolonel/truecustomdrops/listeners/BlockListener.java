package com.thetruecolonel.truecustomdrops.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.thetruecolonel.truecustomdrops.BuildConfigurations;
import com.thetruecolonel.truecustomdrops.TrueCustomDropsSpigot;
import com.thetruecolonel.truecustomdrops.interfaces.IListener;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

@SuppressWarnings("deprecation")
public class BlockListener implements Listener, IListener {
	// Variables
	private boolean defaultDrops;
	private String path = "blocks.";
	private String worldBlock;
	private String drop;
	
	private final String pathDefault = "blocks.";
	
	private static BuildConfigurations config;
	private static Economy economy;
	// Temp variables
	private static Block b;
	private static BlockBreakEvent e;
	private static Player p;

	public BlockListener (Economy e) {
		config = BuildConfigurations.getBlockConfig();
		economy = e;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onBlockBreak (BlockBreakEvent event) {
		// Set class event, player, and block references
		e = event;
		p = e.getPlayer();
		b = e.getBlock();
		
		// If the player is in creative mode, do nothing
		if (p.getGameMode() == GameMode.CREATIVE)
			return;
		// Goes through the config to start checks
		for (String worldBlock : config.getConfigurationSection("blocks").getKeys(false)) {
			this.worldBlock = worldBlock;
			path = pathDefault;
			path += worldBlock;
			
			if (b.getTypeId() == config.getInt(path+".id") && (int)b.getData() == config.getInt(path+".dataID")) {
				// Gets if the block should also drop it's default drop || Defaults to true
				defaultDrops = config.getBoolean(path+".defaultDrops", true);
				// Checks if the player needs a tool and if they have it.
				if (config.isSet(path+".requiredTools")) {
					for (Integer id : config.getIntegerList(path+".requiredTools")) {
						if (p.getEquipment().getItemInMainHand().getTypeId() == id) {
							checkPlayer();
							break;
						}
					}
				} else {
					checkPlayer();
					break;
				}
			}
		}
	}
	
	public void checkPlayer () {
		outerLoop:
		for (String drop : config.getConfigurationSection(path+".drops").getKeys(false)) {
			this.drop = drop;
			path = pathDefault+worldBlock+".drops."+drop;
			
			if (config.isSet(path+".classes")) {
				for (String perm : config.getStringList(path+".classes")) {
					if (p.hasPermission("truecustomdrops.class."+perm) || p.isOp()) {
						checkDrop();
						break outerLoop;
					}
				}
			} else {
				checkDrop();
				break;
			}
		}
	}
	
	public void checkDrop () {
		path = pathDefault+worldBlock+".drops."+drop;
		if (config.isSet(path+".id") && config.isSet(path+".money")) {
			dropItem();
			if (TrueCustomDropsSpigot.useVault) {
				dropMoney();
			}
		} else if (config.isSet(path+".id") && !config.isSet(path+".money")) {
			dropItem();
		} else if (config.isSet(path+".money") && !config.isSet(path+".id")) {
			if (TrueCustomDropsSpigot.useVault) {
				dropMoney();
			}
		}
	}
	
	public void dropItem () {
		path = pathDefault+worldBlock+".drops."+drop;
		
		if (!defaultDrops)
			b.setType(Material.AIR);
		
		if (config.isSet(path+".chance")) {
			if (randomChance(config.getDouble(path+".chance"))) {
				b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), createItem());
			}
		} else {
			b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), createItem());
		}
		
		path = pathDefault;
	}
	
	public void dropMoney () {
		path = pathDefault+worldBlock+".drops."+drop+".money";
		
		if (config.isSet(path+".chance")) {
			if (randomChance(config.getDouble(path+".chance"))) {
				EconomyResponse r = economy.depositPlayer(p, config.getDouble(path+".value"));
				
				if (r.transactionSuccess()) {
					p.sendMessage(ChatColor.GREEN + String.format("Lucky you! %s has been added to your account!", economy.format(r.amount)));
				} else {
					p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
				}
			}
		} else {
			EconomyResponse r = economy.depositPlayer(p, config.getDouble(path+".value"));
			
			if (r.transactionSuccess()) {
				p.sendMessage(ChatColor.GREEN + String.format("%s has been added to your account!", economy.format(r.amount)));
			} else {
				p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
			}
		}
		
		path = pathDefault;
	}
	
	public ItemStack createItem () {
		ItemStack item = new ItemStack(config.getInt(path+".id"));
		
		if (config.isSet(path+".meta")) {
			ItemMeta meta = item.getItemMeta();
			String metaPath = path+".meta";
			
			if (config.isSet(metaPath+".name")) 
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(metaPath+".name")));
			
			if (config.isSet(metaPath+".lore")) {
				List<String> lore = new ArrayList<String>();
				
				for (String line : config.getStringList(metaPath+".lore")) {
					lore.add(ChatColor.translateAlternateColorCodes('&', line));
				}
				
				meta.setLore(lore);
			}
			
			if (config.isSet(metaPath+".enchants")) {
				String enchantPath = metaPath+".enchants";
				
				for (String enchant : config.getConfigurationSection(enchantPath).getKeys(false)) {
					meta.addEnchant(Enchantment.getById(config.getInt(enchantPath+"."+enchant+".id")), config.getInt(enchantPath+"."+enchant+".level"), true);
				}
			}
			item.setItemMeta(meta);
		}
		
		if (config.isSet(path+".amount")) {
			item.setAmount(config.getInt(path+".amount"));
		} else if (config.isSet(path+".min") && config.isSet(path+".max")) {
			item.setAmount(randomInt(config.getInt(path+".min"), config.getInt(path+".max")));
		}
		
		return item;
	}
}