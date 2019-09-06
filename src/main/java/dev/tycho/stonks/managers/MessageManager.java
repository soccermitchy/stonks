package dev.tycho.stonks.managers;

import dev.tycho.stonks.Stonks;
import dev.tycho.stonks.model.core.Member;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.List;

public class MessageManager extends SpigotModule {
  private DatabaseManager databaseManager;

  public MessageManager(Stonks plugin) {
    super("Message Manager", plugin);
    this.databaseManager = (DatabaseManager) plugin.getModule("databaseManager");
  }

  public static void sendHelpMessage(Player player, String label) {
    player.sendMessage(ChatColor.AQUA + "--------------------");
    player.sendMessage(ChatColor.GOLD + "To view all commands and more info about the plugin please go to:");
    player.sendMessage(ChatColor.GOLD + "https://stonks.tycho.dev/");
    player.sendMessage(ChatColor.AQUA + "--------------------");
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    List<Member> invites = databaseManager.getMemberDao().getInvites(event.getPlayer());
    if (invites.size() > 0) {
      event.getPlayer().sendMessage(ChatColor.AQUA + "You have " + ChatColor.GREEN + invites.size() + ChatColor.AQUA + " open company invites! Do " + ChatColor.GREEN + "/stonks invites" + ChatColor.AQUA + " to view them.");
    }
  }
}
