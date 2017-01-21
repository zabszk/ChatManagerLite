package net.zabszk.chatmanagerlite;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event implements Listener
{
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onChat (AsyncPlayerChatEvent e)
	{
		if (Main.locked.length() > 0 && !e.getPlayer().hasPermission("chatmanagerlite.bypass.chat"))
		{
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.config.getString("YouCantSpeakMessage").replace("$player", Main.locked)));
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onCommandPreprocess (PlayerCommandPreprocessEvent e)
	{	
		if (Main.locked.length() > 0 && !e.getPlayer().hasPermission("chatmanagerlite.bypass.command"))
		{
			String command = e.getMessage();
			List<String> Disallowed = (List<String>) Main.config.get("DisallowedCommands");
			
			if (command.length() > 1)
			{
				command = command.substring(1);
				
				if (command.contains(" ")) command = command.substring(0, command.indexOf(" "));
			}
			
			for (String dis : Disallowed)
			{
				if (dis.equalsIgnoreCase(command))
				{
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.config.getString("YouCantSpeakMessage").replace("$player", Main.locked)));
					
					e.setCancelled(true);
				}
			}
		}
	}
}
