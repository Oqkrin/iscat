package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.screens.login.model.UserSettings;
import java.util.Optional;

public interface SettingsDAO {

    /** Carica le impostazioni di un utente */
    Optional<UserSettings> loadSettings(int userId);

    /** Aggiorna un singolo controllo */
    void updateControl(int userId, String columnName, String newKey);

    /** Elimina l'utente e tutte le sue impostazioni/salvataggi */
    void delete(int userId);

    /** Crea impostazioni di default per un nuovo utente (se necessario) */
    void createDefault(int userId);
}