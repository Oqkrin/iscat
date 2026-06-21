package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.BestiaryDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementazione SQLite del DAO per la gestione del Bestiario.
 * Gestisce il salvataggio e il recupero del numero di uccisioni dei nemici su database SQLite.
 */
public class SQLiteBestiaryDAO implements BestiaryDAO {

    /**
     * Incrementa il numero di uccisioni di un nemico per un utente specifico.
     * Utilizza una query di "UPSERT" (INSERT ON CONFLICT) per aggiornare il record se esiste già.
     */
    @Override
    public void incrementKill(int userId, String enemyKey, int count) {
        if (enemyKey == null || count <= 0) return;

        String sql = """
            INSERT INTO Bestiario (UserID, EnemyKEY, KilledTimes)
            VALUES (?, ?, ?)
            ON CONFLICT(UserID, EnemyKEY)
            DO UPDATE SET KilledTimes = KilledTimes + EXCLUDED.KilledTimes;
        """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, enemyKey.toLowerCase().trim());
            pstmt.setInt(3, count);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[SQLiteBestiaryDAO] Errore nell'incremento kill per: " + enemyKey);
            e.printStackTrace();
        }
    }

    /**
     * Recupera la mappa di tutte le uccisioni registrate dall'utente.
     * Mantiene l'ordine di inserimento del database grazie a {@link LinkedHashMap}.
     */
    @Override
    public Map<String, Integer> getKillsForUser(int userId) {
        Map<String, Integer> killsMap = new LinkedHashMap<>();
        String sql = "SELECT EnemyKEY, KilledTimes FROM Bestiario WHERE UserID = ?;";

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("EnemyKEY").toLowerCase().trim();
                    int times = rs.getInt("KilledTimes");
                    killsMap.put(key, times);
                }
            }

        } catch (SQLException e) {
            System.err.println("[SQLiteBestiaryDAO] Errore nel caricamento del bestiario per utente: " + userId);
            e.printStackTrace();
        }

        return killsMap;
    }
}