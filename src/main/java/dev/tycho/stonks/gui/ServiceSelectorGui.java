package dev.tycho.stonks.gui;

import dev.tycho.stonks.model.core.Company;
import dev.tycho.stonks.model.service.Service;
import dev.tycho.stonks.util.Util;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class ServiceSelectorGui extends CollectionGuiBase<Service> {

  private Company company;
  private Consumer<Service> onServiceSelected;

  public ServiceSelectorGui(Company company, String title, Consumer<Service> onServiceSelected, Player player) {
    super(company.getServices(), title);
    this.company = company;
    this.onServiceSelected = onServiceSelected;
  }

  @Override
  protected void customInit(Player player, InventoryContents contents) {
    contents.set(0, 4, ClickableItem.empty(Util.item(Material.getMaterial(company.getLogoMaterial()), company.getName())));
  }

  @Override
  protected ClickableItem itemProvider(Player player, Service obj) {
    return ClickableItem.of(ItemInfoHelper.serviceDisplayItem(obj),
        e -> {
          onServiceSelected.accept(obj);
          close(player);
        });
  }

  public static class Builder {
    private Company company = null;
    private String title = "";
    private Consumer<Service> onServiceSelected;

    public Builder() {

    }

    public ServiceSelectorGui.Builder company(Company company) {
      this.company = company;
      return this;
    }

    public ServiceSelectorGui.Builder title(String title) {
      this.title = title;
      return this;
    }

    public ServiceSelectorGui.Builder serviceSelected(Consumer<Service> onServiceSelected) {
      this.onServiceSelected = onServiceSelected;
      return this;
    }

    public ServiceSelectorGui open(Player player) {
      ServiceSelectorGui serviceSelectorGui = new ServiceSelectorGui(company, title, onServiceSelected, player);
      serviceSelectorGui.show(player);
      return serviceSelectorGui;
    }
  }
}
