package dev.tycho.stonks.gui;

import dev.tycho.stonks.managers.DatabaseManager;
import dev.tycho.stonks.model.AccountType;
import dev.tycho.stonks.model.Company;
import dev.tycho.stonks.util.Util;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfirmationGui implements InventoryProvider {

    public static DatabaseManager databaseManager;
    public static InventoryManager inventoryManager;

    private SmartInventory inventory;
    private Consumer<Boolean> onSelection;

    //turn this consumer into two consumers.
    public ConfirmationGui(Consumer<Boolean> onSelection, String title, Player player) {
        this.onSelection = onSelection;
        this.inventory = SmartInventory.builder()
                .id("ConfirmationGui")
                .provider(this)
                .manager(inventoryManager)
                .size(3, 9)
                .title(title)
                .build();
        inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();


        contents.set(1,3, ClickableItem.of(Util.item(Material.GREEN_WOOL, "YES"),
                e -> {
                    inventory.close(player);
                    onSelection.accept(true);
                }));
        contents.set(1,5, ClickableItem.of(Util.item(Material.RED_WOOL, "NO"),
                e -> {
                    inventory.close(player);
                    onSelection.accept(false);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    public static class Builder {
        private String title = "";
        private Consumer<Boolean> onSelected;

        public Builder() {

        }
        public ConfirmationGui.Builder typeSelected(Consumer<Boolean> onSelected) {
            this.onSelected = onSelected;
            return this;
        }
        public ConfirmationGui.Builder title(String title) {
            this.title = title;
            return this;
        }
        public ConfirmationGui open(Player player) {
            return new ConfirmationGui(onSelected, title, player);
        }
    }


}