package uni.gaben.iscat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class IscatDB {

    private static IscatDB instance;
    private Connection connection;

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
}