package uni.gaben.iscat.database;

import uni.gaben.iscat.database.interfaces.UsersQueriesInterface;
import uni.gaben.iscat.database.sqlite.SqliteUsersQueries;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class IscatDB {

    private static IscatDB instance;
    private Connection connection;

    // Gestione centralizzata del repository utenti
    private UsersQueriesInterface usersQueries;

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
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ritorna la connessione corrente. Se è nulla o è stata chiusa
     * da un try-with-resources precedente, la riapre istantaneamente.
     */
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

    /**
     * Ritorna l'istanza unica del repository utenti, pronta all'uso.
     */
    public UsersQueriesInterface getUsersQueries() {
        return usersQueries;
    }
}