package dev.tycho.stonks.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import dev.tycho.stonks.Stonks;
import dev.tycho.stonks.command.MainCommand;
import dev.tycho.stonks.database.*;
import dev.tycho.stonks.model.accountvisitors.IAccountVisitor;
import dev.tycho.stonks.model.core.*;
import dev.tycho.stonks.model.logging.Transaction;
import dev.tycho.stonks.model.service.Service;
import dev.tycho.stonks.model.service.Subscription;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager extends SpigotModule {

  private JdbcConnectionSource connectionSource = null;
  private CompanyDao companyDao = null;
  private MemberDao memberDao = null;
  private CompanyAccountDao companyAccountDao = null;
  private AccountLinkDaoImpl accountlinkDao = null;
  private HoldingDaoImpl holdingDao = null;
  private HoldingsAccountDaoImpl holdingAccountDao = null;
  private TransactionDaoImpl transactionDao = null;
  private Dao<Service, Integer> serviceDao = null;
  private SubscriptionDaoImpl subscriptionDao = null;


  public DatabaseManager(Stonks plugin) {
    super("databaseManager", plugin);
  }

  @Override
  public void enable() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");

    String host = plugin.getConfig().getString("mysql.host");
    String port = plugin.getConfig().getString("mysql.port");
    String database = plugin.getConfig().getString("mysql.database");
    String username = plugin.getConfig().getString("mysql.username");
    String password = plugin.getConfig().getString("mysql.password");
    String useSsl = plugin.getConfig().getString("mysql.ssl");

    String databaseUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT&useSSL=" + useSsl;

    try {
      connectionSource = new JdbcConnectionSource(databaseUrl, username, password);
      companyDao = DaoManager.createDao(connectionSource, Company.class);
      memberDao = DaoManager.createDao(connectionSource, Member.class);
      companyAccountDao = new CompanyAccountDaoImpl(connectionSource);
      accountlinkDao = new AccountLinkDaoImpl(connectionSource);
      holdingDao = DaoManager.createDao(connectionSource, Holding.class);
      holdingAccountDao = new HoldingsAccountDaoImpl(connectionSource);
      transactionDao = new TransactionDaoImpl(connectionSource);
      subscriptionDao = new SubscriptionDaoImpl(connectionSource);
      serviceDao = DaoManager.createDao(connectionSource, Service.class);


      TableUtils.createTableIfNotExists(connectionSource, Company.class);
      TableUtils.createTableIfNotExists(connectionSource, Member.class);
      TableUtils.createTableIfNotExists(connectionSource, AccountLink.class);
      TableUtils.createTableIfNotExists(connectionSource, CompanyAccount.class);
      TableUtils.createTableIfNotExists(connectionSource, Holding.class);
      TableUtils.createTableIfNotExists(connectionSource, HoldingsAccount.class);
      TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
      TableUtils.createTableIfNotExists(connectionSource, Service.class);
      TableUtils.createTableIfNotExists(connectionSource, Subscription.class);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    new DatabaseHelper(plugin, this);
  }

  @Override
  public void addCommands() {
    MainCommand command = new MainCommand();
    addCommand("company", command);
    plugin.getCommand("company").setTabCompleter(command);
  }

  @Override
  public void disable() {
    try {
      connectionSource.close();
    } catch (IOException ignored) {
    }
  }

  public CompanyDao getCompanyDao() {
    return companyDao;
  }

  public MemberDao getMemberDao() {
    return memberDao;
  }

  public AccountLinkDaoImpl getAccountLinkDao() {
    return accountlinkDao;
  }

  public CompanyAccountDao getCompanyAccountDao() {
    return companyAccountDao;
  }

  Dao<HoldingsAccount, Integer> getHoldingAccountDao() {
    return holdingAccountDao;
  }

  public HoldingDao getHoldingDao() {
    return holdingDao;
  }

  public TransactionDaoImpl getTransactionDao() {
    return transactionDao;
  }

  //TODO move this method to a better place
  Account getAccountWithUUID(UUID uuid) {
    try {
      //Try company account first as those are the most common
      QueryBuilder<CompanyAccount, Integer> queryBuilder = getCompanyAccountDao().queryBuilder();
      queryBuilder.where().eq("uuid", uuid);
      CompanyAccount companyAccount = queryBuilder.queryForFirst();
      //If no company account was found try and find a holdings account
      if (companyAccount == null) {
        QueryBuilder<HoldingsAccount, Integer> queryBuilder2 = getHoldingAccountDao().queryBuilder();
        queryBuilder2.where().eq("uuid", uuid);
        //This will either return a result or null
        //If it is null there are no accounts with this match
        return queryBuilder2.queryForFirst();
      } else {
        return companyAccount;
      }

    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  //TODO move this too
  public void logTransaction(Transaction transaction) {
    try {
      getTransactionDao().create(transaction);
    } catch (SQLException e) {
      e.printStackTrace();
      Bukkit.broadcastMessage(ChatColor.RED + "SQL Exception creating a log ");
    }
  }

  public void refreshAccount(Account account) {
    //Refresh the account because we have to recurse quite deeply
    IAccountVisitor visitor = new IAccountVisitor() {
      @Override
      public void visit(CompanyAccount a) {
        try {
          DatabaseHelper.getInstance().getDatabaseManager().getCompanyAccountDao().refresh(a);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void visit(HoldingsAccount a) {
        try {
          DatabaseHelper.getInstance().getDatabaseManager().getHoldingsAccountDao().refresh(a);
          for (Holding h : a.getHoldings()) getHoldingDao().refresh(h);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    };
    account.accept(visitor);
  }

  public void updateAccount(Account account) {
    //Update the account database
    IAccountVisitor visitor = new IAccountVisitor() {
      @Override
      public void visit(CompanyAccount a) {
        try {
          getCompanyAccountDao().update(a);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void visit(HoldingsAccount a) {
        try {
          //Update the account and holdings
          getHoldingAccountDao().update(a);
          if (a.getHoldings() != null)
          for (Holding h : a.getHoldings()) if (h != null) getHoldingDao().update(h);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    };
    account.accept(visitor);
  }

  public Dao<Service, Integer> getServiceDao() {
    return serviceDao;
  }

  public HoldingsAccountDaoImpl getHoldingsAccountDao() {
    return holdingAccountDao;
  }

  public SubscriptionDaoImpl getSubscriptionDao() {
    return subscriptionDao;
  }
}
