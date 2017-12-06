package com.thetruecolonel.truecustomdrops.interfaces;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

public interface IListener {
	void checkPlayer();
	
	void checkDrop();
	
	void dropItem();
	
	void dropMoney();
	
	ItemStack createItem();
	
	default boolean randomChance (double p) { return new Random().nextFloat() <= (p / 100); }
	
	default int randomInt (int minNumber, int maxNumber) { return new Random().nextInt((maxNumber - minNumber) + 1) + minNumber; }
}
