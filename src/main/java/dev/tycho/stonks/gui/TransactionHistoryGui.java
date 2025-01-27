package dev.tycho.stonks.gui;

import dev.tycho.stonks.model.accountvisitors.IAccountVisitor;
import dev.tycho.stonks.model.core.AccountLink;
import dev.tycho.stonks.model.core.CompanyAccount;
import dev.tycho.stonks.model.core.HoldingsAccount;
import dev.tycho.stonks.model.logging.Transaction;
import dev.tycho.stonks.util.Util;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TransactionHistoryGui extends CollectionGuiBase<Transaction> {
  private AccountLink accountLink;

  private TransactionHistoryGui(AccountLink accountLink, String title) {
    super(databaseManager.getTransactionDao()
            .getTransactionsForAccount(accountLink, databaseManager.getAccountLinkDao().queryBuilder(), 100, 0),
        title);
    this.accountLink = accountLink;
  }

  @Override
  protected void customInit(Player player, InventoryContents contents) {
    contents.set(0, 0, ClickableItem.of(Util.item(Material.BARRIER, "Back to accounts"),
        e -> player.performCommand("stonks accounts " + accountLink.getCompany().getName())));
    IAccountVisitor visitor = new IAccountVisitor() {
      @Override
      public void visit(CompanyAccount a) {
        contents.set(0, 4, ClickableItem.empty(Util.item(Material.DIAMOND, a.getName())));
      }

      @Override
      public void visit(HoldingsAccount a) {
        contents.set(0, 4, ClickableItem.empty(Util.item(Material.GOLD_INGOT, a.getName())));
      }
    };
    accountLink.getAccount().accept(visitor);
  }

  @Override
  protected ClickableItem itemProvider(Player player, Transaction obj) {
    return ClickableItem.empty(ItemInfoHelper.transactionDisplayItem(obj));
  }

  public static class Builder {
    private AccountLink accountLink;
    private String title = "";


    public Builder() {
    }

    public TransactionHistoryGui.Builder accountLink(AccountLink accountLink) {
      this.accountLink = accountLink;
      return this;
    }

    public TransactionHistoryGui.Builder title(String title) {
      this.title = title;
      return this;
    }

    public TransactionHistoryGui open(Player player) {
      TransactionHistoryGui transactionHistoryGui = new TransactionHistoryGui(accountLink, title);
      transactionHistoryGui.show(player);
      return transactionHistoryGui;
    }
  }


}