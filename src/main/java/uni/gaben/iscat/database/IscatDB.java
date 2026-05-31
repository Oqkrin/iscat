package uni.gaben.iscat.database;

import uni.gaben.iscat.database.interfaces.UsersQueriesInterface;
import uni.gaben.iscat.database.sqlite.SqliteUsersQueries;
import uni.gaben.iscat.database.sqlite.SQLiteScoreDAO;
import uni.gaben.iscat.database.sqlite.SQLiteSettingsDAO;
import uni.gaben.iscat.database.sqlite.SQLiteEnemyDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class IscatDB {

    private static IscatDB instance;
    private Connection connection;

    private UsersQueriesInterface usersQueries;

    private SQLiteScoreDAO scoreDAO;
    private SQLiteSettingsDAO settingsDAO;
    private SQLiteEnemyDAO enemyDAO;

    private static final String URL = "jdbc:sqlite:IscatDB.db";

    private IscatDB() {}

    public static synchronized IscatDB getInstance() {
        if (instance == null) {
            instance = new IscatDB();
        }
        return instance;
    }

    public void init() {
        connect();
        this.usersQueries = new SqliteUsersQueries();

        this.scoreDAO = new SQLiteScoreDAO();
        this.settingsDAO = new SQLiteSettingsDAO();
        this.enemyDAO = new SQLiteEnemyDAO();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public UsersQueriesInterface getUsersQueries() {
        return usersQueries;
    }

    public SQLiteScoreDAO getScoreDAO() {
        return scoreDAO;
    }

    public SQLiteSettingsDAO getSettingsDAO() {
        return settingsDAO;
    }

    public SQLiteEnemyDAO getEnemyDAO() {
        return enemyDAO;
    }
}