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

    public static IscatDB getInstance() {
        if (instance == null) {
            instance = new IscatDB();
        }
        return instance;
    }

    public void init() {
        connect();
        // Inizializza il repository SUBITO DOPO aver stabilito la connessione
        this.usersQueries = new SqliteUsersQueries();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("DB connected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Ritorna l'istanza unica del repository utenti, pronta all'uso.
     */
    public UsersQueriesInterface getUsersQueries() {
        return usersQueries;
    }
}