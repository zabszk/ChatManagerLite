package net.zabszk.chatmanagerlite;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class Main extends JavaPlugin
{
	public static FileConfiguration config;
	public static String locked;
	
	public static Event event;
	
	@Override
	public void onEnable()
	{
		System.out.println("[ChatManagerLite] Activating plugin...");
		
		config = getConfig();
		locked = "";
		
		event = new Event();
		
		config.addDefault("AllowMetrics", true);
		config.addDefault("EmptyLines", 100);
		config.addDefault("AccessDeniedMessage", "&4[ChatManagerLite] You don't have permissions.");
		config.addDefault("ClearChatMessage", "&aChat has been cleared by:&3 $player");
		config.addDefault("ClearChatSelfMessage", "&aYou cleared your own chat.");
		config.addDefault("ClearChatOtherMessage", "&aYou have cleared chat of player:&3 $player");
		config.addDefault("ClearChatByOtherMessage", "&aYour chat has been cleared by:&3 $player");
		config.addDefault("LockChatMessage", "&6Chat has been locked by:&3 $player");
		config.addDefault("UnLockChatMessage", "&aChat has been unlocked by:&3 $player");
		config.addDefault("YouCantSpeakMessage", "&cChat has been locked by player:&3 $player&c, so you cannot speak.");
		
		List<String> cmds = new ArrayList();
		cmds.add("say");
		cmds.add("me");
		cmds.add("broadcast");
		cmds.add("bc");
		
		config.addDefault("DisallowedCommands", cmds);
		
		config.options().copyDefaults(true);
		saveConfig();
		
		getServer().getPluginManager().registerEvents(event, this);
		
		if (config.getBoolean("AllowMetrics"))
		{
			try {
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			} catch (IOException e) {
				System.out.println("Metrics error!");
	            e.printStackTrace();
			}
		}
		
		System.out.println("[ChatManagerLite] Plugin activated!");
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("[ChatManagerLite] Plugin deactivated.");;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("cc") && Perm(sender, "clearchat"))
		{
			for (Player pl : getOnline())
			{
				for (int i = 0; i < config.getInt("EmptyLines"); i++) pl.sendMessage("");
				
				pl.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("ClearChatMessage").replace("$player", sender.getName())));
			}
			
			System.out.println(ChatColor.translateAlternateColorCodes('&', config.getString("ClearChatMessage").replace("$player", sender.getName())));
		}
		if (cmd.getName().equalsIgnoreCase("ccl") && Perm(sender, "clearlocalchat"))
		{
			if (args.length == 0)
			{
				if (sender instanceof Player)
				{
					for (int i = 0; i < config.getInt("EmptyLines"); i++) sender.sendMessage("");
					
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("ClearChatSelfMessage")));
				}
				else sender.sendMessage(ChatColor.RED + "[ChatManagerLite] Usage: /" + label + " <nick>");
			}
			else if (args.length == 1)
			{
				if (Perm(sender, "clearotherschat"))
				{
					try
					{
						Player target = Bukkit.getPlayer(args[0]);
						
						for (int i = 0; i < config.getInt("EmptyLines"); i++) target.sendMessage("");
						
						target.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("ClearChatByOtherMessage").replace("$player", sender.getName())));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("ClearChatOtherMessage").replace("$player", target.getName())));
					}
					catch (Exception ex)
					{
						sender.sendMessage(ChatColor.RED + "[ChatManagerLite] " + args[0] + " is offline.");
					}
				}
			}
			else sender.sendMessage(ChatColor.RED + "[ChatManagerLite] Usage: /" + label + " [nick]");
		}
		else if (cmd.getName().equalsIgnoreCase("cl") && Perm(sender, "lockchat"))
		{
			for (Player pl : getOnline())
			{
				if (locked.length() < 1) pl.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("LockChatMessage").replace("$player", sender.getName())));
				else pl.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("UnLockChatMessage").replace("$player", sender.getName())));
			}
			
			if (locked.length() < 1) System.out.println(ChatColor.translateAlternateColorCodes('&', config.getString("LockChatMessage").replace("$player", sender.getName())));
			else System.out.println(ChatColor.translateAlternateColorCodes('&', config.getString("UnLockChatMessage").replace("$player", sender.getName())));
			
			if (locked.length() < 1) locked = sender.getName();
			else locked = "";
		}
		else if (cmd.getName().equalsIgnoreCase("cmr") && Perm(sender, "reload"))
		{
			reloadConfig();
			config = getConfig();
			
			sender.sendMessage(ChatColor.GREEN + "[ChatManagerLite] Configuration has been reloaded.");
		}
		
		return true;
	}
	
	private boolean Perm(CommandSender target, String perm)
	{
		if (target.hasPermission("chatmanagerlite." + perm)) return true;
		else
		{
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("AccessDeniedMessage")));
			
			return false;
		}
	}
	
	public static Player[] getOnline()
    {
        try
        {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object players = method.invoke(null);
            
            if (players instanceof Player[]) {
                Player[] oldPlayers = (Player[]) players;
                return oldPlayers;
             
            }
            else
            {
                Collection<Player> newPlayers = (Collection<Player>) players;
                
                Player[] online = new Player[newPlayers.size()];
                
                Object[] obj = newPlayers.toArray();
                
                int counter = 0;
                
                for (int i = 0; i < obj.length; i++)
                {
                	if (obj[i] instanceof Player)
                	{
                		String name = obj[i].toString().substring(obj[i].toString().indexOf("{"));
                		name = name.replace("{name=", "");
                		name = name.substring(0, name.length() - 1);
                		
                		online[counter] = Bukkit.getPlayer(name);
                		counter = counter + 1;
                	}
                }
                return online;
            }
         
        } 
        catch (Exception e)
        {
            System.out.println("Player online ERROR");
            System.out.println(e.toString());
            e.printStackTrace();
            
            return null;
        }
	}
}
