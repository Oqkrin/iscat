package uni.gaben.iscat.database;

import uni.gaben.iscat.database.dao.*;
import uni.gaben.iscat.database.sqlite.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class IscatDB {

    private static IscatDB instance;
    private static final String URL = "jdbc:sqlite:IscatDB.db";

    // Expose abstractions (Interfaces) instead of implementations
    private UserDAO userDAO;
    private ScoreDAO scoreDAO;
    private SettingsDAO settingsDAO;
    private EnemyDAO enemyDAO;

    private IscatDB() {}

    public static synchronized IscatDB getInstance() {
        if (instance == null) {
            instance = new IscatDB();
        }
        return instance;
    }

    /**
     * Initializes database engine drivers, performance options, and DAOs.
     */
    public void init() {
        // Explicitly load SQLite driver class
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find SQLite JDBC Driver", e);
        }

        // Optimize SQLite defaults (WAL mode enhances concurrency support)
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA foreign_keys=ON;");
        } catch (SQLException e) {
            System.err.println("Warning: Could not configure SQLite PRAGMAs: " + e.getMessage());
        }

        this.userDAO = new SQLiteUserDAO();
        this.scoreDAO = new SQLiteScoreDAO();
        this.settingsDAO = new SQLiteSettingsDAO();
        this.enemyDAO = new SQLiteEnemyDAO();
    }

    /**
     * Generates a fresh Connection instance.
     * Calling context is strictly responsible for closing it via try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Program to Interfaces Clean Getters
    public UserDAO getUserDAO() { return userDAO; }
    public ScoreDAO getScoreDAO() { return scoreDAO; }
    public SettingsDAO getSettingsDAO() { return settingsDAO; }
    public EnemyDAO getEnemyDAO() { return enemyDAO; }
}