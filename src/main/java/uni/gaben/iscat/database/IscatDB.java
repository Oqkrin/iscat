package uni.gaben.iscat.database;

import uni.gaben.iscat.database.dao.*;
import uni.gaben.iscat.database.sqlite.*;
import uni.gaben.iscat.utils.Cooldown;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class IscatDB {

    private static IscatDB instance;
    private static final String URL = "jdbc:sqlite:IscatDB.db";

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "IscatDB-Cooldown-Ticker");
        t.setDaemon(true);
        return t;
    });

    private Connection sharedConnection;
    private int activeTasks = 0;
    private final Cooldown connectionCooldown = new Cooldown(30);

    private UserDAO userDAO;
    private ScoreDAO scoreDAO;
    private SettingsDAO settingsDAO;
    private BestiaryDAO bestiaryDAO;

    private IscatDB() {
        init();
    }

    public static synchronized IscatDB getInstance() {
        if (instance == null) {
            instance = new IscatDB();
        }
        return instance;
    }

    public void init() {
        this.userDAO = new SQLiteUserDAO();
        this.scoreDAO = new SQLiteScoreDAO();
        this.settingsDAO = new SQLiteSettingsDAO();
        this.bestiaryDAO = new SQLiteBestiaryDAO();

        ticker.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (sharedConnection != null && activeTasks == 0) {
                    connectionCooldown.update(1);
                    if (connectionCooldown.isReady()) {
                        try {
                            if (!sharedConnection.isClosed()) {
                                sharedConnection.close();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            sharedConnection = null;
                        }
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC Driver non trovato", e);
            }
            sharedConnection = DriverManager.getConnection(URL);
        }
        connectionCooldown.start();
        return sharedConnection;
    }

    private synchronized void preExecuteTask() {
        activeTasks++;
        connectionCooldown.start();
    }

    private synchronized void postExecuteTask() {
        activeTasks--;
        if (activeTasks < 0) activeTasks = 0;
    }

    public void executeAsync(Runnable task) {
        dbExecutor.execute(() -> {
            preExecuteTask();
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                postExecuteTask();
            }
        });
    }

    public <T> CompletableFuture<T> queryAsync(Supplier<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        dbExecutor.submit(() -> {
            preExecuteTask();
            try {
                T result = task.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                postExecuteTask();
            }
        });
        return future;
    }

    public void shutdown() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        ticker.shutdown();

        synchronized (this) {
            if (sharedConnection != null) {
                try {
                    if (!sharedConnection.isClosed()) {
                        sharedConnection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public UserDAO getUserDAO() { return userDAO; }
    public ScoreDAO getScoreDAO() { return scoreDAO; }
    public SettingsDAO getSettingsDAO() { return settingsDAO; }
    public BestiaryDAO getBestiaryDAO() { return bestiaryDAO; }
}