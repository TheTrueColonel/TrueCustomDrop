package com.thetruecolonel.truecustomdrops.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.thetruecolonel.truecustomdrops.BuildConfigurations;
import com.thetruecolonel.truecustomdrops.TrueCustomDropsSpigot;
import com.thetruecolonel.truecustomdrops.interfaces.IListener;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

@SuppressWarnings("deprecation")
public class MobListener implements Listener, IListener {
	// Variables
	private boolean defaultDrops;
	private String path = "mobs.";
	private String entity;
	private String drop;
	private List<ItemStack> drops = new ArrayList<>();
	
	private final String pathDefault = "mobs.";
	
	private static BuildConfigurations config;
	private static Economy economy;
	// Temp variables
	private static LivingEntity en;
	private static EntityDeathEvent e;
	private static Player p;
	
	public MobListener (Economy e) {
		config = BuildConfigurations.getMobConfig();
		economy = e;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	void onEntityDeath (EntityDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			e = event;
			en = e.getEntity();
			p = en.getKiller();
			
			if (p.getGameMode() == GameMode.CREATIVE)
				return;

			for (String entity : config.getConfigurationSection("mobs").getKeys(false)) {
				this.entity = entity;
				path = pathDefault;
				path += entity;

				defaultDrops = config.getBoolean(path + ".defaultDrops", true);

				if (en.getType().name().equals("SLIME") || en.getType().name().equals("MAGMA_CUBE")) {
					slimeCheck();
					break;
				} else {
					checkPrelims();
					break;
				}
			}
		}
	}
	
	private void slimeCheck() {
		if (en.getType().name().equals("SLIME")) {
			if (((Slime)en).getSize() == config.getInt(path+".size"))
				checkPrelims();
		} else if (en.getType().toString().equals("MAGMA_CUBE")) {
			if (((MagmaCube)en).getSize() == config.getInt(path+".size"))
				checkPrelims();
		}
	}
	
	private void checkPrelims() {
		if (en.getType().toString().equals(entity)) {
			if (config.isSet(path+".requiredTools")) {
				for (Integer id : config.getIntegerList(path+".requiredTools")) {
					if (p.getEquipment().getItemInMainHand().getTypeId() == id) {
						checkPlayer();
						break;
					}
				}
			} else {
				checkPlayer();
			}
		}
	}
	
	public void checkPlayer () {
		outerLoop:
		for (String drop : config.getConfigurationSection(path+".drops").getKeys(false)) {
			this.drop = drop;
			path = pathDefault+entity+".drops."+drop;
			
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
	
	public void checkDrop() {
		path = pathDefault+entity+".drops."+drop;
		if (config.isSet(path+".id") && config.isSet(path+".money")) {
			dropItem();
			if (TrueCustomDropsSpigot.useVault) {
				dropMoney();
			}
		} else if (config.isSet(path+".id")) {
			dropItem();
		} else if (config.isSet(path+".money")) {
			if (TrueCustomDropsSpigot.useVault) {
				dropMoney();
			}
		}
	}
	
	public void dropItem () {
		path = pathDefault+entity+".drops."+drop;
		drops.clear();
		
		if (!defaultDrops)
			e.getDrops().clear();
		
		if (config.isSet(path+".chance")) {
			if (randomChance(config.getDouble(path+".chance"))) {
				drops.add(createItem());
				e.getDrops().addAll(drops);
			}
		} else {
			drops.add(createItem());
			e.getDrops().addAll(drops);
		}
		
		path = pathDefault;
	}
	
	public void dropMoney () {
		path = pathDefault+entity+".drops."+drop+".money";
		
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
				List<String> lore = new ArrayList<>();
				
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



















