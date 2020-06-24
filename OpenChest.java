package com.github.plagueisthewise21.ChestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.plagueisthewise21.Main;



public class OpenChest implements Listener{

	private Main plugin;

	public OpenChest(Main main) {
		plugin = main;
	}

	private static final Random RANDOM = new Random();

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getCurrentItem() == null) return;
		if(e.getView().getTitle().equalsIgnoreCase("Searching...")) {
			e.setCancelled(true);
		}
	}

	private boolean cancel = false;

	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		//check if player closes inv while it is searching
		if(event.getView().getTitle().equalsIgnoreCase("Searching...")) {
			cancel = true;
		}
	}


	@EventHandler
	public void onChestOpen(PlayerInteractEvent event) {	
		Player player = event.getPlayer();
		boolean enabled = plugin.getConfig().getBoolean("chest-scramble-enabled");		

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getType().equals(Material.CHEST) && enabled) {				

				InventoryHolder chest = (InventoryHolder) event.getClickedBlock().getState();
				Location loc = event.getClickedBlock().getLocation();
				if(player.hasPermission("chestsearch.negatesearch")) {
					event.setCancelled(false);
				}else {
					event.setCancelled(true);
					scrambleChest(player, chest, loc);
				}
			}
		}
	}

	public void scrambleChest(Player player, InventoryHolder chest, Location loc) {

		Inventory original = chest.getInventory();
		Integer chestSize = original.getSize();

		int timeToSearch;

		if(player.hasPermission("chestsearch.searchtime")) {
			System.out.println("Player has searchtime permission");
			timeToSearch = plugin.getConfig().getInt("adv-search-time") * 20;
		}else {
			timeToSearch = plugin.getConfig().getInt("search-time") * 20;
		}

		Inventory scramble = Bukkit.createInventory(null, chestSize, "Searching...");

		List<ItemStack> chestOptions = new ArrayList<ItemStack>();
		chestOptions.clear();

		for(Material val : Material.values()) {
			ItemStack is = new ItemStack(val);
			chestOptions.add(is);
		}

		player.openInventory(scramble);
		
		Inventory newChest = Bukkit.createInventory(null, chestSize, "Chest");
		
		for(int i = 0; i < chestSize; i++) {
			newChest.setItem(i, original.getItem(i));
		}

		new BukkitRunnable() {

			//creates a loop counter for iterations, 20 counters = 1 second of scrambling
			int counter = 0;

			public void run() {
				if(counter >= timeToSearch) {

					if(plugin.getConfig().getBoolean("destroy-chest-after-opening")) {
						if(chest.getInventory().getHolder() instanceof DoubleChest) {
							DoubleChest c = (DoubleChest)chest.getInventory().getHolder();
				            Chest left = (Chest)c.getLeftSide();
				            Chest right = (Chest)c.getRightSide();
				            left.getBlock().setType(Material.AIR);
				            right.getBlock().setType(Material.AIR);
						}
						player.getWorld().getBlockAt(loc).setType(Material.AIR);		
					}
					
					if(cancel == false) {
						scramble.clear();
						player.closeInventory();
						player.openInventory(newChest);
						
						if(plugin.getConfig().getBoolean("destroy-chest-after-opening")) {
							player.getWorld().getBlockAt(loc).setType(Material.AIR);		
						}
						this.cancel();
					}else {
						this.cancel();
					}

				}

				//clear the scrambled inventory
				scramble.clear();


				//scrambles new inventory
				for(int s = 0; s < chestSize; s++) { 
					int i = RANDOM.nextInt(10); 
					if(i >= 8) {
						int c = RANDOM.nextInt(scramble.getSize());
						scramble.setItem(s, chestOptions.get(c));
					}
				}

				player.updateInventory();													
				counter++;
			}
		}.runTaskTimer(plugin, 1, 1);
		cancel = false;
	}
}


