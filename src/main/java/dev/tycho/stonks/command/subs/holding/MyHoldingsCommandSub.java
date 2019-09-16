package dev.tycho.stonks.command.subs.holding;

import dev.tycho.stonks.command.base.CommandSub;
import dev.tycho.stonks.managers.DatabaseHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MyHoldingsCommandSub extends CommandSub {
    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    @Override
    public void onCommand(Player player, String alias, String[] args) {
        DatabaseHelper.getInstance().showPlayerHoldings(player);
    }
}
