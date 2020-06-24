package com.github.plagueisthewise21;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.plagueisthewise21.ChestData.OpenChest;


public class Main extends JavaPlugin{
	
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		Bukkit.getPluginManager().registerEvents(new OpenChest(this), this);		
	}
	
	public void onDisable() {
	}

}
