package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.model.user.UserSettings;
import java.util.Optional;

/**
 * DAO per la gestione e la persistenza delle impostazioni utente (audio, video, controlli, estetica).
 */
public interface SettingsDAO {

    /** Carica tutte le impostazioni personalizzate di un utente. */
    Optional<UserSettings> loadSettings(int userId);

    /** Aggiorna un'impostazione video o di visualizzazione (es. fullscreen, risoluzione). */
    void updateDisplaySetting(int userId, String columnName, int value);

    /** * Aggiorna la mappatura di un singolo comando/tasto di gioco. */
    void updateControl(int userId, String columnName, String newKey);

    /** Elimina definitivamente l'utente e tutte le sue configurazioni associate. */
    void delete(int userId);

    /** Inizializza le impostazioni di default per un nuovo utente. */
    void createDefault(int userId);

    /** * Modifica il livello del volume di un canale audio specifico (es. master, musica, sfx). */
    void updateVolume(int userId, String columnName, double volumeValue);

    /** Aggiorna il tema grafico dell'interfaccia e il relativo colore esadecimale. */
    void updateThemeSetting(int userId, String choosenTheme, String hexvalue);

    /** Salva l'identificativo della skin selezionata dal giocatore. */
    void updatePlayerSkin(int userId, String skinKey);

    /** Recupera l'identificativo della skin attualmente equipaggiata dal giocatore. */
    String loadPlayerSkin(int userId);
}